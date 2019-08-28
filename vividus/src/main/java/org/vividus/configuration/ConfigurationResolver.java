/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.PropertyPlaceholderHelper;

public final class ConfigurationResolver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationResolver.class);

    private static final String SYSTEM_PROPERTIES_PREFIX = "system.";
    private static final String VIVIDUS_SYSTEM_PROPERTY_FAMILY = "vividus.";
    private static final String CONFIGURATION_PROPERTY_FAMILY = "configuration.";
    private static final String ROOT = "";
    private static final String PROFILE = "profile";
    private static final String ENVIRONMENT = "environment";
    private static final String ENVIRONMENTS = "environments";
    private static final String SUITE = "suite";

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLACEHOLDER_VALUE_SEPARATOR = "=";

    private static ConfigurationResolver instance;

    private final Properties properties;

    private ConfigurationResolver(Properties properties)
    {
        this.properties = properties;
    }

    public static ConfigurationResolver getInstance() throws IOException
    {
        if (instance != null)
        {
            return instance;
        }

        PropertiesLoader propertiesLoader = new PropertiesLoader(BeanFactory.getResourcePatternResolver());

        Properties configurationProperties = propertiesLoader.loadFromSingleResource("configuration.properties");
        Properties overridingProperties = propertiesLoader.loadFromOptionalResource("overriding.properties");

        Properties properties = new Properties();
        properties.putAll(configurationProperties);
        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively("defaults"));

        Multimap<String, String> configuration = assembleConfiguration(configurationProperties, overridingProperties);
        for (Entry<String, String> configurationEntry : configuration.entries())
        {
            properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(configurationEntry.getKey(),
                    configurationEntry.getValue()));
        }

        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(ROOT));

        Properties deprecatedProperties = propertiesLoader.loadFromResourceTreeRecursively("deprecated");
        DeprecatedPropertiesHandler deprecatedPropertiesHandler = new DeprecatedPropertiesHandler(
                deprecatedProperties, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        deprecatedPropertiesHandler.replaceDeprecated(properties);

        Properties overridingAndSystemProperties = new Properties();
        overridingAndSystemProperties.putAll(overridingProperties);
        overridingAndSystemProperties.putAll(loadFilteredSystemProperties());

        deprecatedPropertiesHandler.replaceDeprecated(overridingAndSystemProperties, properties);

        properties.putAll(overridingAndSystemProperties);

        PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX,
                PLACEHOLDER_SUFFIX, PLACEHOLDER_VALUE_SEPARATOR, false);

        for (Entry<Object, Object> entry : properties.entrySet())
        {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            deprecatedPropertiesHandler.warnIfDeprecated(key, value);
            try
            {
                entry.setValue(propertyPlaceholderHelper.replacePlaceholders(value, properties::getProperty));
            }
            catch (IllegalArgumentException e)
            {
                processEnvironmentVariable(entry, value);
            }
        }
        deprecatedPropertiesHandler.removeDeprecated(properties);
        resolveSpelExpressions(properties);
        processSystemProperties(properties);

        instance = new ConfigurationResolver(properties);
        return instance;
    }

    private static Multimap<String, String> assembleConfiguration(Properties configurationProperties,
            Properties overridingProperties)
    {
        List<String> environments = Stream.of(StringUtils.split(
                getConfigurationPropertyValue(configurationProperties, overridingProperties, ENVIRONMENTS), ','))
                .collect(Collectors.toList());

        Multimap<String, String> configuration = LinkedHashMultimap.create();
        configuration.put(PROFILE,
                getConfigurationPropertyValue(configurationProperties, overridingProperties, PROFILE));
        // First environments in the sequence have high priority then next ones
        configuration.putAll(ENVIRONMENT, Lists.reverse(environments));
        configuration.put(SUITE, getConfigurationPropertyValue(configurationProperties, overridingProperties, SUITE));
        return configuration;
    }

    private static Map<String, String> loadFilteredSystemProperties()
    {
        Properties systemProperties = System.getProperties();
        return systemProperties.stringPropertyNames().stream()
                .filter(p -> p.startsWith(VIVIDUS_SYSTEM_PROPERTY_FAMILY))
                .filter(p -> !p.startsWith(VIVIDUS_SYSTEM_PROPERTY_FAMILY + CONFIGURATION_PROPERTY_FAMILY))
                .collect(Collectors.toMap(
                    p -> StringUtils.removeStart(p, VIVIDUS_SYSTEM_PROPERTY_FAMILY), systemProperties::getProperty));
    }

    private static String getConfigurationPropertyValue(Properties configurationProperties,
            Properties overridingProperties, String key)
    {
        String propertyName = CONFIGURATION_PROPERTY_FAMILY + key;
        String value = System.getProperty(VIVIDUS_SYSTEM_PROPERTY_FAMILY + propertyName,
                System.getProperty(VIVIDUS_SYSTEM_PROPERTY_FAMILY + key,
                        System.getProperty(propertyName, System.getProperty(key))));
        if (value == null)
        {
            value = overridingProperties.getProperty(propertyName);
            if (value == null)
            {
                value = configurationProperties.getProperty(propertyName);
                if (value == null)
                {
                    throw new IllegalStateException(key + " is not set");
                }
            }
        }
        else
        {
            overridingProperties.put(propertyName, value);
        }
        return value;
    }

    private static void resolveSpelExpressions(Properties properties)
    {
        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            String value = (String) entry.getValue();
            try
            {
                entry.setValue(spelExpressionParser.parseExpression(value, ParserContext.TEMPLATE_EXPRESSION)
                        .getValue());
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Exception during evaluation of expression " + value
                        + " for property '" + entry.getKey() + "'", e);
            }
        }
    }

    private static void processSystemProperties(Properties properties)
    {
        Iterator<Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext())
        {
            Entry<Object, Object> entry = iterator.next();
            String key = (String) entry.getKey();
            if (key.startsWith(SYSTEM_PROPERTIES_PREFIX))
            {
                System.setProperty(StringUtils.removeStart(key, SYSTEM_PROPERTIES_PREFIX), (String) entry.getValue());
                iterator.remove();
            }
        }
    }

    private static void processEnvironmentVariable(Entry<Object, Object> entry, String value)
    {
        String[] placeholders = StringUtils.substringsBetween(value, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        String updatedValue = value;
        if (null != placeholders)
        {
            for (int i = 0; i < placeholders.length; i++)
            {
                String environmentVar = null == System.getenv(placeholders[i]) ? placeholders[i]
                        : System.getenv(placeholders[i]);
                updatedValue = StringUtils.replace(updatedValue, PLACEHOLDER_PREFIX + placeholders[i]
                        + PLACEHOLDER_SUFFIX, environmentVar);
            }
        }
        entry.setValue(updatedValue);
    }

    public static void reset()
    {
        instance = null;
    }

    public Properties getProperties()
    {
        isReset();
        return (Properties) properties.clone();
    }

    private static void isReset()
    {
        if (instance == null)
        {
            throw new IllegalStateException("ConfigurationResolver has not been initialized after the reset");
        }
    }

    private static final class PropertiesLoader
    {
        private static final String ROOT_LOCATION = "classpath*:/properties/";
        private static final String DELIMITER = "/";

        private final ResourcePatternResolver resourcePatternResolver;

        PropertiesLoader(ResourcePatternResolver resourcePatternResolver)
        {
            this.resourcePatternResolver = resourcePatternResolver;
        }

        Properties loadFromSingleResource(String resourceName) throws IOException
        {
            String location = ROOT_LOCATION + resourceName;
            Resource[] resources = resourcePatternResolver.getResources(location);
            int resourcesLength = resources.length;
            if (resourcesLength == 0)
            {
                return new Properties();
            }
            if (resourcesLength > 1)
            {
                throw new IllegalStateException(
                        "Exactly one resource is expected: " + location + ", but found: " + resourcesLength);
            }
            return loadProperties(resources[0]);
        }

        Properties loadFromOptionalResource(String resourceName) throws IOException
        {
            Resource resource = resourcePatternResolver.getResource("classpath:/" + resourceName);
            return resource.exists() ? loadProperties(resource) : new Properties();
        }

        Properties loadFromResourceTreeRecursively(String... resourcePathParts) throws IOException
        {
            String resourcePath = String.join(DELIMITER, resourcePathParts);
            List<Resource> propertyResources = collectResourcesRecursively(resourcePatternResolver, resourcePath);
            LOGGER.info("Loading properties from /{}", resourcePath);
            Properties loadedProperties = loadProperties(propertyResources.toArray(new Resource[0]));
            loadedProperties.forEach((key, value) -> LOGGER.debug("{}=={}", key, value));
            return loadedProperties;
        }

        private static List<Resource> collectResourcesRecursively(ResourcePatternResolver resourcePatternResolver,
                String resourcePath) throws IOException
        {
            List<Resource> propertyResources = new LinkedList<>();
            StringBuilder path = new StringBuilder(ROOT_LOCATION);
            String[] locationParts = resourcePath.isEmpty() ? new String[] { resourcePath }
                    : StringUtils.split(resourcePath, DELIMITER);
            for (int i = 0; i < locationParts.length; i++)
            {
                boolean deepestLevel = i + 1 == locationParts.length;
                String locationPart = locationParts[i];
                path.append(locationPart);
                if (!locationPart.isEmpty())
                {
                    path.append(DELIMITER);
                }
                String resourceLocation = path.toString() + "*.properties";
                Resource[] resources = resourcePatternResolver.getResources(resourceLocation);
                if (deepestLevel && resources.length == 0)
                {
                    throw new IllegalStateException(
                            "No files with properties were found at location with pattern: " + resourceLocation);
                }
                propertyResources.addAll(Stream.of(resources).filter(Resource::exists).collect(Collectors.toList()));
            }
            return propertyResources;
        }

        private static Properties loadProperties(Resource... propertyResources) throws IOException
        {
            PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
            propertiesFactoryBean.setFileEncoding(StandardCharsets.UTF_8.name());
            propertiesFactoryBean.setLocations(propertyResources);
            propertiesFactoryBean.setSingleton(false);
            return propertiesFactoryBean.getObject();
        }
    }
}

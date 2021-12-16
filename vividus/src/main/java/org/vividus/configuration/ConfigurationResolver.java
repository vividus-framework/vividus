/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
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
    private static final String VIVIDUS_ENCRYPTOR_PASSWORD_PROPERTY = "vividus.encryptor.password";
    private static final String DEFAULT_ENCRYPTOR_ALGORITHM = "PBEWithMD5AndDES";
    private static final String DEFAULT_ENCRYPTOR_PASSWORD = "82=thuMUH@";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationResolver.class);

    private static final String VIVIDUS_SYSTEM_PROPERTY_FAMILY = "vividus.";
    private static final String CONFIGURATION_PROPERTY_FAMILY = "configuration.";
    private static final String ROOT = "";
    private static final String PROFILES = "profiles";
    private static final String ENVIRONMENTS = "environments";
    private static final String SUITES = "suites";

    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String PLACEHOLDER_VALUE_SEPARATOR = "=";

    private static final String[] DEFAULTS_PATHS = {
        "org/vividus/http/client",
        "org/vividus/util"
    };

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

        Properties configurationProperties = propertiesLoader.loadConfigurationProperties();
        Properties overridingProperties = propertiesLoader.loadOverridingProperties();

        Properties properties = new Properties();
        properties.putAll(configurationProperties);

        for (String defaultPath : DEFAULTS_PATHS)
        {
            properties.putAll(propertiesLoader.loadResourceFromClasspath(true, defaultPath, "defaults.properties"));
        }

        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(true, "defaults"));
        properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(true, ROOT));

        Multimap<String, String> configuration = assembleConfiguration(configurationProperties, overridingProperties);
        for (Entry<String, String> configurationEntry : configuration.entries())
        {
            properties.putAll(propertiesLoader.loadFromResourceTreeRecursively(true, configurationEntry.getKey(),
                    configurationEntry.getValue()));
        }

        Properties deprecatedProperties = propertiesLoader.loadFromResourceTreeRecursively(false, "deprecated");
        DeprecatedPropertiesHandler deprecatedPropertiesHandler = new DeprecatedPropertiesHandler(
                deprecatedProperties, PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);
        deprecatedPropertiesHandler.replaceDeprecated(properties);

        Properties overridingAndSystemProperties = new Properties();
        overridingAndSystemProperties.putAll(overridingProperties);
        overridingAndSystemProperties.putAll(System.getenv());
        overridingAndSystemProperties.putAll(loadFilteredSystemProperties());

        deprecatedPropertiesHandler.replaceDeprecated(overridingAndSystemProperties, properties);

        properties.putAll(overridingAndSystemProperties);

        resolveSpelExpressions(properties, true);

        PropertyPlaceholderHelper propertyPlaceholderHelper = createPropertyPlaceholderHelper(false);

        for (Entry<Object, Object> entry : properties.entrySet())
        {
            Object value = entry.getValue();
            deprecatedPropertiesHandler.warnIfDeprecated((String) entry.getKey(), value);
            if (value instanceof String)
            {
                entry.setValue(propertyPlaceholderHelper.replacePlaceholders((String) value, properties::getProperty));
            }
        }
        deprecatedPropertiesHandler.removeDeprecated(properties);
        resolveSpelExpressions(properties, false);

        StringEncryptor stringEncryptor = createStringEncryptor(properties);
        BeanFactory.registerBean("stringEncryptor", StringEncryptor.class, () -> stringEncryptor);
        PropertiesDecryptor decryptor = new PropertiesDecryptor(stringEncryptor);
        SystemPropertiesProcessor systemPropertiesProcessor = new SystemPropertiesProcessor(decryptor);
        systemPropertiesProcessor.process(properties);
        properties = decryptor.decryptProperties(properties);

        instance = new ConfigurationResolver(properties);
        return instance;
    }

    private static StringEncryptor createStringEncryptor(Properties properties)
    {
        String password = System.getProperty(VIVIDUS_ENCRYPTOR_PASSWORD_PROPERTY);
        if (password == null)
        {
            password = properties.getProperty("system." + VIVIDUS_ENCRYPTOR_PASSWORD_PROPERTY);
        }
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(DEFAULT_ENCRYPTOR_ALGORITHM);
        encryptor.setPassword(password != null ? password : DEFAULT_ENCRYPTOR_PASSWORD);
        return encryptor;
    }

    private static PropertyPlaceholderHelper createPropertyPlaceholderHelper(boolean ignoreUnresolvablePlaceholders)
    {
        return new PropertyPlaceholderHelper(PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX, PLACEHOLDER_VALUE_SEPARATOR,
                ignoreUnresolvablePlaceholders);
    }

    private static Multimap<String, String> assembleConfiguration(Properties configurationProperties,
            Properties overridingProperties)
    {
        String profiles = getConfigurationPropertyValue(configurationProperties, overridingProperties, PROFILES);
        String environments = getConfigurationPropertyValue(configurationProperties, overridingProperties,
                ENVIRONMENTS);
        String suites = getConfigurationPropertyValue(configurationProperties, overridingProperties, SUITES);

        Properties mergedProperties = new Properties();
        mergedProperties.putAll(configurationProperties);
        mergedProperties.putAll(overridingProperties);
        PropertyPlaceholderHelper propertyPlaceholderHelper = createPropertyPlaceholderHelper(true);

        profiles = propertyPlaceholderHelper.replacePlaceholders(profiles, mergedProperties::getProperty);
        environments = propertyPlaceholderHelper.replacePlaceholders(environments, mergedProperties::getProperty);
        suites = propertyPlaceholderHelper.replacePlaceholders(suites, mergedProperties::getProperty);

        Multimap<String, String> configuration = LinkedHashMultimap.create();
        configuration.putAll("profile", asPaths(profiles));
        configuration.putAll("environment", asPaths(environments));
        configuration.putAll("suite", asPaths(suites));
        return configuration;
    }

    private static List<String> asPaths(String value)
    {
        // First configuration paths in the sequence have high priority then next ones
        return Stream.of(StringUtils.split(value, ','))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Lists::reverse));
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
                    throw new IllegalStateException(
                            String.format("The '%s%s' property is not set", CONFIGURATION_PROPERTY_FAMILY, key));
                }
            }
        }
        else
        {
            overridingProperties.put(propertyName, value);
        }
        return value;
    }

    private static void resolveSpelExpressions(Properties properties, boolean ignoreValuesWithPropertyPlaceholders)
    {
        Optional<Set<String>> propertyPlaceholders = ignoreValuesWithPropertyPlaceholders
                ? Optional.of(properties.stringPropertyNames().stream()
                        .map(n -> PLACEHOLDER_PREFIX + n + PLACEHOLDER_SUFFIX)
                        .collect(Collectors.toSet()))
                : Optional.empty();

        SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof String)
            {
                String strValue = (String) value;
                if (propertyPlaceholders.stream().flatMap(Set::stream).noneMatch(strValue::contains))
                {
                    try
                    {
                        entry.setValue(spelExpressionParser.parseExpression(strValue, ParserContext.TEMPLATE_EXPRESSION)
                                .getValue());
                    }
                    catch (Exception e)
                    {
                        throw new IllegalStateException(
                                "Exception during evaluation of expression " + strValue + " for property '" + entry
                                        .getKey() + "'", e);
                    }
                }
            }
        }
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
        private static final String ROOT_LOCATION = CLASSPATH_ALL_URL_PREFIX + "/properties/";
        private static final String DELIMITER = "/";

        private final ResourcePatternResolver resourcePatternResolver;

        PropertiesLoader(ResourcePatternResolver resourcePatternResolver)
        {
            this.resourcePatternResolver = resourcePatternResolver;
        }

        Properties loadConfigurationProperties() throws IOException
        {
            String location = ROOT_LOCATION + "configuration.properties";
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

        Properties loadOverridingProperties() throws IOException
        {
            Resource resource = resourcePatternResolver.getResource("classpath:/overriding.properties");
            return resource.exists() ? loadProperties(resource) : new Properties();
        }

        Properties loadFromResourceTreeRecursively(boolean failOnMissingResource, String... resourcePathParts)
                throws IOException
        {
            String resourcePath = String.join(DELIMITER, resourcePathParts);
            List<Resource> propertyResources = collectResourcesRecursively(resourcePath, failOnMissingResource);
            return loadPropertiesFromResources(resourcePath, propertyResources);
        }

        Properties loadResourceFromClasspath(boolean failOnMissingResource, String resourcePath, String resourcePattern)
                throws IOException
        {
            return loadPropertiesFromResources(resourcePath,
                    collectResources(failOnMissingResource, CLASSPATH_ALL_URL_PREFIX, resourcePattern, resourcePath));
        }

        private Properties loadPropertiesFromResources(String resourcePath, List<Resource> propertyResources)
                throws IOException
        {
            LOGGER.info("Loading properties from /{}", resourcePath);
            Properties loadedProperties = loadProperties(propertyResources.toArray(new Resource[0]));
            loadedProperties.forEach((key, value) -> LOGGER.debug("{}=={}", key, value));
            return loadedProperties;
        }

        private List<Resource> collectResourcesRecursively(String resourcePath, boolean failOnMissingResource)
                throws IOException
        {
            String[] locationParts = resourcePath.isEmpty() ? new String[] { resourcePath }
                    : StringUtils.split(resourcePath, DELIMITER);
            return collectResources(failOnMissingResource, ROOT_LOCATION, "*.properties", locationParts);
        }

        private List<Resource> collectResources(boolean failOnMissingResource, String root, String resourcePattern,
                String... locationParts) throws IOException
        {
            List<Resource> propertyResources = new LinkedList<>();
            StringBuilder path = new StringBuilder(root);
            for (int i = 0; i < locationParts.length; i++)
            {
                boolean deepestLevel = i + 1 == locationParts.length;
                String locationPart = locationParts[i];
                path.append(locationPart);
                if (!locationPart.isEmpty())
                {
                    path.append(DELIMITER);
                }
                String resourceLocation = path + resourcePattern;
                Resource[] resources = resourcePatternResolver.getResources(resourceLocation);
                if (deepestLevel && resources.length == 0 && failOnMissingResource)
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

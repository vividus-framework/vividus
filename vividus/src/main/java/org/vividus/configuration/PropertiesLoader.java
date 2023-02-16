/*
 * Copyright 2019-2023 the original author or authors.
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

class PropertiesLoader
{
    static final String CONFIGURATION_FILENAME = "configuration.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesLoader.class);
    private static final String ROOT_LOCATION = CLASSPATH_ALL_URL_PREFIX + "/properties/";
    private static final String DELIMITER = "/";

    private final ResourcePatternResolver resourcePatternResolver;

    private boolean validateConfigurationProperties;

    PropertiesLoader(ResourcePatternResolver resourcePatternResolver)
    {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    Properties loadConfigurationProperties() throws IOException
    {
        String location = ROOT_LOCATION + CONFIGURATION_FILENAME;
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

    Properties loadFromResourceTreeRecursively(boolean failOnMissingResource, Predicate<Resource> filter,
            String... resourcePathParts) throws IOException
    {
        String resourcePath = String.join(DELIMITER, resourcePathParts);
        List<Resource> propertyResources = collectResourcesRecursively(resourcePath, failOnMissingResource, filter);
        return loadPropertiesFromResources(resourcePath, propertyResources);
    }

    Properties loadFromResourceTreeRecursively(boolean failOnMissingResource, String... resourcePathParts)
            throws IOException
    {
        return loadFromResourceTreeRecursively(failOnMissingResource, r -> true, resourcePathParts);
    }

    Properties loadDefaultProperties(String resourcePath) throws IOException
    {
        return loadPropertiesFromResources(resourcePath, collectResources(true, CLASSPATH_ALL_URL_PREFIX,
                "defaults.properties", r -> true, resourcePath));
    }

    void prohibitConfigurationProperties()
    {
        this.validateConfigurationProperties = true;
    }

    private Properties loadPropertiesFromResources(String resourcePath, List<Resource> propertyResources)
            throws IOException
    {
        Properties loadedProperties = new Properties();
        LOGGER.info("Loading properties from /{}", resourcePath);
        for (Resource resource : propertyResources)
        {
            loadedProperties.putAll(loadProperties(resource));
        }
        loadedProperties.forEach((key, value) -> LOGGER.debug("{}=={}", key, value));
        return loadedProperties;
    }

    private List<Resource> collectResourcesRecursively(String resourcePath, boolean failOnMissingResource,
            Predicate<Resource> filter) throws IOException
    {
        String[] locationParts = resourcePath.isEmpty() ? new String[] { resourcePath }
                                                        : StringUtils.split(resourcePath, DELIMITER);
        return collectResources(failOnMissingResource, ROOT_LOCATION, "*.properties", filter, locationParts);
    }

    private List<Resource> collectResources(boolean failOnMissingResource, String root, String resourcePattern,
            Predicate<Resource> resourceFilter, String... locationParts) throws IOException
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
            propertyResources.addAll(Stream.of(resources).filter(Resource::exists)
                .filter(resourceFilter).collect(Collectors.toList()));
        }
        return propertyResources;
    }

    private Properties loadProperties(Resource propertyResources) throws IOException
    {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setFileEncoding(StandardCharsets.UTF_8.name());
        propertiesFactoryBean.setLocation(propertyResources);
        propertiesFactoryBean.setSingleton(false);
        Properties properties = propertiesFactoryBean.getObject();
        return validateConfigurationProperties ? validate(properties, propertyResources) : properties;
    }

    private Properties validate(Properties properties, Resource propertyResources)
    {
        List<String> configurationProperties =
            properties.entrySet()
                      .stream()
                      .map(Entry::getKey)
                      .map(String.class::cast)
                      .filter(k -> k.startsWith(ConfigurationResolver.CONFIGURATION_PROPERTY_FAMILY)
                                || k.startsWith(ConfigurationResolver.CONFIGURATION_SET_PROPERTY_FAMILY))
                      .collect(Collectors.toList());
        if (configurationProperties.isEmpty())
        {
            return properties;
        }
        throw new IllegalStateException(String.format(
                "The configuration.* and configuration-set.* properties can be set using: System properties,"
                        + " overriding.properties file or configuration.properties file. But found: %s; In "
                        + "resource: %s", configurationProperties, propertyResources));
    }
}

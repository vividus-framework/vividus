/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.resource;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.util.property.IPropertyParser;

public class TestResourceLoader implements ITestResourceLoader
{
    private static final String VARIABLES_PROPERTY_PREFIX = "bdd.resource-loader.";
    private static final String SEPARATOR = "/";
    private static final String FILE_URL_PREFIX = "file://";

    private final List<String> resourceLoadParameters;
    private final ResourcePatternResolver resourcePatternResolver;

    public TestResourceLoader(IPropertyParser propertyParser, ResourcePatternResolver resourcePatternResolver)
    {
        resourceLoadParameters = propertyParser.getPropertyValuesByPrefix(VARIABLES_PROPERTY_PREFIX).values().stream()
                .filter(p -> p != null && !p.isEmpty())
                .collect(Collectors.toList());
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public Resource[] getResources(String resourceLocation, String resourcePattern)
    {
        try
        {
            String normalizedResourceLocation = StringUtils.appendIfMissing(resourceLocation, SEPARATOR);
            String locationPattern;
            String fullLocationPattern;
            if (resourceLocation.startsWith(FILE_URL_PREFIX))
            {
                locationPattern = normalizedResourceLocation;
                fullLocationPattern = locationPattern + resourcePattern;
            }
            else
            {
                locationPattern = CLASSPATH_ALL_URL_PREFIX + normalizedResourceLocation;
                fullLocationPattern = locationPattern + "**/" + resourcePattern;
            }
            Resource[] allResources = resourcePatternResolver.getResources(fullLocationPattern);
            List<URL> resourceUrls = new ArrayList<>(allResources.length);
            for (Resource resource : allResources)
            {
                resourceUrls.add(resource.getURL());
            }
            Optional<String> deepestResourcePath = resourceUrls.stream()
                    .map(URL::toString)
                    .map(resourceUrl -> StringUtils.substringAfter(resourceUrl, normalizedResourceLocation))
                    .map(this::generatePathByLoadConfig)
                    .max(Comparator.comparing(String::length));
            StringBuilder resourcePatternBuilder = new StringBuilder(locationPattern);
            deepestResourcePath.ifPresent(resourcePatternBuilder::append);
            String baseResourcePattern = resourcePatternBuilder.toString();
            return resourcePatternResolver.getResources(baseResourcePattern + resourcePattern);
        }
        catch (IOException e)
        {
            throw new ResourceLoadException(e);
        }
    }

    private String generatePathByLoadConfig(String relativeResourcePath)
    {
        return generatePathByLoadConfig(relativeResourcePath, new StringBuilder(), resourceLoadParameters);
    }

    private String generatePathByLoadConfig(String initialPath, StringBuilder pathBuilder, List<String> initialProps)
    {
        List<String> props = new ArrayList<>(initialProps);
        for (String prop : initialProps)
        {
            String pathStart = prop.concat(SEPARATOR);
            if (initialPath.startsWith(pathStart))
            {
                props.remove(prop);
                String localPath = initialPath.replaceFirst(pathStart, StringUtils.EMPTY);
                pathBuilder.append(prop).append(SEPARATOR);
                return generatePathByLoadConfig(localPath, pathBuilder, props);
            }
        }
        return pathBuilder.toString();
    }
}

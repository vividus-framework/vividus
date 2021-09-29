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

package org.vividus.bdd.resource;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

public class BddResourceLoader implements IBddResourceLoader
{
    private static final String SEPARATOR = "/";
    private static final String FILE_URL_PREFIX = "file://";

    private ResourcePatternResolver resourcePatternResolver;

    private IResourceLoadConfiguration resourceLoadConfiguration;

    @Override
    public Resource getResource(String rawResourcePath)
    {
        String parentDir = FilenameUtils.getFullPathNoEndSeparator(rawResourcePath);
        String fileName = FilenameUtils.getName(rawResourcePath);
        Resource[] resources = getResources(parentDir, fileName);
        if (resources.length > 1)
        {
            throw new ResourceLoadException("More than 1 resource is found for " + rawResourcePath);
        }
        if (resources.length == 0)
        {
            throw new ResourceLoadException("No resource is found for " + rawResourcePath);
        }
        return resources[0];
    }

    @Override
    public Resource[] getResources(String resourceLocation, String resourcePattern)
    {
        String normalizedResourceLocation = StringUtils.appendIfMissing(resourceLocation, SEPARATOR);
        String locationPattern = resourceLocation.startsWith(FILE_URL_PREFIX)
                ? normalizedResourceLocation : CLASSPATH_ALL_URL_PREFIX + normalizedResourceLocation;
        Resource[] allResources = getResources(locationPattern + "**/" + resourcePattern);
        List<URL> resourceUrls = new ArrayList<>(allResources.length);
        for (Resource resource : allResources)
        {
            try
            {
                resourceUrls.add(resource.getURL());
            }
            catch (IOException e)
            {
                throw new ResourceLoadException(e);
            }
        }
        Optional<String> deepestResourcePath = resourceUrls.stream()
                .map(URL::toString)
                .map(resourceUrl -> StringUtils.substringAfter(resourceUrl, normalizedResourceLocation))
                .map(this::generatePathByLoadConfig)
                .max(Comparator.comparing(String::length));
        StringBuilder resourcePatternBuilder = new StringBuilder(locationPattern);
        deepestResourcePath.ifPresent(resourcePatternBuilder::append);
        String baseResourcePattern = resourcePatternBuilder.toString();
        return getResources(baseResourcePattern + resourcePattern);
    }

    private Resource[] getResources(String locationPattern)
    {
        try
        {
            return resourcePatternResolver.getResources(locationPattern);
        }
        catch (IOException e)
        {
            throw new ResourceLoadException(e);
        }
    }

    private String generatePathByLoadConfig(String relativeResourcePath)
    {
        return generatePathByLoadConfig(relativeResourcePath, new StringBuilder(),
                resourceLoadConfiguration.getResourceLoadParametersValues());
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

    public void setResourcePatternResolver(ResourcePatternResolver resourcePatternResolver)
    {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public void setResourceLoadConfiguration(IResourceLoadConfiguration resourceLoadConfiguration)
    {
        this.resourceLoadConfiguration = resourceLoadConfiguration;
    }
}

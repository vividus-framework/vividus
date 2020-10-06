/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;

public final class ResourceUtils
{
    private ResourceUtils()
    {
    }

    /**
     * Loads resource from the classpath root as UTF-8 string
     * @param resourceName Resource name
     * @return Resource as string
     */
    public static String loadResource(String resourceName)
    {
        return loadResource(ResourceUtils.class, ensureRootPath(resourceName));
    }

    /**
     * Loads resource from the classpath root as byte array
     * @param resourceName Resource name
     * @return Resource as string
     */
    public static byte[] loadResourceAsByteArray(String resourceName)
    {
        return loadResourceAsByteArray(ResourceUtils.class, ensureRootPath(resourceName));
    }

    /**
     * Loads resource as UTF-8 string
     * @param clazz Class to search resource relatively
     * @param resourceName Resource name
     * @return Resource as string
     */
    public static String loadResource(Class<?> clazz, String resourceName)
    {
        return loadResource(clazz, resourceName, resource -> IOUtils.toString(resource, StandardCharsets.UTF_8));
    }

    /**
     * Loads resource as byte array
     * @param clazz Class to search resource relatively
     * @param resourceName Resource name
     * @return Resource as string
     */
    public static byte[] loadResourceAsByteArray(Class<?> clazz, String resourceName)
    {
        return loadResource(clazz, resourceName, IOUtils::toByteArray);
    }

    /**
     * Searches for resource and return URL if it's found, otherwise throws exception
     * @param clazz Class to search resource relatively
     * @param resourceName Resource name
     * @return resource URL
     */
    public static URL findResource(Class<?> clazz, String resourceName)
    {
        URL resource = clazz.getResource(resourceName);
        if (resource != null)
        {
            return resource;
        }
        throw new IllegalArgumentException("Resource with name " + resourceName + " for " + clazz + " is not found");
    }

    /**
     * Loads File
     * @param clazz Class to search resource relatively
     * @param filePath Path to file
     * @return File
     */
    public static File loadFile(Class<?> clazz, String filePath)
    {
        URL resourceUrl = findResource(clazz, filePath);
        try
        {
            return Paths.get(resourceUrl.toURI()).toFile();
        }
        catch (URISyntaxException ex)
        {
            return Paths.get(resourceUrl.getFile()).toFile();
        }
    }

    private static String ensureRootPath(String resourceName)
    {
        return StringUtils.prependIfMissing(resourceName, "/");
    }

    private static <T> T loadResource(Class<?> clazz, String resourceName,
            FailableFunction<URL, T, IOException> resourceReader)
    {
        URL resource = findResource(clazz, resourceName);
        try
        {
            return resourceReader.apply(resource);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}

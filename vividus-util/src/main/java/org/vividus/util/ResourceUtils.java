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

package org.vividus.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
     * @return Resource as byte array
     */
    public static byte[] loadResourceAsByteArray(Class<?> clazz, String resourceName)
    {
        return loadResource(clazz, resourceName, IOUtils::toByteArray);
    }

    /**
     * Loads resource or file as byte array
     *
     * @param resourceNameOrFilePath Resource name or file path to load
     * @return Resource or file content as byte array
     * @throws IOException if an I/O error occurs
     */
    public static byte[] loadResourceOrFileAsByteArray(String resourceNameOrFilePath) throws IOException
    {
        String resourcePath = ensureRootPath(resourceNameOrFilePath);
        URL resource = ResourceUtils.class.getResource(resourcePath);
        if (resource != null)
        {
            return IOUtils.toByteArray(resource);
        }
        Path path = Paths.get(resourceNameOrFilePath);
        File file = path.toFile();
        if (file.exists() && file.isFile())
        {
            return Files.readAllBytes(path);
        }
        throw new IllegalArgumentException(
                "Neither resource with name '" + resourcePath + "' nor file at path '" + resourceNameOrFilePath
                        + "' is found");
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

    /**
     * Creates an empty temporary file
     *
     * @param prefix the prefix string to be used in generating the file name
     * @param suffix the suffix string to be used in generating the file name
     * @return Path the path to the newly created file
     * @throws IOException IOException if an I/O exception of some sort has occurred
     */
    public static Path createTempFile(String prefix, String suffix) throws IOException
    {
        Path tempFilePath = Files.createTempFile(prefix, suffix);
        File tempFile = tempFilePath.toFile();
        tempFile.deleteOnExit();
        return tempFilePath;
    }

    /**
     * Creates an empty temporary file
     *
     * @param baseFileName the base file name used to generate the prefix and the suffix for the creating temporary file
     * @return Path the path to the newly created file
     * @throws IOException IOException if an I/O exception of some sort has occurred
     */
    public static Path createTempFile(String baseFileName) throws IOException
    {
        return createTempFile(FilenameUtils.getBaseName(baseFileName), "." + FilenameUtils.getExtension(baseFileName));
    }

    /**
     * Creates a temporary file with the specified content
     *
     * @param prefix the prefix string to be used in generating the file name
     * @param suffix the suffix string to be used in generating the file name
     * @param data   the content to write to the file
     * @return Path the path to the newly created file
     * @throws IOException IOException if an I/O exception of some sort has occurred
     */
    public static Path createTempFile(String prefix, String suffix, String data) throws IOException
    {
        Path tempFilePath = createTempFile(prefix, suffix);
        FileUtils.writeStringToFile(tempFilePath.toFile(), data, StandardCharsets.UTF_8);
        return tempFilePath;
    }

    private static String ensureRootPath(String resourceName)
    {
        return StringUtils.prependIfMissing(resourceName, "/");
    }

    private static <T> T loadResource(Class<?> clazz, String resourceName, ResourceReader<T> resourceReader)
    {
        URL resource = findResource(clazz, resourceName);
        try
        {
            return resourceReader.read(resource);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private interface ResourceReader<R>
    {
        R read(URL resource) throws IOException;
    }
}

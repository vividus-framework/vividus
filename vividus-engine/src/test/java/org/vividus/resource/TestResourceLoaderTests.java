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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class TestResourceLoaderTests
{
    private static final String STORY_LOCATION = "story/any/dir";
    private static final String ANY_STORY = "any.story";

    private static final String BRAND_KEY = "brand";
    private static final String BRAND_VALUE = "vividus";

    private static final String MARKET_KEY = "market";
    private static final String MARKET_VALUE = "ca";

    private static final String LOCALE_KEY = "locale";
    private static final String LOCALE_VALUE = "en";

    private static final String BRANDED_FULL_PATH = "file:/src/resources/story/bvt/vividus/bvt.story";

    @Mock private ResourcePatternResolver resourcePatternResolver;

    private TestResourceLoader testResourceLoader;

    private void initLoader(Map<String, String> props)
    {
        var propertyParser = mock(IPropertyParser.class);
        when(propertyParser.getPropertyValuesByPrefix("bdd.resource-loader.")).thenReturn(props);
        testResourceLoader = new TestResourceLoader(propertyParser, resourcePatternResolver);
    }

    private Resource[] mockResources(String... urls) throws IOException
    {
        Resource[] allResources = new Resource[urls.length];
        for (int i = 0; i < urls.length; i++)
        {
            Resource resource = mock(Resource.class);
            when(resource.getURL()).thenReturn(new URL(urls[i]));
            allResources[i] = resource;
        }
        return allResources;
    }

    private Resource[] mockGetAllResources(String prefix, String... urls) throws IOException
    {
        Resource[] allResources = mockResources(urls);
        when(resourcePatternResolver.getResources(startsWith(prefix))).thenReturn(allResources);
        return allResources;
    }

    @Test
    void testGetFileResources() throws IOException
    {
        var path = "file:///C:/Users/test/";
        var fileName = "temp.table";
        initLoader(Map.of(BRAND_KEY, BRAND_VALUE));
        Resource[] allResources = mockResources("file:///C:/Users/test/vividus/ca/temp.table");
        when(resourcePatternResolver.getResources(path + fileName)).thenReturn(allResources);

        var actualResources = testResourceLoader.getResources(path, fileName);
        assertArrayEquals(allResources, actualResources);
    }

    @Test
    void testGetResourcesWhenMoreThanOneFound() throws IOException
    {
        initLoader(Map.of(BRAND_KEY, BRAND_VALUE));
        var allResources = mockGetAllResources(CLASSPATH_ALL_URL_PREFIX,
                "file:/src/resources/story/uat/vividus/ca/en/sit.story",
                "file:/src/resources/story/uat/vividus/super.story",
                "file:/src/resources/story/uat/vividus/sit.story");

        Resource[] expectedResources = { allResources[1], allResources[2] };
        when(resourcePatternResolver.getResources(
                CLASSPATH_ALL_URL_PREFIX + "story/uat/vividus/diamond.story")).thenReturn(expectedResources);

        var actualResources = testResourceLoader.getResources("story/uat", "diamond.story");
        assertArrayEquals(expectedResources, actualResources);
    }

    @Test
    void testGetNoResourceFound() throws IOException
    {
        initLoader(Map.of());
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX))).thenReturn(new Resource[] {});
        var actualResource = testResourceLoader.getResources("story", "unknown.story");
        assertEquals(0, actualResource.length);
    }

    @Test
    void testGetResourceIOException() throws IOException
    {
        initLoader(Map.of());
        var cause = new IOException();
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX))).thenThrow(cause);
        var exception = assertThrows(ResourceLoadException.class,
                () -> testResourceLoader.getResources("story/sit", "any.table"));
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testGetResourcesThrowsExceptionAtGettingUri() throws IOException
    {
        initLoader(Map.of());
        IOException ioException = new IOException();
        Resource resource = when(mock(Resource.class).getURL()).thenThrow(ioException).getMock();
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX))).thenReturn(
                new Resource[] { resource });
        var exception = assertThrows(ResourceLoadException.class,
            () ->  testResourceLoader.getResources(STORY_LOCATION, ANY_STORY));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testGetResources() throws IOException
    {
        initLoader(Map.of(
                LOCALE_KEY, "",
                MARKET_KEY, MARKET_VALUE,
                BRAND_KEY, BRAND_VALUE
        ));
        mockGetAllResources(CLASSPATH_ALL_URL_PREFIX,
                "file:/src/resources/story/bvt/vividus/ca/en/bvt.story",
                "file:/src/resources/story/bvt/vividus/ca/bvt.story", BRANDED_FULL_PATH);
        testSingleResourceRetrieval("/vividus/ca");
    }

    @Test
    void testGetResourcesSimilarProps() throws IOException
    {
        Map<String, String> props = new HashMap<>();
        props.put(LOCALE_KEY, "cn");
        props.put(MARKET_KEY, null);
        props.put(BRAND_KEY, BRAND_VALUE);
        initLoader(props);
        mockGetAllResources(CLASSPATH_ALL_URL_PREFIX, BRANDED_FULL_PATH);
        testSingleResourceRetrieval("/vividus");
    }

    @Test
    void testGetResourcesIdenticalProps() throws IOException
    {
        initLoader(Map.of(
                LOCALE_KEY, LOCALE_VALUE,
                MARKET_KEY, LOCALE_VALUE,
                BRAND_KEY, BRAND_VALUE
        ));
        mockGetAllResources(CLASSPATH_ALL_URL_PREFIX, "file:/src/resources/story/bvt/vividus/en/en/bvt.story");
        testSingleResourceRetrieval("/vividus/en/en");
    }

    @Test
    void testGetResourcesWhenNonMatchingPathsFound() throws IOException
    {
        initLoader(Map.of(
                BRAND_KEY, BRAND_VALUE,
                LOCALE_KEY, LOCALE_VALUE
        ));
        mockGetAllResources(CLASSPATH_ALL_URL_PREFIX,
                "file:/src/resources/story/bvt/test/ca/en/sit.story",
                "file:/src/resources/story/bvt/test/super.story",
                "file:/src/resources/story/bvt/diamond/sit.story");
        testSingleResourceRetrieval("");
    }

    @Test
    void testGetResourcesWhenEmptyLoadConfig() throws IOException
    {
        initLoader(Map.of());
        mockGetAllResources(CLASSPATH_ALL_URL_PREFIX,
                "file:/src/resources/story/bvt/vividus/ca/en/sit.story",
                "file:/src/resources/story/bvt/vividus/super.story",
                "file:/src/resources/story/bvt/sit.story");
        testSingleResourceRetrieval("");
    }

    private void testSingleResourceRetrieval(String dynamicPathPart) throws IOException
    {
        var resourceLocation = "story/bvt";
        var resourceName = "bvt.story";
        Resource[] expectedResources = { mock(Resource.class) };
        when(resourcePatternResolver.getResources(
                CLASSPATH_ALL_URL_PREFIX + resourceLocation + dynamicPathPart + "/" + resourceName)).thenReturn(
                expectedResources);

        var actualResources = testResourceLoader.getResources(resourceLocation, resourceName);

        assertArrayEquals(expectedResources, actualResources);
    }
}

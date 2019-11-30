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

package org.vividus.bdd.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class BddResourceLoaderTests
{
    private static final String STORY_LOCATION = "story/any/dir";
    private static final String ANY_STORY = "any.story";

    private static final String BRAND = "vividus";
    private static final String MARKET = "ca";
    private static final String LOCALE = "en";

    private static final String SUCCESS = "success";

    private static final String[] DEFAULT_URLS = {
            "file:/src/resources/story/sit/vividus/ca/de/table/vividus.table",
            "file:/src/resources/story/sit/vividus/table/vividus.table",
            "file:/src/resources/story/sit/vividus/ca/table/vividus.table" };

    @Mock
    private ResourcePatternResolver resourcePatternResolver;

    @Mock
    private IResourceLoadConfiguration resourceLoadConfiguration;

    @Mock
    private Resource trueResource;

    @InjectMocks
    private BddResourceLoader bddResourceLoader;

    private Resource[] mockGetAllResources(String... urls) throws IOException
    {
        Resource[] allResources = new Resource[urls.length];
        for (int i = 0; i < urls.length; i++)
        {
            Resource resource = mock(Resource.class);
            when(resource.getURL()).thenReturn(new URL(urls[i]));
            allResources[i] = resource;
        }
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX)))
                .thenReturn(allResources);
        return allResources;
    }

    @Test
    void testGetResources() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/sit/vividus/ca/en/sit.story",
                "file:/src/resources/story/sit/vividus/ca/sit.story",
                "file:/src/resources/story/sit/vividus/sit.story");

        String resourceLocation = "story/sit";
        String resourcePattern = "sit.story";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/sit/vividus/ca/sit.story";
        mockResourceConfig(truePath, BRAND, MARKET);

        Resource[] actualResource = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertThat(actualResource[0].getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResourcesWhenMoreThanOneFound() throws IOException
    {
        Resource[] allResources = mockGetAllResources("file:/src/resources/story/uat/vividus/ca/en/sit.story",
                "file:/src/resources/story/uat/vividus/super.story", "file:/src/resources/story/uat/vividus/sit.story");

        String resourceLocation = "story/uat";
        String resourcePattern = "diamond.story";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/uat/vividus/diamond.story";
        List<String> params = List.of(BRAND);

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(params);
        when(resourcePatternResolver.getResources(truePath))
                .thenReturn(new Resource[] { allResources[1], allResources[2] });
        when(allResources[1].getFilename()).thenReturn(SUCCESS);
        when(allResources[2].getFilename()).thenReturn(SUCCESS);

        Resource[] actualResource = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertEquals(2, actualResource.length);
        assertThat(actualResource[0].getFilename(), equalTo(SUCCESS));
        assertThat(actualResource[1].getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResourcesWhenNonMatchingPathsFound() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/xx/test/ca/en/sit.story",
                "file:/src/resources/story/xx/test/super.story", "file:/src/resources/story/xx/diamond/sit.story");

        String resourceLocation = "story/xx";
        String resourcePattern = "extra.story";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/xx/extra.story";
        mockResourceConfig(truePath, BRAND, LOCALE);

        Resource[] actualResource = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertEquals(1, actualResource.length);
        assertThat(actualResource[0].getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResourcesWhenEmptyLoadConfig() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/xxx/vividus/ca/en/sit.story",
                "file:/src/resources/story/xxx/vividus/super.story", "file:/src/resources/story/xxx/sit.story");

        String resourceLocation = "story/xxx";
        String resourcePattern = "*.story";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/xxx/*.story";

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(List.of());
        when(resourcePatternResolver.getResources(truePath)).thenReturn(new Resource[] { trueResource });

        Resource[] actualResource = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertEquals(1, actualResource.length);
        assertThat(actualResource[0].getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResource() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/bvt/vividus/ca/en/vividus.table",
                "file:/src/resources/story/bvt/vividus/ca/vividus.table",
                "file:/src/resources/story/bvt/vividus/vividus.table");
        String rawPath = "story/bvt/vividus.table";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/bvt/vividus/ca/en/vividus.table";
        List<String> params = List.of(LOCALE, MARKET, BRAND);

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(params);
        when(resourcePatternResolver.getResources(truePath)).thenReturn(new Resource[] { trueResource });

        Resource actualResource = bddResourceLoader.getResource(rawPath);
        assertThat(actualResource.getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResource2() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/sit/vividus/ca/de/super.table",
                "file:/src/resources/story/sit/vividus/super.table",
                "file:/src/resources/story/sit/vividus/ca/super.table");
        String rawPath = "story/sit/super.table";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/sit/vividus/ca/super.table";
        List<String> params = List.of(MARKET, BRAND, LOCALE);

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(params);
        when(resourcePatternResolver.getResources(truePath)).thenReturn(new Resource[] { trueResource });

        Resource actualResource = bddResourceLoader.getResource(rawPath);
        assertThat(actualResource.getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResourceWhenMoreThanOneFound() throws IOException
    {
        mockGetAllResources(DEFAULT_URLS);
        String rawPath = "story/sit/vividus.table";
        List<String> params = List.of(MARKET, BRAND, LOCALE);

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(params);
        assertThrows(ResourceLoadException.class, () -> bddResourceLoader.getResource(rawPath));
    }

    @Test
    void testGetResourceWhenNotFound() throws IOException
    {
        mockGetAllResources(DEFAULT_URLS);
        String rawPath = "story/sit/list.table";
        List<String> params = List.of(MARKET, BRAND, LOCALE);

        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(params);
        assertThrows(ResourceLoadException.class, () -> bddResourceLoader.getResource(rawPath));
    }

    @Test
    void testGetNoResourceFound() throws IOException
    {
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX)))
                .thenReturn(new Resource[] {});
        String resourceLocation = "story";
        String resourcePattern = "some story.story";

        Resource[] resources = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertThat(resources, arrayWithSize(0));
    }

    @Test
    void testGetResourceIOException() throws IOException
    {
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX)))
                .thenThrow(new IOException());
        assertThrows(ResourceLoadException.class, () -> bddResourceLoader.getResource("story/sit/unknown.table"));
    }

    @Test
    void testGetResourcesThrowsExceptionAtGettingUri() throws IOException
    {
        IOException ioException = new IOException();
        Resource resource = when(mock(Resource.class).getURL()).thenThrow(ioException).getMock();
        when(resourcePatternResolver.getResources(startsWith(CLASSPATH_ALL_URL_PREFIX)))
                .thenReturn(new Resource[] { resource });
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () ->  bddResourceLoader.getResources(STORY_LOCATION, ANY_STORY));
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void testGetResourcesSimilarProps() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/bvt/vividus/bvt.story");

        String resourceLocation = "story/bvt";
        String resourcePattern = "bvt.story";
        String truePath = CLASSPATH_ALL_URL_PREFIX + "story/bvt/vividus/bvt.story";
        mockResourceConfig(truePath, "cn", BRAND);

        Resource[] actualResource = bddResourceLoader.getResources(resourceLocation, resourcePattern);
        assertThat(actualResource[0].getFilename(), equalTo(SUCCESS));
    }

    @Test
    void testGetResourcesIdenticalProps() throws IOException
    {
        when(trueResource.getFilename()).thenReturn(SUCCESS);
        mockGetAllResources("file:/src/resources/story/ut/vividus/en/en/uat.story");
        mockResourceConfig(CLASSPATH_ALL_URL_PREFIX + "story/ut/vividus/en/en/uat.story", LOCALE, LOCALE,
                BRAND);
        assertThat(bddResourceLoader.getResources("story/ut", "uat.story")[0]
                .getFilename(), equalTo(SUCCESS));
    }

    private void mockResourceConfig(String truePath, String... props) throws IOException
    {
        when(resourceLoadConfiguration.getResourceLoadParametersValues()).thenReturn(List.of(props));
        when(resourcePatternResolver.getResources(truePath)).thenReturn(new Resource[] { trueResource });
    }
}

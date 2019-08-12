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

package org.vividus.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ResourceUtilsTests
{
    private static final String RESOURCE_NAME = "test-resource.txt";
    private static final String RESOURCE_CONTENT = "text line" + System.lineSeparator();
    private static final String ROOT_RESOURCE_CONTENT = "root" + System.lineSeparator();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testResourceLoadingFromRootAsStringIsSuccesful()
    {
        assertEquals(ROOT_RESOURCE_CONTENT, StringUtils
                .normalizeLineFeeds(ResourceUtils.loadResource(RESOURCE_NAME)));
    }

    @Test
    public void testResourceLoadingFromRootAsByteArrayIsSuccesful()
    {
        assertArrayEquals(ROOT_RESOURCE_CONTENT.getBytes(StandardCharsets.UTF_8),
                normalizeBytes(ResourceUtils.loadResourceAsByteArray(RESOURCE_NAME)));
    }

    @Test
    public void testResourceLoadingAsStringIsSuccesful()
    {
        assertEquals(RESOURCE_CONTENT, StringUtils
                .normalizeLineFeeds(ResourceUtils.loadResource(ResourceUtils.class, RESOURCE_NAME)));
    }

    @Test
    public void testResourceLoadingAsByteArrayIsSuccesful()
    {
        assertArrayEquals(RESOURCE_CONTENT.getBytes(StandardCharsets.UTF_8),
                normalizeBytes(ResourceUtils.loadResourceAsByteArray(ResourceUtils.class, RESOURCE_NAME)));
    }

    private byte[] normalizeBytes(byte[] bytes)
    {
        return StringUtils.normalizeLineFeeds(new String(bytes, StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
    }

    @Test
    public void testFileLoadingIsSuccesful()
    {
        File actual = ResourceUtils.loadFile(ResourceUtils.class, RESOURCE_NAME);
        Assertions.assertTrue(actual.exists());
    }

    @Test
    @PrepareForTest({ URL.class, ResourceUtils.class })
    public void testFileLoadingURISyntaxException() throws URISyntaxException, IOException
    {
        PowerMockito.spy(ResourceUtils.class);
        URL mockedUrl = PowerMockito.mock(URL.class);

        File file = folder.newFile(RESOURCE_NAME);

        PowerMockito.when(ResourceUtils.findResource(ResourceUtilsTests.class, RESOURCE_NAME)).thenReturn(mockedUrl);
        PowerMockito.when(mockedUrl.toURI()).thenThrow(new URISyntaxException("bla", "bla-bla"));
        PowerMockito.when(mockedUrl.getFile()).thenReturn(file.getPath());
        File actual = ResourceUtils.loadFile(ResourceUtilsTests.class, RESOURCE_NAME);
        assertThat(actual.getAbsolutePath(), equalTo(file.getAbsolutePath()));
    }

    @Test
    public void testUnexistentResourceLoadingIsFailed()
    {
        String resourceName = "unexistent-test-resource.txt";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ResourceUtils.findResource(ResourceUtils.class, resourceName));
        assertEquals(
                "Resource with name " + resourceName + " for class org.vividus.util.ResourceUtils is not found",
                exception.getMessage());
    }

    @Test
    @PrepareForTest(IOUtils.class)
    public void testResourceLoadingIsFailedWithIoException() throws IOException
    {
        PowerMockito.mockStatic(IOUtils.class);
        IOException ioException = new IOException("mocked IOException");
        PowerMockito.when(IOUtils.toString(any(URL.class), eq(StandardCharsets.UTF_8))).thenThrow(ioException);
        UncheckedIOException exception = assertThrows(UncheckedIOException.class,
            () -> ResourceUtils.loadResource(ResourceUtils.class, RESOURCE_NAME));
        assertEquals(ioException, exception.getCause());
    }
}

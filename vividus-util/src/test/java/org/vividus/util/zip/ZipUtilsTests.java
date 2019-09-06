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

package org.vividus.util.zip;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.util.ResourceUtils;

@RunWith(PowerMockRunner.class)
public class ZipUtilsTests
{
    private static final String ZIP = "archive.zip";

    @Test
    public void testReadArchiveEntriesFromBytes()
    {
        ZipUtils.readZipEntriesFromBytes(ResourceUtils.loadResourceAsByteArray(getClass(), ZIP)).forEach(
            (name, content) -> Assertions.assertAll(
                () -> Assertions.assertNotNull(content),
                () -> assertThat(name, containsString("archive"))
            )
        );
    }

    @Test
    public void testReadArchiveEntryNamesFromBytes() throws IOException
    {
        File file = FileUtils.toFile(ResourceUtils.findResource(getClass(), ZIP));
        Set<String> names = ZipUtils.readZipEntryNamesFromBytes(FileUtils.readFileToByteArray(file));
        assertThat(names, is(equalTo(Set.of("archive/text.txt", "archive/"))));
    }

    @Test
    public void testReadArchiveEntriesFromBytesFilter() throws IOException
    {
        File file = FileUtils.toFile(ResourceUtils.findResource(getClass(), ZIP));
        Map<String, byte[]> zipEntries = ZipUtils.readZipEntriesFromBytes(FileUtils.readFileToByteArray(file),
            name -> false);
        assertThat(zipEntries, anEmptyMap());
    }

    @Test
    @PrepareForTest({ ZipUtils.class, ZipInputStream.class, ByteArrayInputStream.class })
    public void testReadArchiveEntriesFromBytesException() throws Exception
    {
        PowerMockito.mockStatic(ZipInputStream.class);
        PowerMockito.mockStatic(ByteArrayInputStream.class);
        byte[] file = {};
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file);
        PowerMockito.whenNew(ByteArrayInputStream.class).withArguments(file).thenReturn(byteArrayInputStream);
        PowerMockito.whenNew(ZipInputStream.class).withArguments(byteArrayInputStream).thenThrow(IOException.class);

        assertThrows(UncheckedIOException.class, () -> ZipUtils.readZipEntriesFromBytes(file));
    }
}

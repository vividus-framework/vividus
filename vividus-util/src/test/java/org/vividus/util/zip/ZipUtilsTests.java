/*
 * Copyright 2019-2024 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.util.ResourceUtils;

class ZipUtilsTests
{
    private static final String ZIP = "archive.zip";

    @Test
    void testReadArchiveEntriesFromBytes() throws IOException
    {
        ZipUtils.readZipEntriesFromBytes(ResourceUtils.loadResourceAsByteArray(getClass(), ZIP)).forEach(
            (name, content) -> Assertions.assertAll(
                () -> Assertions.assertNotNull(content),
                () -> assertThat(name, containsString("archive"))
            )
        );
    }

    @Test
    void testReadArchiveEntryNamesFromBytes() throws IOException
    {
        File file = FileUtils.toFile(ResourceUtils.findResource(getClass(), ZIP));
        Set<String> names = ZipUtils.readZipEntryNamesFromBytes(FileUtils.readFileToByteArray(file));
        assertThat(names, is(equalTo(Set.of("archive/text.txt", "archive/"))));
    }

    @Test
    void testReadArchiveEntriesFromBytesFilter() throws IOException
    {
        File file = FileUtils.toFile(ResourceUtils.findResource(getClass(), ZIP));
        Map<String, byte[]> zipEntries = ZipUtils.readZipEntriesFromBytes(FileUtils.readFileToByteArray(file),
            name -> false);
        assertThat(zipEntries, anEmptyMap());
    }
}

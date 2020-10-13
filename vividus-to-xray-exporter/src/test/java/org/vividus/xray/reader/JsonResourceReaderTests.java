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

package org.vividus.xray.reader;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vividus.util.ResourceUtils;
import org.vividus.xray.reader.JsonResourceReader.FileEntry;

@ExtendWith(TestLoggerFactoryExtension.class)
class JsonResourceReaderTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(JsonResourceReader.class);

    @Test
    void shouldReadJson() throws URISyntaxException, IOException
    {
        Path path = Paths.get(ResourceUtils.findResource(getClass(), "data").toURI());
        List<FileEntry> fileEntries = JsonResourceReader.readFrom(path);
        assertThat(fileEntries, hasSize(1));
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Reading JSON files from filesystem by path {}", path),
            info("Content of file '{}' is not JSON", path.resolve("image.png").toString())
        )));
        FileEntry fileEntry = fileEntries.get(0);
        assertEquals(path.resolve("file.json").toString(), fileEntry.getPath());
        assertEquals("{}", fileEntry.getContent().strip());
    }
}

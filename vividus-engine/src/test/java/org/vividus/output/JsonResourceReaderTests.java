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

package org.vividus.output;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vividus.util.ResourceUtils;

@ExtendWith(TestLoggerFactoryExtension.class)
class JsonResourceReaderTests
{
    private static final String IGNORED_FILE_MESSAGE = "The file '{}' is ignored";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(JsonResourceReader.class);

    @Test
    void shouldReadJson() throws URISyntaxException, IOException
    {
        var path = Paths.get(ResourceUtils.findResource(getClass(), "data").toURI());
        var beforeStories = "BeforeStories.json";
        var afterStories = "AfterStories.json";
        var fileEntries = new JsonResourceReader(List.of(beforeStories, afterStories)).readFrom(path);
        assertThat(fileEntries, hasSize(1));
        var loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasSize(4));
        assertThat(loggingEvents.get(0), equalTo(info("Reading JSON files from filesystem by path {}", path)));
        assertThat(loggingEvents.subList(1, 4), containsInAnyOrder(
                info(IGNORED_FILE_MESSAGE, path.resolve(beforeStories).toString()),
                info(IGNORED_FILE_MESSAGE, path.resolve(afterStories).toString()),
                info("Content of file '{}' is not JSON", path.resolve("image.png").toString())
        ));
        var fileEntry = fileEntries.get(0);
        assertEquals(path.resolve("file.json").toString(), fileEntry.getPath());
        assertEquals("{}", fileEntry.getContent().strip());
    }
}

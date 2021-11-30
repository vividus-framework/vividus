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

package org.vividus.output;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.junit.jupiter.api.io.TempDir;
import org.vividus.model.jbehave.Story;
import org.vividus.util.ResourceUtils;

@ExtendWith(TestLoggerFactoryExtension.class)
class OutputReaderTests
{
    private static final String PARSING_MESSAGE = "Parsing {}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(OutputReader.class);

    @Test
    void shouldReadStoriesFromJsons() throws URISyntaxException, IOException
    {
        Path path = Paths.get(ResourceUtils.findResource(getClass(), "data").toURI());
        String jsonPath = path.resolve("file.json").toString();
        List<Story> stories = OutputReader.readStoriesFromJsons(path);
        assertThat(stories, hasSize(1));
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("JSON files: {}", jsonPath),
            info(PARSING_MESSAGE, jsonPath)
        )));
    }

    @Test
    void shouldThrowExceptionIfJsonDirectoryIsEmpty(@TempDir Path directory)
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> OutputReader.readStoriesFromJsons(directory));
        String expected = "The directory '" + directory.toString() + "' does not contain needed JSON files";
        assertEquals(expected, thrown.getMessage());
        assertThat(logger.getLoggingEvents(), empty());
    }
}

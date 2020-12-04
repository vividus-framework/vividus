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

package org.vividus.zephyr.parser;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.model.TestCase;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
public class TestCaseParserTests
{
    private static final String JSON_FILES_STRING = "Json files: {}";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(TestCaseParser.class);

    @Mock
    private ZephyrExporterProperties zephyrExporterProperties;

    @InjectMocks
    private TestCaseParser testCaseParser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testConfigurationWithEmptyResultsToExport(@TempDir Path tempDir) throws IOException
    {
        Path emptyDirectoryPath = tempDir.resolve("empty");
        Files.createDirectory(emptyDirectoryPath);
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(emptyDirectoryPath);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> testCaseParser.createTestCases(objectMapper));
        assertEquals("Folder '" + emptyDirectoryPath + "' does not contain needed json files",
                exception.getMessage());
        assertThat(testLogger.getLoggingEvents(), empty());
    }

    @Test
    void testTestCaseWithEmptyKey() throws URISyntaxException
    {
        configureObjectMapper();
        Path sourceDirectory = Paths.get(getClass().getResource("/emptyKey/test-cases").toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> testCaseParser.createTestCases(objectMapper));
        List<File> files = List.of(sourceDirectory.resolve("emptyKey.json").toFile());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(JSON_FILES_STRING, files))));
        assertEquals("There are not any test cases for exporting", exception.getMessage());
    }

    @Test
    void testResultsProblemWithReading() throws URISyntaxException
    {
        Path sourceDirectory = Paths.get(getClass().getResource("/exception/test-cases").toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> testCaseParser.createTestCases(objectMapper));
        assertThat(exception.getMessage(), matchesRegex("Problem with reading values from json file .*"));
        List<File> files = List.of(sourceDirectory.resolve("readingException.json").toFile());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(JSON_FILES_STRING, files))));
    }

    @Test
    void testCreateTestCases() throws URISyntaxException, IOException
    {
        configureObjectMapper();
        Path sourceDirectory = Paths.get(getClass().getResource("/test-cases").toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        List<TestCase> testCases = testCaseParser.createTestCases(objectMapper);
        assertEquals(testCases.size(), 2);
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(1).getMessage(), is("Test cases: {}"));
        assertThat(testLogger.getLoggingEvents().get(1).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(2).getMessage(), is("Test cases for exporting to JIRA: {}"));
        assertThat(testLogger.getLoggingEvents().get(2).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().size(), equalTo(3));
    }

    private void configureObjectMapper()
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        objectMapper.registerModule(new SimpleModule().addDeserializer(TestCase.class, new TestCaseDeserializer()));
    }
}

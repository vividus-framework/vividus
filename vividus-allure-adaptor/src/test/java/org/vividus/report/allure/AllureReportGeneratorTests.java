/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.report.allure;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.report.allure.notification.NotificationsSender;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;
import org.vividus.util.property.PropertyMapper;

import io.qameta.allure.Constants;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.core.ReportWebPlugin;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.summary.SummaryPlugin;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AllureReportGeneratorTests
{
    private static final String ALLURE_CUSTOMIZATION_PATH = "/allure-customization/";
    private static final String ALLURE_CUSTOMIZATION_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + ALLURE_CUSTOMIZATION_PATH + "**";
    private static final String ALLURE_RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
    private static final String RESULTS = "results";
    private static final String REPORT = "report";

    private static final String EXECUTOR_JSON = "executor.json";
    private static final String ALLURE_EXECUTOR_PROPERTY_PREFIX = "allure.executor.";
    private static final String INDEX_HTML = "index.html";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AllureReportGenerator.class);

    private Path tempDir;
    private Path resultsDirectory;

    @Mock private PropertyMapper propertyMapper;
    @Mock private ResourcePatternResolver resourcePatternResolver;
    @Mock private AllurePluginsProvider allurePluginsProvider;
    @Mock private NotificationsSender notificationsSender;

    private AllureReportGenerator allureReportGenerator;

    @BeforeEach
    void beforeEach(@TempDir Path tempDir) throws IOException
    {
        this.tempDir = tempDir;
        resultsDirectory = tempDir.resolve("allure-results");
        Files.createDirectories(resultsDirectory);
        System.setProperty(ALLURE_RESULTS_DIRECTORY_PROPERTY, resultsDirectory.toAbsolutePath().toString());
        allureReportGenerator = new AllureReportGenerator(propertyMapper, resourcePatternResolver,
                allurePluginsProvider, notificationsSender);
    }

    @AfterEach
    void afterEach()
    {
        System.clearProperty(ALLURE_RESULTS_DIRECTORY_PROPERTY);
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(Map::clear);
    }

    @Test
    void testStart() throws IOException
    {
        File testFile = tempDir.resolve("any.json").toFile();
        FileUtils.writeLines(testFile, List.of("{}"));
        File reportDirectory = tempDir.toFile();
        allureReportGenerator.setReportDirectory(reportDirectory);
        allureReportGenerator.start();
        assertFalse(FileUtils.directoryContains(reportDirectory, testFile));
    }

    @Test
    void testStartNoReportDirectory()
    {
        File reportDirectory = mock(File.class);
        allureReportGenerator.setReportDirectory(reportDirectory);
        allureReportGenerator.start();
        verify(reportDirectory, never()).listFiles();
    }

    @Test
    void testStartException()
    {
        File reportDirectory = mock(File.class);
        allureReportGenerator.setReportDirectory(reportDirectory);
        when(reportDirectory.exists()).thenReturn(true);
        try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class))
        {
            IOException ioException = new IOException();
            fileUtils.when(() -> FileUtils.cleanDirectory(reportDirectory)).thenThrow(ioException);
            Exception thrown = assertThrows(IllegalStateException.class, allureReportGenerator::start);
            assertEquals(ioException, thrown.getCause());
        }
    }

    @Test
    void testEndNotStarted()
    {
        try (MockedStatic<Files> files = mockStatic(Files.class))
        {
            allureReportGenerator.end();
            files.verifyNoInteractions();
        }
    }

    @Test
    void testEndWhenResultsDirectoryExists() throws IOException
    {
        File reportDirectory = tempDir.toFile();
        ExecutorInfo executorInfo = createExecutorInfo();
        when(propertyMapper.readValue(ALLURE_EXECUTOR_PROPERTY_PREFIX, ExecutorInfo.class)).thenReturn(
                Optional.of(executorInfo));
        testEnd(reportDirectory);
        assertThat(logger.getLoggingEvents(), is(List.of(
            buildCleanUpDirectoryLogEvent(RESULTS, resultsDirectory.toFile()),
            buildDirectoryCleanedLogEvent(RESULTS, resultsDirectory.toFile()),
            buildCleanUpDirectoryLogEvent(REPORT, reportDirectory),
            buildDirectoryCleanedLogEvent(REPORT, reportDirectory),
            buildReportGeneratedLogEvent(tempDir)
        )));
        assertExecutorJson(executorInfo);
    }

    @Test
    void testEndWhenResultsDirectoryDoesNotExist() throws IOException
    {
        File reportDirectory = tempDir.toFile();
        resultsDirectory = tempDir.resolve("allure-results-to-be-created");
        System.setProperty(ALLURE_RESULTS_DIRECTORY_PROPERTY, resultsDirectory.toAbsolutePath().toString());
        allureReportGenerator = new AllureReportGenerator(propertyMapper, resourcePatternResolver,
                allurePluginsProvider, notificationsSender);
        when(propertyMapper.readValue(ALLURE_EXECUTOR_PROPERTY_PREFIX, ExecutorInfo.class)).thenReturn(
                Optional.empty());
        testEnd(reportDirectory);
        assertThat(logger.getLoggingEvents(), is(List.of(
            buildCleanUpDirectoryLogEvent(RESULTS, resultsDirectory.toFile()),
            debug("Allure {} directory {} doesn't exist", RESULTS, resultsDirectory.toFile()),
            buildCleanUpDirectoryLogEvent(REPORT, reportDirectory),
            buildDirectoryCleanedLogEvent(REPORT, reportDirectory),
            buildReportGeneratedLogEvent(tempDir)
        )));
        assertFalse(resultsDirectory.resolve(EXECUTOR_JSON).toFile().exists());
        verify(notificationsSender).sendNotifications(reportDirectory);
    }

    private LoggingEvent buildCleanUpDirectoryLogEvent(String directoryDescription, File directory)
    {
        return debug("Cleaning up allure {} directory {}", directoryDescription, directory);
    }

    private LoggingEvent buildDirectoryCleanedLogEvent(String directoryDescription, File directory)
    {
        return debug("Allure {} directory {} is successfully cleaned", directoryDescription, directory);
    }

    private LoggingEvent buildReportGeneratedLogEvent(Path reportDirectory)
    {
        String htmlReportPath = reportDirectory.resolve(INDEX_HTML).toFile().getAbsolutePath();
        return info("Allure report is successfully generated at {}", htmlReportPath);
    }

    @SuppressWarnings("unchecked")
    private void testEnd(File reportDirectory) throws IOException
    {
        Path historyDirectory = tempDir.resolve("history");
        List<Plugin> plugins = List.of();
        Files.createDirectories(historyDirectory);
        allureReportGenerator.setHistoryDirectory(historyDirectory.toFile());
        allureReportGenerator.setReportDirectory(reportDirectory);
        try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class);
                MockedConstruction<ReportGenerator> reportGenerator = mockConstruction(ReportGenerator.class,
                        (mock, context) -> {
                            doAnswer(a -> {
                                resolveTrendsDir(reportDirectory);
                                return a;
                            }).when(mock).generate(any(Path.class), any(List.class));

                            assertEquals(1, context.getCount());
                            List<?> arguments = context.arguments();
                            assertEquals(1, arguments.size());
                            Configuration config = (Configuration) arguments.get(0);

                            assertEquals(plugins, config.getPlugins());
                            assertEquals(26, config.getAggregators().size());

                            Path reportDirectoryPath = reportDirectory.toPath();

                            var summaryPlugin = config.getAggregators().stream().filter(SummaryPlugin.class::isInstance)
                                    .reduce((first, second) -> second);
                            assertTrue(summaryPlugin.isPresent());
                            summaryPlugin.get().aggregate(config, List.of(), reportDirectoryPath);
                            assertSummaryJson(reportDirectoryPath);

                            var reportWebPlugin = config.getAggregators().stream().filter(
                                    ReportWebPlugin.class::isInstance).reduce((first, second) -> second);
                            assertTrue(reportWebPlugin.isPresent());
                            reportWebPlugin.get().aggregate(config, List.of(), reportDirectoryPath);
                            assertIndexHtml(reportDirectoryPath);
                        }))
        {
            String text = "text";
            fileUtils.when(() -> FileUtils.readFileToString(any(File.class), eq(StandardCharsets.UTF_8))).thenReturn(
                    text);
            allureReportGenerator.setReportDirectory(reportDirectory);
            Resource resource = mockResource("/allure-customization/some_file.txt");
            Resource folder = mockResource("/allure-customization/folder/");
            Resource[] resources = { resource, folder };
            when(resourcePatternResolver.getResources(ALLURE_CUSTOMIZATION_PATTERN)).thenReturn(resources);
            prepareEnvironmentConfiguration();

            when(allurePluginsProvider.getPlugins()).thenReturn(plugins);
            allureReportGenerator.start();
            allureReportGenerator.end();
            fileUtils.verify(() -> FileUtils.copyInputStreamToFile(eq(folder.getInputStream()), any(File.class)),
                    never());
            fileUtils.verify(() -> FileUtils.copyInputStreamToFile(eq(resource.getInputStream()), any(File.class)));
            fileUtils.verify(() -> FileUtils.writeStringToFile(any(File.class), eq(text), eq(StandardCharsets.UTF_8)),
                    times(2));
            fileUtils.verify(() -> FileUtils
                    .copyDirectory(argThat(arg -> arg.getAbsolutePath().equals(resolveTrendsDir(reportDirectory))),
                            eq(historyDirectory.toFile())));
            verify(reportGenerator.constructed().get(0)).generate(any(Path.class), any(List.class));
            assertEnvironmentProperties();
            assertCategoriesJson();
            verify(notificationsSender).sendNotifications(reportDirectory);
        }
    }

    private void prepareEnvironmentConfiguration()
    {
        Map<PropertyCategory, Map<String, String>> environmentConfiguration =
                EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION;
        environmentConfiguration.get(PropertyCategory.CONFIGURATION).put("Suite", "allure-test");
        environmentConfiguration.get(PropertyCategory.PROFILE).put("Operating System", "Mac OS X");
        environmentConfiguration.get(PropertyCategory.SUITE).put("Global Meta Filters", "groovy: !skip");
        environmentConfiguration.get(PropertyCategory.ENVIRONMENT).put("Main Application Page", "https://vividus.dev/");
    }

    private ExecutorInfo createExecutorInfo()
    {
        ExecutorInfo executorInfo = new ExecutorInfo();
        executorInfo.setName("Jenkins");
        executorInfo.setType("jenkins");
        executorInfo.setUrl("https://my-jenkins.url");
        executorInfo.setBuildOrder(77L);
        executorInfo.setBuildName("test-run#77");
        executorInfo.setBuildUrl("https://my-jenkins.url/test-run#77");
        executorInfo.setReportName("Test Run Allure Report");
        executorInfo.setReportUrl("https://my-jenkins.url/test-run#77/AllureReport");
        return executorInfo;
    }

    private void assertIndexHtml(Path reportDirectory) throws IOException
    {
        assertThat(Files.readString(reportDirectory.resolve(INDEX_HTML)), not(containsString("googletagmanager")));
    }

    private void assertSummaryJson(Path reportDirectory) throws IOException
    {
        assertFile(reportDirectory, "widgets/summary.json",
                "{\"reportName\":\"Test Report\","
                    + "\"testRuns\":[],"
                    + "\"statistic\":{\"failed\":0,\"broken\":0,\"skipped\":0,\"passed\":0,\"unknown\":0,\"total\":0},"
                    + "\"time\":{}"
                    + "}"
        );
    }

    private void assertEnvironmentProperties() throws IOException
    {
        assertResultFile("environment.properties",
                      "Suite=allure-test\n"
                    + "Operating\\ System=Mac OS X\n"
                    + "Global\\ Meta\\ Filters=groovy: !skip\n"
                    + "Main\\ Application\\ Page=https://vividus.dev/\n"
        );
    }

    private void assertExecutorJson(ExecutorInfo executorInfo) throws IOException
    {
        assertResultFile(EXECUTOR_JSON,
                  "{"
                + "\"name\":\"" + executorInfo.getName()
                + "\",\"type\":\"" + executorInfo.getType()
                + "\",\"url\":\"" + executorInfo.getUrl()
                + "\",\"buildOrder\":" + executorInfo.getBuildOrder()
                + ",\"buildName\":\"" + executorInfo.getBuildName()
                + "\",\"buildUrl\":\"" + executorInfo.getBuildUrl()
                + "\",\"reportName\":\"" + executorInfo.getReportName()
                + "\",\"reportUrl\":\"" + executorInfo.getReportUrl()
                + "\"}"
        );
    }

    private void assertCategoriesJson() throws IOException
    {
        assertResultFile("categories.json",
                  "["
                + "{\"name\":\"Test defects\","
                + "\"matchedStatuses\":[\"broken\"]},"
                + "{\"name\":\"Product defects\","
                + "\"matchedStatuses\":[\"failed\"]},"
                + "{\"name\":\"Known issues\","
                + "\"matchedStatuses\":[\"unknown\"]}"
                + "]"
        );
    }

    private void assertResultFile(String fileName, String expected) throws IOException
    {
        assertFile(resultsDirectory, fileName, expected);
    }

    private static void assertFile(Path directory, String fileName, String expected) throws IOException
    {
        assertEquals(expected, Files.readString(directory.resolve(fileName)).replace("\r", ""));
    }

    private Resource mockResource(String asString) throws IOException
    {
        Resource resource = mock(Resource.class);
        URL resourceURL = mock(URL.class);
        when(resource.getURL()).thenReturn(resourceURL);
        when(resourceURL.toString()).thenReturn(asString);
        InputStream stubInputStream = new ByteArrayInputStream("test data".getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(stubInputStream);
        return resource;
    }

    private String resolveTrendsDir(File dir)
    {
        try
        {
            File history = new File(dir, Constants.HISTORY_DIR);
            history.mkdir();
            File trends = new File(history, "trends");
            trends.createNewFile();
            return history.getAbsolutePath();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}

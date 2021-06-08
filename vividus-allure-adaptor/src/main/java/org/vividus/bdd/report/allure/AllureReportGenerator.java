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

package org.vividus.bdd.report.allure;

import static org.apache.commons.io.FileUtils.copyDirectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.function.FailableRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.bdd.report.allure.model.AllureCategory;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;
import org.vividus.util.property.IPropertyMapper;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.Constants;
import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.behaviors.BehaviorsPlugin;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.duration.DurationTrendPlugin;
import io.qameta.allure.entity.ExecutorInfo;
import io.qameta.allure.entity.Status;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.history.HistoryTrendPlugin;
import io.qameta.allure.plugin.DefaultPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.util.PropertiesUtils;

public class AllureReportGenerator implements IAllureReportGenerator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AllureReportGenerator.class);
    private static final String ALLURE_CUSTOMIZATION_PATH = "/allure-customization/";
    private static final String ALLURE_CUSTOMIZATION_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + ALLURE_CUSTOMIZATION_PATH + "**";
    private static final String STYLES_CSS = "styles.css";

    private File historyDirectory;
    private File reportDirectory;
    private final File resultsDirectory =
            new File((String) PropertiesUtils.loadAllureProperties().get("allure.results.directory"));

    private final IPropertyMapper propertyMapper;
    private final ResourcePatternResolver resourcePatternResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean started;

    public AllureReportGenerator(IPropertyMapper propertyMapper, ResourcePatternResolver resourcePatternResolver)
    {
        this.propertyMapper = propertyMapper;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public void start()
    {
        deleteResultsDirectory();
        deleteReportDirectory();
        started = true;
    }

    @Override
    public void end()
    {
        if (started)
        {
            generateReport();
        }
        started = false;
    }

    private void deleteReportDirectory()
    {
        deleteDirectory("report", reportDirectory);
    }

    private void deleteResultsDirectory()
    {
        deleteDirectory("results", resultsDirectory);
    }

    private void generateReport()
    {
        wrap(() ->
        {
            createDirectories(resultsDirectory, reportDirectory, historyDirectory);
            writeEnvironmentProperties();
            writeCategoriesInfo();
            writeExecutorInfo();
            copyDirectory(historyDirectory, resolveHistoryDir(resultsDirectory));
            generateData();
            customizeReport();
            copyDirectory(resolveHistoryDir(reportDirectory), historyDirectory);
        });
        LOGGER.atInfo()
                .addArgument(() -> new File(reportDirectory, "index.html").getAbsolutePath())
                .log("Allure report is successfully generated at {}");
    }

    private void writeCategoriesInfo() throws IOException
    {
        List<AllureCategory> categories = List.of(
                new AllureCategory("Test defects", List.of(Status.BROKEN)),
                new AllureCategory("Product defects", List.of(Status.FAILED)),
                new AllureCategory("Known issues", List.of(Status.UNKNOWN)));
        createJsonFileInResultsDirectory("categories.json", categories);
    }

    private void createJsonFileInResultsDirectory(String fileName, Object content) throws IOException
    {
        try (BufferedWriter writer = Files.newBufferedWriter(resultsDirectory.toPath().resolve(fileName)))
        {
            objectMapper.writeValue(writer, content);
        }
    }

    private void writeExecutorInfo() throws IOException
    {
        ExecutorInfo executorInfo = propertyMapper.readValue("allure.executor.", ExecutorInfo.class);
        createJsonFileInResultsDirectory("executor.json", executorInfo);
    }

    private void writeEnvironmentProperties() throws IOException
    {
        Map<String, String> testExecutionProperties = new LinkedHashMap<>();
        Map<PropertyCategory, Map<String, String>> environmentConfig = EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION;
        testExecutionProperties.putAll(environmentConfig.get(PropertyCategory.CONFIGURATION));
        testExecutionProperties.putAll(environmentConfig.get(PropertyCategory.PROFILE));
        testExecutionProperties.putAll(environmentConfig.get(PropertyCategory.SUITE));
        testExecutionProperties.putAll(environmentConfig.get(PropertyCategory.ENVIRONMENT));
        File targetFile = Paths.get(resultsDirectory.getPath(), "environment.properties").toFile();
        new JavaPropsMapper().writeValue(targetFile, testExecutionProperties);
    }

    private File resolveHistoryDir(File root)
    {
        return new File(root, Constants.HISTORY_DIR);
    }

    private void generateData() throws IOException
    {
        List<Extension> extensions = List.of(
                new SummaryPlugin(),
                new HistoryTrendPlugin(),
                new DurationTrendPlugin(),
                new ExecutorPlugin()
        );
        List<Plugin> plugins = List.of(
                new EmbeddedPlugin("behaviors", List.of("index.js"), new BehaviorsPlugin()),
                new EmbeddedPlugin("custom-logo", List.of(STYLES_CSS))
        );
        Configuration configuration = new ConfigurationBuilder()
                .useDefault()
                .fromExtensions(extensions)
                .fromPlugins(plugins)
                .build();
        new ReportGenerator(configuration).generate(reportDirectory.toPath(), List.of(resultsDirectory.toPath()));
    }

    private void customizeReport() throws IOException
    {
        for (Resource resource : resourcePatternResolver.getResources(ALLURE_CUSTOMIZATION_PATTERN))
        {
            String path = resource.getURL().toString();
            String relativePath = path
                    .substring(path.lastIndexOf(ALLURE_CUSTOMIZATION_PATH) + ALLURE_CUSTOMIZATION_PATH.length());
            if (!relativePath.endsWith("/"))
            {
                File destination = new File(reportDirectory, relativePath);
                if (destination.isFile() || !destination.exists())
                {
                    FileUtils.copyInputStreamToFile(resource.getInputStream(), destination);
                }
            }
        }

        patchAllureFiles();
    }

    private void patchAllureFiles() throws IOException
    {
        File javascriptFile = new File(reportDirectory, "app.js");
        File cssFile = new File(reportDirectory, STYLES_CSS);
        String javascriptString = FileUtils.readFileToString(javascriptFile, StandardCharsets.UTF_8);
        String cssString = FileUtils.readFileToString(cssFile, StandardCharsets.UTF_8);

        String brokenStatusColor = "#d35ebf";
        javascriptString = javascriptString
                // Replacing of gray colors with #d35ebf in CSS does not affect the color used to draw
                // <rect> HTML elements used to display trends
                .replace("#aaa", brokenStatusColor)
                .replace("\"unknown\":\"Unknown\"", "\"unknown\":\"Known\"")
                .replace("\"failed\",\"broken\",\"passed\",\"skipped\",\"unknown\"",
                        "\"failed\",\"unknown\",\"passed\",\"broken\",\"skipped\"");
        cssString = cssString
                .replace("#ffd050", brokenStatusColor)
                .replace("#d35ebe", "#ffd051")
                .replace("#fffae6", "#faebf8")
                .replace("#faebf7", "#fffae7")
                .replace("#ffeca0", "#ecb7e3")
                .replace("#ecb7e2", "#ffeca1");

        FileUtils.writeStringToFile(javascriptFile, javascriptString, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(cssFile, cssString, StandardCharsets.UTF_8);
    }

    private static void createDirectories(File... directories) throws IOException
    {
        for (File directory : directories)
        {
            Files.createDirectories(directory.toPath());
        }
    }

    private static void deleteDirectory(String directoryDescription, File directory)
    {
        LOGGER.debug("Cleaning up allure {} directory {}", directoryDescription, directory);
        if (directory.exists())
        {
            wrap(() -> FileUtils.cleanDirectory(directory));
            LOGGER.debug("Allure {} directory {} is successfully cleaned", directoryDescription, directory);
        }
        else
        {
            LOGGER.debug("Allure {} directory {} doesn't exist", directoryDescription, directory);
        }
    }

    private static void wrap(FailableRunnable<IOException> runnable)
    {
        try
        {
            runnable.run();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void setReportDirectory(File reportDirectory)
    {
        this.reportDirectory = reportDirectory;
    }

    public void setHistoryDirectory(File historyDirectory)
    {
        this.historyDirectory = historyDirectory;
    }

    private static class EmbeddedPlugin extends DefaultPlugin
    {
        EmbeddedPlugin(String id, List<String> jsFiles, Extension extension)
        {
            super(new PluginConfiguration().setId(id).setJsFiles(jsFiles), List.of(extension), null);
        }

        EmbeddedPlugin(String id, List<String> cssFiles)
        {
            super(new PluginConfiguration().setId(id).setCssFiles(cssFiles), List.of(), null);
        }

        @Override
        public void unpackReportStatic(Path outputDirectory)
        {
            // do nothing
        }
    }
}

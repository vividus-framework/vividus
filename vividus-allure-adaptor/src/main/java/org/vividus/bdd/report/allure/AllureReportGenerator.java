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

package org.vividus.bdd.report.allure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.util.PropertiesUtils;

public class AllureReportGenerator implements IAllureReportGenerator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AllureReportGenerator.class);
    private static final String ALLURE_CUSTOMIZATION_PATH = "/allure-customization/";
    private static final String ALLURE_CUSTOMIZATION_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + ALLURE_CUSTOMIZATION_PATH + "**";

    private File reportDirectory;
    private final File resultsDirectory =
            new File((String) PropertiesUtils.loadAllureProperties().get("allure.results.directory"));

    private final ResourcePatternResolver resourcePatternResolver;

    private boolean started;

    public AllureReportGenerator(ResourcePatternResolver resourcePatternResolver)
    {
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
        try
        {
            createDirectory(reportDirectory);
            generateData();
            customizeReport();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        LOGGER.info("Allure report is successfully generated at: {}", reportDirectory.getAbsolutePath());
    }

    private void generateData() throws IOException
    {
        Configuration configuration = new ConfigurationBuilder().useDefault().fromExtensions(List.of(
                new SummaryPlugin())).build();
        new ReportGenerator(configuration).generate(reportDirectory.toPath(), resultsDirectory.toPath());
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
        File cssFile = new File(reportDirectory, "styles.css");
        String javascriptString = FileUtils.readFileToString(javascriptFile, StandardCharsets.UTF_8);
        String cssString = FileUtils.readFileToString(cssFile, StandardCharsets.UTF_8);

        javascriptString = javascriptString
                .replace("unknown:\"Unknown\"", "unknown:\"Known\"")
                .replace("\"failed\",\"broken\",\"passed\",\"skipped\",\"unknown\"",
                        "\"passed\",\"unknown\",\"failed\",\"broken\",\"skipped\"");
        cssString = cssString
                .replace("#ffd050", "#d35ebf")
                .replace("#d35ebe", "#ffd051")
                .replace("#fffae6", "#faebf8")
                .replace("#faebf7", "#fffae7")
                .replace("#ffeca0", "#ecb7e3")
                .replace("#ecb7e2", "#ffeca1");

        FileUtils.writeStringToFile(javascriptFile, javascriptString, StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(cssFile, cssString, StandardCharsets.UTF_8);
    }

    private static void createDirectory(File directory) throws IOException
    {
        if (!directory.exists())
        {
            FileUtils.forceMkdir(directory);
        }
    }

    private static void deleteDirectory(String directoryDescription, File directory)
    {
        LOGGER.debug("Cleaning up allure {} directory {}", directoryDescription, directory);
        if (directory.exists())
        {
            try
            {
                FileUtils.cleanDirectory(directory);
            }
            catch (IOException e)
            {
                throw new IllegalStateException(e);
            }
            LOGGER.debug("Allure {} directory {} is successfully cleaned", directoryDescription, directory);
        }
        else
        {
            LOGGER.debug("Allure {} directory {} doesn't exist", directoryDescription, directory);
        }
    }

    public void setReportDirectory(File reportDirectory)
    {
        this.reportDirectory = reportDirectory;
    }
}

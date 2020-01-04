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

package org.vividus.bdd.report.allure.adapter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.bdd.report.allure.AllureReportGenerator;

import io.qameta.allure.ReportGenerator;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.xml.*")
public class AllureReportGeneratorTests
{
    private static final String ALLURE_CUSTOMIZATION_PATH = "/allure-customization/";
    private static final String ALLURE_CUSTOMIZATION_PATTERN = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
            + ALLURE_CUSTOMIZATION_PATH + "**";
    private static final String ALLURE_RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder(new File("./"));

    @Mock
    private ResourcePatternResolver resourcePatternResolver;

    private AllureReportGenerator allureReportGenerator;

    @Before
    public void before()
    {
        System.setProperty(ALLURE_RESULTS_DIRECTORY_PROPERTY, "output/allure-results");
        allureReportGenerator = new AllureReportGenerator(resourcePatternResolver);
    }

    @After
    public void after()
    {
        System.clearProperty(ALLURE_RESULTS_DIRECTORY_PROPERTY);
    }

    @Test
    public void testStart() throws IOException
    {
        File testFile = testFolder.newFile();
        File resultsDirectory = testFolder.getRoot();
        allureReportGenerator.setReportDirectory(resultsDirectory);
        allureReportGenerator.start();
        assertFalse(FileUtils.directoryContains(resultsDirectory, testFile));
    }

    @Test
    public void testStartNoReportDirectory()
    {
        File resultsDirectory = Mockito.mock(File.class);
        allureReportGenerator.setReportDirectory(resultsDirectory);
        allureReportGenerator.start();
        verify(resultsDirectory, never()).listFiles();
    }

    @Test
    public void testStartException()
    {
        File resultsDirectory = Mockito.mock(File.class);
        allureReportGenerator.setReportDirectory(resultsDirectory);
        when(resultsDirectory.exists()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> allureReportGenerator.start());
    }

    @Test
    @PrepareForTest({ReportGenerator.class, AllureReportGenerator.class, FileUtils.class})
    public void testEnd() throws Exception
    {
        File resultsDirectory = testFolder.getRoot();
        allureReportGenerator.setReportDirectory(resultsDirectory);
        ReportGenerator reportGenerator = PowerMockito.mock(ReportGenerator.class);
        PowerMockito.whenNew(ReportGenerator.class).withAnyArguments().thenReturn(reportGenerator);
        PowerMockito.doNothing().when(reportGenerator).generate(any(Path.class), any(Path[].class));
        PowerMockito.mockStatic(FileUtils.class);
        String text = "text";
        when(FileUtils.readFileToString(any(File.class), eq(StandardCharsets.UTF_8))).thenReturn(text);
        allureReportGenerator.setReportDirectory(testFolder.getRoot());
        Resource resource = Mockito.mock(Resource.class);
        Resource[] resources = {resource};
        when(resourcePatternResolver.getResources(ALLURE_CUSTOMIZATION_PATTERN)).thenReturn(resources);
        URL resourceURL = PowerMockito.mock(URL.class);
        when(resource.getURL()).thenReturn(resourceURL);
        when(resourceURL.toString()).thenReturn("/allure-customization/some_file.txt");
        InputStream stubInputStream = new ByteArrayInputStream("test data".getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(stubInputStream);
        allureReportGenerator.start();
        allureReportGenerator.end();
        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.copyInputStreamToFile(eq(resource.getInputStream()), any(File.class));
        FileUtils.writeStringToFile(any(File.class), eq(text), eq(StandardCharsets.UTF_8));
        verify(reportGenerator).generate(any(Path.class), any(Path.class));
    }
}

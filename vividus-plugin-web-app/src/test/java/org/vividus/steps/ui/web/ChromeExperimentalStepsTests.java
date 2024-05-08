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

package org.vividus.steps.ui.web;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.vividus.context.VariableContext;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ChromeExperimentalStepsTests
{
    private static final String WAITING_FOR_DOWNLOADING_LOG_MSG = "Waiting for the {} file to download";
    private static final String CHROME_DOWNLOADS_PAGE = "chrome://downloads";
    private static final String DEFAULT_FILENAME_REGEX = "fileNameRegex";
    private static final String DEFAULT_FILEPATH = "C:\\user\\fileNameRegex-123.pdf";
    private static final String DEFAULT_FILE_CONTENT_INNER = "MQ==";
    private static final String DEFAULT_FILE_CONTENT = "data:@file/octet-stream;base64," + DEFAULT_FILE_CONTENT_INNER;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);
    private static final Set<VariableScope> DEFAULT_VARIABLE_SCOPE_SET = Set.of(VariableScope.SCENARIO);
    private static final String DEFAULT_VARIABLE_NAME = "variable";
    private static final String CHROME_DOWNLOAD_SEARCH_FILE_JS = "chrome-download-search-file.js";
    private static final String CHROME_DOWNLOAD_CREATE_INPUT_JS = "chrome-download-create-input.js";
    private static final String CHROME_DOWNLOAD_GET_FILE_CONTENT_JS = "chrome-download-get-file-content.js";
    private static final String CHROME_DOWNLOAD_FILES_LIST_JS = "chrome-download-files-list.js";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ChromeExperimentalSteps.class);

    @Mock private WebJavascriptActions javascriptActions;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private ISoftAssert softAssert;
    @Mock private VariableContext variableContext;
    @Mock private IWaitActions waitActions;
    @Mock private PageSteps pageSteps;
    @Mock private WebElement createdInput;
    @Mock private WaitResult<Object> waitResult;
    @Mock private WindowSteps windowSteps;

    private final byte[] decodedContent = Base64.getDecoder().decode(DEFAULT_FILE_CONTENT_INNER);

    @InjectMocks private ChromeExperimentalSteps chromeExperimentalSteps;

    @BeforeEach
    void beforeEach()
    {
        chromeExperimentalSteps.setFileDownloadTimeout(DEFAULT_TIMEOUT);
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(true);
    }

    @Test
    void testDownloadFileSuccessfully()
    {
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class, CHROME_DOWNLOAD_SEARCH_FILE_JS,
                DEFAULT_FILENAME_REGEX)).thenReturn(DEFAULT_FILEPATH);

        when(waitActions.wait(eq(javascriptActions), eq(DEFAULT_TIMEOUT), Mockito.any(), eq(true)))
                .thenReturn(waitResult);
        when(waitResult.isWaitPassed()).thenReturn(true);

        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class,
                CHROME_DOWNLOAD_CREATE_INPUT_JS)).thenReturn(createdInput);

        when(javascriptActions.executeAsyncScriptFromResource(ChromeExperimentalSteps.class,
                CHROME_DOWNLOAD_GET_FILE_CONTENT_JS, createdInput)).thenReturn(DEFAULT_FILE_CONTENT);

        chromeExperimentalSteps.downloadFile(DEFAULT_FILENAME_REGEX, DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME);
        verify(pageSteps).openPageInNewTab(CHROME_DOWNLOADS_PAGE);
        verify(createdInput).sendKeys(DEFAULT_FILEPATH);
        verify(variableContext).putVariable(DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME, decodedContent);
        verify(windowSteps).closeCurrentTab();

        LoggingEvent waitingForDownloadLoggingEvent = info(WAITING_FOR_DOWNLOADING_LOG_MSG, "fileNameRegex-123.pdf");
        LoggingEvent downloadCompleteLoggingEvent = info("Download for the {} file is completed", DEFAULT_FILEPATH);
        MatcherAssert.assertThat(logger.getLoggingEvents(),
                is(List.of(waitingForDownloadLoggingEvent, downloadCompleteLoggingEvent)));
    }

    @Test
    void testDownloadFileWrongBrowser()
    {
        when(webDriverManager.isBrowserAnyOf(Browser.CHROME)).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class, () -> chromeExperimentalSteps.downloadFile(DEFAULT_FILENAME_REGEX,
                        DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME),
                "The step is supported only on Chrome browser.");
        verifyNoInteractions(pageSteps);
        verifyNoInteractions(variableContext);
        verifyNoInteractions(pageSteps);
        MatcherAssert.assertThat(logger.getLoggingEvents(), is(Collections.emptyList()));
    }

    @Test
    void testNoFilesFoundByRegex()
    {
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class, CHROME_DOWNLOAD_SEARCH_FILE_JS,
                DEFAULT_FILENAME_REGEX)).thenReturn(null);
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class,
                CHROME_DOWNLOAD_FILES_LIST_JS)).thenReturn(List.of("file1", "file2"));

        chromeExperimentalSteps.downloadFile(DEFAULT_FILENAME_REGEX, DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME);
        verify(softAssert).recordFailedAssertion("Unable to find any file matching regex [" + DEFAULT_FILENAME_REGEX
                + "] among files on the browser downloads page: file1, file2");
        verify(pageSteps).openPageInNewTab(CHROME_DOWNLOADS_PAGE);
        verifyNoInteractions(variableContext);
        verify(windowSteps).closeCurrentTab();

        MatcherAssert.assertThat(logger.getLoggingEvents(), is(Collections.emptyList()));
    }

    @Test
    void testNoFilesFoundByRegexEmptyFilesList()
    {
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class, CHROME_DOWNLOAD_SEARCH_FILE_JS,
                DEFAULT_FILENAME_REGEX)).thenReturn(null);
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class, CHROME_DOWNLOAD_FILES_LIST_JS))
                .thenReturn(Collections.emptyList());

        chromeExperimentalSteps.downloadFile(DEFAULT_FILENAME_REGEX, DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME);
        verify(softAssert).recordFailedAssertion("There are no files on the browser downloads page");
        verify(pageSteps).openPageInNewTab(CHROME_DOWNLOADS_PAGE);
        verifyNoInteractions(variableContext);
        verify(windowSteps).closeCurrentTab();

        MatcherAssert.assertThat(logger.getLoggingEvents(), is(Collections.emptyList()));
    }

    @Test
    void testDownloadTimeoutError()
    {
        when(javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class, CHROME_DOWNLOAD_SEARCH_FILE_JS,
                DEFAULT_FILENAME_REGEX)).thenReturn("C:\\user\\fileNameRegex-124.pdf");

        when(waitActions.wait(eq(javascriptActions), eq(DEFAULT_TIMEOUT), Mockito.any(), eq(true)))
                .thenReturn(waitResult);
        when(waitResult.isWaitPassed()).thenReturn(false);

        chromeExperimentalSteps.downloadFile(DEFAULT_FILENAME_REGEX, DEFAULT_VARIABLE_SCOPE_SET, DEFAULT_VARIABLE_NAME);
        verify(pageSteps).openPageInNewTab(CHROME_DOWNLOADS_PAGE);
        verifyNoInteractions(variableContext);
        verify(windowSteps).closeCurrentTab();

        LoggingEvent waitingForDownloadLoggingEvent = info(WAITING_FOR_DOWNLOADING_LOG_MSG, "fileNameRegex-124.pdf");
        MatcherAssert.assertThat(logger.getLoggingEvents(), is(List.of(waitingForDownloadLoggingEvent)));
    }
}

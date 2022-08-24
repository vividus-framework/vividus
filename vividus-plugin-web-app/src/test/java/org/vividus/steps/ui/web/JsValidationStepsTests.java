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

package org.vividus.steps.ui.web;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.logging.BrowserLogLevel;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.WaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class JsValidationStepsTests
{
    private static final String URL = "url";
    private static final String ERROR_MESSAGE = "error message";
    private static final Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(ERROR_MESSAGE);
    private static final String EXTENSION = "extension ";
    private static final Pattern EXTENSION_PATTERN = Pattern.compile(EXTENSION);
    private static final String ERRORS_ASSERTION_DESCRIPTION = "Current page contains no JavaScript errors";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private WaitActions waitActions;

    @Mock
    private VariableContext variableContext;

    @InjectMocks
    private JsValidationSteps jsValidationSteps;

    @Test
    void testCheckThereAreLogEntriesOnOpenedPageFilteredByRegExp()
    {
        Map<String, Collection<LogEntry>> expectedResults = testCheckJsErrors(ERROR_MESSAGE, () -> jsValidationSteps
                .checkThereAreLogEntriesOnOpenedPageFilteredByRegExp(singletonList(BrowserLogLevel.ERRORS),
                        ERROR_MESSAGE_PATTERN));
        InOrder inOrder = inOrder(attachmentPublisher, softAssert);
        verifyAttachmentPublisher(expectedResults, inOrder);
        inOrder.verify(softAssert).assertFalse(String.format("Current page contains JavaScript %s by regex '%s'",
                BrowserLogLevel.ERRORS.toString().toLowerCase(), ERROR_MESSAGE), false);
    }

    @Test
    void testCheckJsErrorsOnPage()
    {
        Map<String, Collection<LogEntry>> expectedResults = testCheckJsErrors(ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(singletonList(BrowserLogLevel.ERRORS)));
        verifyTestActions(expectedResults, ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsOnPageByRegExpNoMatch()
    {
        testCheckJsErrors(ERROR_MESSAGE, () -> jsValidationSteps
                .checkJsLogEntriesOnOpenedPageFilteredByRegExp(singletonList(BrowserLogLevel.ERRORS),
                    EXTENSION_PATTERN));
        verifyTestActions(Map.of(URL, Collections.emptySet()), ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsAndWarningsOnPage()
    {
        List<BrowserLogLevel> logLevels = Arrays.asList(BrowserLogLevel.ERRORS, BrowserLogLevel.WARNINGS);
        Map<String, Collection<LogEntry>> expectedResults = testCheckJsErrors(ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(logLevels));
        verifyTestActions(expectedResults, "Current page contains no JavaScript errors, warnings");
    }

    @Test
    void testCheckJsErrorsContainsBrowserExtensionErrors()
    {
        jsValidationSteps.setIncludeBrowserExtensionLogEntries(true);
        Map<String, Collection<LogEntry>> expectedResults = testCheckJsErrors(EXTENSION + ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(singletonList(BrowserLogLevel.ERRORS)));
        verifyTestActions(expectedResults, ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsExcludesBrowserExtensionErrors()
    {
        jsValidationSteps.setIncludeBrowserExtensionLogEntries(false);
        testCheckJsErrors(EXTENSION + ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(singletonList(BrowserLogLevel.ERRORS)));
        verifyTestActions(Map.of(URL, Collections.emptySet()), ERRORS_ASSERTION_DESCRIPTION);
    }

    @ParameterizedTest
    @CsvSource({"true, 1", "false, 0"})
    void shouldWaitForMessagesAndSaveResultIntoScopedVariable(boolean waitPassed, int savesVariable)
    {
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL);
        var options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        var logs = mock(Logs.class);
        when(options.logs()).thenReturn(logs);
        var severeEntry = new LogEntry(Level.SEVERE, 1L, "severe");
        var infoEntry = new LogEntry(Level.INFO, 1L, "info");
        when(logs.get(LogType.BROWSER)).thenReturn(new LogEntries(List.of(severeEntry)),
            new LogEntries(List.of(infoEntry)));
        var result = new WaitResult<>();
        result.setWaitPassed(waitPassed);
        when(waitActions.wait(eq(webDriver), argThat(f -> {
            f.apply(webDriver);
            f.apply(webDriver);
            return "appearance of JavaScript infos matching `.*info.*` regex".equals(f.toString());
        }))).thenReturn(result);
        var variableName = "variableName";
        var scopes = Set.of(VariableScope.SCENARIO);
        jsValidationSteps.waitForMessageAndSave(List.of(BrowserLogLevel.INFOS), Pattern.compile(".*info.*"), scopes,
            variableName);
        var ordered = Mockito.inOrder(attachmentPublisher, variableContext);
        ordered.verify(variableContext, times(savesVariable)).putVariable(scopes, variableName, List.of(infoEntry));
        verifyAttachmentPublisher(Map.of(URL, List.of(infoEntry)), ordered);
    }

    private Map<String, Collection<LogEntry>> testCheckJsErrors(String logErrorMessage, Runnable action)
    {
        LogEntry entry = mockGetLogEntry(logErrorMessage);
        action.run();
        return Map.of(URL, Collections.singleton(entry));
    }

    private LogEntry mockGetLogEntry(String logErrorMessage)
    {
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL);
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Logs logs = mock(Logs.class);
        when(options.logs()).thenReturn(logs);
        LogEntry severeEntry = new LogEntry(Level.SEVERE, 1L, logErrorMessage);
        LogEntry infoEntry = new LogEntry(Level.INFO, 1L, logErrorMessage);
        LogEntries logEntries = new LogEntries(Arrays.asList(severeEntry, infoEntry));
        when(logs.get(LogType.BROWSER)).thenReturn(logEntries);
        return severeEntry;
    }

    private void verifyTestActions(Map<String, Collection<LogEntry>> expectedResults, String assertionDescription)
    {
        InOrder inOrder = inOrder(attachmentPublisher, softAssert);
        verifyAttachmentPublisher(expectedResults, inOrder);
        inOrder.verify(softAssert).assertEquals(assertionDescription, 0,
                expectedResults.entrySet().iterator().next().getValue().size());
    }

    private void verifyAttachmentPublisher(Map<String, Collection<LogEntry>> expectedResults, InOrder order)
    {
        order.verify(attachmentPublisher).publishAttachment(
                "/org/vividus/steps/ui/web/js-console-validation-result-table.ftl",
                Map.of("results", expectedResults), "JavaScript console validation results");
    }
}

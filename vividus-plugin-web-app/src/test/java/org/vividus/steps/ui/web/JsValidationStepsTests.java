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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Collection;
import java.util.LinkedHashSet;
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
import org.mockito.MockedStatic;
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
import org.vividus.selenium.logging.BrowserLogManager;
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

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @Mock private WaitActions waitActions;
    @Mock private VariableContext variableContext;
    @InjectMocks private JsValidationSteps jsValidationSteps;

    @Test
    void testCheckThereAreLogEntriesOnOpenedPageFilteredByRegExp()
    {
        var expectedResults = testCheckJsErrors(ERROR_MESSAGE,
                () -> jsValidationSteps.checkThereAreLogEntriesOnOpenedPageFilteredByRegExp(
                        Set.of(BrowserLogLevel.ERRORS), ERROR_MESSAGE_PATTERN));
        var inOrder = inOrder(attachmentPublisher, softAssert);
        verifyAttachmentPublisher(expectedResults, inOrder);
        inOrder.verify(softAssert).assertFalse(String.format("Current page contains JavaScript %s by regex '%s'",
                BrowserLogLevel.ERRORS.toString().toLowerCase(), ERROR_MESSAGE), false);
    }

    @Test
    void testCheckJsErrorsOnPage()
    {
        var expectedResults = testCheckJsErrors(ERROR_MESSAGE,
                () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(Set.of(BrowserLogLevel.ERRORS)));
        verifyTestActions(expectedResults, ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsOnPageByRegExpNoMatch()
    {
        testCheckJsErrors(ERROR_MESSAGE,
                () -> jsValidationSteps.checkJsLogEntriesOnOpenedPageFilteredByRegExp(Set.of(BrowserLogLevel.ERRORS),
                        EXTENSION_PATTERN));
        verifyTestActions(Map.of(URL, Set.of()), ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsAndWarningsOnPage()
    {
        var logLevels = new LinkedHashSet<BrowserLogLevel>();
        logLevels.add(BrowserLogLevel.ERRORS);
        logLevels.add(BrowserLogLevel.WARNINGS);
        var expectedResults = testCheckJsErrors(ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(logLevels));
        verifyTestActions(expectedResults, "Current page contains no JavaScript errors, warnings");
    }

    @Test
    void testCheckJsErrorsContainsBrowserExtensionErrors()
    {
        jsValidationSteps.setIncludeBrowserExtensionLogEntries(true);
        var expectedResults = testCheckJsErrors(EXTENSION + ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(Set.of(BrowserLogLevel.ERRORS)));
        verifyTestActions(expectedResults, ERRORS_ASSERTION_DESCRIPTION);
    }

    @Test
    void testCheckJsErrorsExcludesBrowserExtensionErrors()
    {
        jsValidationSteps.setIncludeBrowserExtensionLogEntries(false);
        testCheckJsErrors(EXTENSION + ERROR_MESSAGE,
            () -> jsValidationSteps.checkJsLogEntriesOnOpenedPage(Set.of(BrowserLogLevel.ERRORS)));
        verifyTestActions(Map.of(URL, Set.of()), ERRORS_ASSERTION_DESCRIPTION);
    }

    @ParameterizedTest
    @CsvSource({"true, 1", "false, 0"})
    void shouldWaitForMessagesAndSaveResultIntoScopedVariable(boolean waitPassed, int savesVariable)
    {
        WebDriver webDriver = mock(withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL);
        Options options = mock();
        when(webDriver.manage()).thenReturn(options);
        Logs logs = mock();
        when(options.logs()).thenReturn(logs);
        var severeEntry = new LogEntry(Level.SEVERE, 1L, "severe");
        var infoEntry = new LogEntry(Level.INFO, 1L, "info");
        when(logs.get(LogType.BROWSER)).thenReturn(new LogEntries(List.of(severeEntry)),
            new LogEntries(List.of(infoEntry)));
        var result = new WaitResult<>();
        result.setWaitPassed(waitPassed);
        var logEntries = Set.of(BrowserLogLevel.INFOS);
        when(waitActions.wait(eq(webDriver), argThat(f -> {
            f.apply(webDriver);
            f.apply(webDriver);
            return "appearance of JavaScript infos matching `.*info.*` regex".equals(f.toString());
        }))).thenReturn(result);
        var variableName = "variableName";
        var scopes = Set.of(VariableScope.SCENARIO);
        jsValidationSteps.waitForMessageAndSave(logEntries, Pattern.compile(".*info.*"), scopes,
            variableName);
        var ordered = inOrder(attachmentPublisher, variableContext);
        ordered.verify(variableContext, times(savesVariable)).putVariable(scopes, variableName, List.of(infoEntry));
        verifyAttachmentPublisher(Map.of(URL, List.of(infoEntry)), ordered);
    }

    @Test
    void shouldClearBrowserConsoleLogs()
    {
        try (MockedStatic<BrowserLogManager> mockedManager = mockStatic(BrowserLogManager.class))
        {
            WebDriver webDriver = mock(WebDriver.class);
            when(webDriverProvider.get()).thenReturn(webDriver);

            jsValidationSteps.clearBrowserConsoleLogs();

            mockedManager.verify(() -> BrowserLogManager.resetBuffer(webDriver));
        }
    }

    private Map<String, Collection<LogEntry>> testCheckJsErrors(String logErrorMessage, Runnable action)
    {
        var entry = mockGetLogEntry(logErrorMessage);
        action.run();
        return Map.of(URL, Set.of(entry));
    }

    private LogEntry mockGetLogEntry(String logErrorMessage)
    {
        WebDriver webDriver = mock(withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(URL);
        Options options = mock();
        when(webDriver.manage()).thenReturn(options);
        Logs logs = mock();
        when(options.logs()).thenReturn(logs);
        var severeEntry = new LogEntry(Level.SEVERE, 1L, logErrorMessage);
        var infoEntry = new LogEntry(Level.INFO, 1L, logErrorMessage);
        var logEntries = new LogEntries(List.of(severeEntry, infoEntry));
        when(logs.get(LogType.BROWSER)).thenReturn(logEntries);
        return severeEntry;
    }

    private void verifyTestActions(Map<String, Collection<LogEntry>> expectedResults, String assertionDescription)
    {
        var inOrder = inOrder(attachmentPublisher, softAssert);
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

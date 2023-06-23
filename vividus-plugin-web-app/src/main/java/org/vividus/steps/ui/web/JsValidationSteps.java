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

package org.vividus.steps.ui.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.logging.BrowserLogLevel;
import org.vividus.selenium.logging.BrowserLogManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.WaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.variable.VariableScope;

public class JsValidationSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;
    private final WaitActions waitActions;
    private final VariableContext variableContext;
    private boolean includeBrowserExtensionLogEntries;

    public JsValidationSteps(IWebDriverProvider webDriverProvider, ISoftAssert softAssert,
            IAttachmentPublisher attachmentPublisher, WaitActions waitActions, VariableContext variableContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
        this.waitActions = waitActions;
        this.variableContext = variableContext;
    }

    /**
     * Checks that opened page contains JavaScript browser console logs that matches regex.
     * <p>Note that log buffers are reset after step invocation, meaning that available log entries correspond to those
     * entries not yet returned for a given log type. In practice, this means that this step invocation will return the
     * available log entries since the last step invocation (or from the last page navigation if corresponding listener
     * is enabled).</p>
     * <p>Step passes if console logs were found, otherwise it fails. All found logs are available in
     * report step's attachment</p>
     * @param logEntries Comma-separated list of entries to check. Possible values: "errors", "warnings", "infos".
     * @param regex Regular expression to filter log entries
     */
    @Then("there are browser console $logEntries by regex `$regex`")
    public void checkThereAreLogEntriesOnOpenedPageFilteredByRegExp(List<BrowserLogLevel> logEntries, Pattern regex)
    {
        Set<LogEntry> filteredLogEntries = getLogEntries(logEntries, regex);
        softAssert.assertFalse(String.format("Current page contains JavaScript %s by regex '%s'",
                toString(logEntries), regex), filteredLogEntries.isEmpty());
    }

    /**
     * Checks that each page opened doesn't contain JavaScript browser errors.
     * <p>Note that log buffers are reset after step invocation, meaning that available log entries correspond to those
     * entries not yet returned for a given log type. In practice, this means that this step invocation will return the
     * available log entries since the last step invocation (or from the last page navigation if corresponding listener
     * is enabled).</p>
     * <p>Step passes if no errors were found, otherwise it fails. All found errors are available in
     * report step's attachment</p>
     * @param logEntries Comma-separated list of entries to check. Possible values: "errors", "warnings", "infos".
     */
    @Then("there are no browser console $logEntries")
    public void checkJsLogEntriesOnOpenedPage(List<BrowserLogLevel> logEntries)
    {
        checkLogMessagesAbsence(getLogEntries(logEntries,
            e -> includeBrowserExtensionLogEntries || !e.getMessage().contains("extension")), logEntries);
    }

    /**
     * Checks that each page opened doesn't contain JavaScript browser errors that matches regex.
     * <p>Note that log buffers are reset after step invocation, meaning that available log entries correspond to those
     * entries not yet returned for a given log type. In practice, this means that this step invocation will return the
     * available log entries since the last step invocation (or from the last page navigation if corresponding listener
     * is enabled).</p>
     * <p>Step passes if no errors were found, otherwise it fails. All found errors are available in
     * report step's attachment</p>
     * @param logEntries Comma-separated list of entries to check. Possible values: "errors", "warnings", "infos".
     * @param regex Regular expression to filter log entries
     */
    @Then(value = "there are no browser console $logEntries by regex `$regex`", priority = 1)
    public void checkJsLogEntriesOnOpenedPageFilteredByRegExp(List<BrowserLogLevel> logEntries, Pattern regex)
    {
        checkLogMessagesAbsence(getLogEntries(logEntries, regex), logEntries);
    }

    /**
     * Waits for the appearance of the console log entries with the expected level and which match regular expression
     * and saves all the entries (including awaited ones) of the expected level gathered during the wait to the
     * scoped variable.
     *
     * @param logEntries   Comma-separated list of entries to check. Possible values: "errors", "warnings", "infos".
     * @param regex        Regular expression to filter log entries.
     * @param scopes       The set (comma-separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>.
     * @param variableName The name of the variable to save the SFTP commands execution result.
     */
    @When("I wait until browser console $logEntries by regex `$regex` appear and save all entries into $scopes "
            + "variable `$variableName`")
    public void waitForMessageAndSave(List<BrowserLogLevel> logEntries, Pattern regex, Set<VariableScope> scopes,
            String variableName)
    {
        WebDriver webDriver = webDriverProvider.get();
        List<LogEntry> messages = new ArrayList<>();
        WaitResult<Boolean> waitResult = waitActions.wait(webDriver, new Function<>()
        {
            @Override
            public Boolean apply(WebDriver driver)
            {
                Set<LogEntry> newMessages = getLogEntries(logEntries, driver);
                messages.addAll(newMessages);
                return newMessages.stream().map(LogEntry::getMessage).anyMatch(regex.asMatchPredicate());
            }

            @Override
            public String toString()
            {
                return String.format("appearance of JavaScript %s matching `%s` regex",
                    JsValidationSteps.toString(logEntries), regex);
            }
        });
        if (waitResult.isWaitPassed())
        {
            variableContext.putVariable(scopes, variableName, messages);
        }
        publishAttachment(webDriver, messages);
    }

    private void checkLogMessagesAbsence(Set<LogEntry> logEntries, List<BrowserLogLevel> logLevels)
    {
        softAssert.assertEquals("Current page contains no JavaScript " + toString(logLevels), 0,
                logEntries.size());
    }

    private Set<LogEntry> getLogEntries(List<BrowserLogLevel> logEntries, Predicate<? super LogEntry> filter)
    {
        WebDriver webDriver = webDriverProvider.get();
        Set<LogEntry> filteredLogEntries = getLogEntries(logEntries, webDriver).stream()
                .filter(filter::test)
                .collect(Collectors.toSet());

        publishAttachment(webDriver, filteredLogEntries);
        return filteredLogEntries;
    }

    private Set<LogEntry> getLogEntries(List<BrowserLogLevel> logEntries, WebDriver webDriver)
    {
        return BrowserLogManager.getFilteredLog(webDriver, logEntries);
    }

    private Set<LogEntry> getLogEntries(List<BrowserLogLevel> logEntries, Pattern regex)
    {
        return getLogEntries(logEntries, message -> regex.matcher(message.getMessage()).matches());
    }

    private void publishAttachment(WebDriver webDriver, Collection<LogEntry> filteredLogEntries)
    {
        attachmentPublisher.publishAttachment("/org/vividus/steps/ui/web/js-console-validation-result-table.ftl",
                Map.of("results", Map.of(webDriver.getCurrentUrl(), filteredLogEntries)),
            "JavaScript console validation results");
    }

    private static String toString(List<BrowserLogLevel> logLevels)
    {
        return logLevels.stream()
                .map(BrowserLogLevel::toString)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
    }

    public void setIncludeBrowserExtensionLogEntries(boolean includeBrowserExtensionLogEntries)
    {
        this.includeBrowserExtensionLogEntries = includeBrowserExtensionLogEntries;
    }
}

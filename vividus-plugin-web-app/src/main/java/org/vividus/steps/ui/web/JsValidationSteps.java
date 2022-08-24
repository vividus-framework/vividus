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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntry;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.logging.BrowserLogLevel;
import org.vividus.selenium.logging.BrowserLogManager;
import org.vividus.softassert.ISoftAssert;

public class JsValidationSteps
{
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IAttachmentPublisher attachmentPublisher;
    @Inject private ISoftAssert softAssert;
    private boolean includeBrowserExtensionLogEntries;

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
    @Then(value = "there are no browser console $logEntries by regex '$regex'", priority = 1)
    public void checkJsLogEntriesOnOpenedPageFilteredByRegExp(List<BrowserLogLevel> logEntries, Pattern regex)
    {
        checkLogMessagesAbsence(getLogEntries(logEntries, regex), logEntries);
    }

    private void checkLogMessagesAbsence(Set<LogEntry> logEntries, List<BrowserLogLevel> logLevels)
    {
        softAssert.assertEquals("Current page contains no JavaScript " + toString(logLevels), 0,
                logEntries.size());
    }

    private Set<LogEntry> getLogEntries(List<BrowserLogLevel> logEntries, Predicate<? super LogEntry> filter)
    {
        WebDriver webDriver = webDriverProvider.get();
        Set<LogEntry> filteredLogEntries = BrowserLogManager.getFilteredLog(webDriver, logEntries).stream()
                .filter(filter::test)
                .collect(Collectors.toSet());

        publishAttachment(Map.of(webDriver.getCurrentUrl(), filteredLogEntries));
        return filteredLogEntries;
    }

    private Set<LogEntry> getLogEntries(List<BrowserLogLevel> logEntries, Pattern regex)
    {
        return getLogEntries(logEntries, message -> regex.matcher(message.getMessage()).matches());
    }

    private void publishAttachment(Map<String, Set<LogEntry>> results)
    {
        attachmentPublisher.publishAttachment("/org/vividus/steps/ui/web/js-console-validation-result-table.ftl",
                Map.of("results", results), "JavaScript console validation results");
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

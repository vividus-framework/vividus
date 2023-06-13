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

package org.vividus.accessibility;

import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.accessibility.executor.AccessibilityEngine;
import org.vividus.accessibility.executor.AccessibilityTestExecutor;
import org.vividus.accessibility.model.AbstractAccessibilityCheckOptions;
import org.vividus.accessibility.model.axe.AxeCheckOptions;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.accessibility.model.axe.AxeReportEntry;
import org.vividus.accessibility.model.axe.ResultType;
import org.vividus.accessibility.model.htmlcs.AccessibilityViolation;
import org.vividus.accessibility.model.htmlcs.HtmlCsCheckOptions;
import org.vividus.accessibility.model.htmlcs.ViolationLevel;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

public class AccessibilitySteps
{
    private static final String HTML_CS_MESSAGE = "Number of accessibility violations of level %s"
            + " and above at the page %s";
    private static final String AXE_CORE_MESSAGE = "[%s] Number of accessibility violations at the page %s";

    private AccessibilityEngine accessibilityEngine;

    private final AccessibilityTestExecutor accessibilityTestExecutor;
    private final IWebDriverProvider webDriverProvider;
    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;

    public AccessibilitySteps(AccessibilityTestExecutor accessibilityTestExecutor,
                              IWebDriverProvider webDriverProvider,
                              IAttachmentPublisher attachmentPublisher,
                              ISoftAssert softAssert)
    {
        this.accessibilityTestExecutor = accessibilityTestExecutor;
        this.webDriverProvider = webDriverProvider;
        this.attachmentPublisher = attachmentPublisher;
        this.softAssert = softAssert;
    }

    /**
     * Performs an accessibility scan of the currently opened page, using specified criteria.
     *
     * @param checkOptions Example table with check options, where:
     *        <ul>
     *          <li><b>elementsToCheck</b> -    The locators of elements to check</li>
     *          <li><b>elementsToIgnore</b> -   The locators of elements to ignore</li>
     *          <li><b>standard</b> -           The standard to check against. One of:
     *                                          <ul>
     *                                            <li><b>WCGA2A</b></li>
     *                                            <li><b>WCGA2AA</b></li>
     *                                            <li><b>WCGA2AAA</b></li>
     *                                            <li><b>SECTION_508</b></li>
     *                                          </ul>
     *          <li><b>violationsToIgnore</b> - The comma-separated violations that will be ignored.</li>
     *          <li><b>violationsToCheck</b> -  The comma-separated violations that will be checked.</li>
     *          <li><b>level</b> -              The level of violations which will be considered as fails. One of:
     *                                          <ul>
     *                                            <li><b>ERROR</b></li>
     *                                            <li><b>WARNING</b></li>
     *                                            <li><b>NOTICE</b></li>
     *                                          </ul>
     *        </ul>
     * @see <a href="https://squizlabs.github.io/HTML_CodeSniffer/Standards/WCAG2/">WCAG2.0</a>
     * @see <a href="https://squizlabs.github.io/HTML_CodeSniffer/Standards/Section508/">Section508</a>
     */
    @When("I perform accessibility scan:$options")
    public void performAccessibilityScan(ExamplesTable checkOptions)
    {
        String currentUrl = webDriverProvider.get().getCurrentUrl();

        if (accessibilityEngine == AccessibilityEngine.HTML_CS)
        {
            perform(checkOptions, HtmlCsCheckOptions.class, options ->
            {
                List<AccessibilityViolation> violations = accessibilityTestExecutor
                        .execute(accessibilityEngine, options, AccessibilityViolation.class);
                ViolationLevel level = options.getLevel();
                publishAttachment(options.getStandard(), currentUrl, convertResult(violations));
                softAssert.assertThat(String.format(HTML_CS_MESSAGE, level, currentUrl),
                    violations.stream().filter(v -> v.getTypeCode() <= level.getCode()).count(), equalTo(0L));
            });
        }
        else
        {
            perform(checkOptions, AxeCheckOptions.class, options ->
            {
                List<AxeReportEntry> reportEntries = accessibilityTestExecutor.execute(accessibilityEngine, options,
                        AxeReportEntry.class);

                AxeOptions axeOptions = options.getRunOnly();
                String axeOptionsAsString = axeOptions.getStandardOrRulesAsString();
                int numberOfResults = reportEntries.stream().map(AxeReportEntry::getResults).map(List::size)
                        .mapToInt(Integer::intValue).sum();
                Validate.isTrue(numberOfResults != 0, "Axe scan has not returned any results for the provided %s,"
                        + " please make sure the configuration is valid", axeOptionsAsString);

                publishAttachment(axeOptionsAsString, currentUrl, Map.of("entries", reportEntries, "url", currentUrl,
                        "run", axeOptions));

                long failures = reportEntries.stream().filter(e -> ResultType.FAILED == e.getType())
                        .map(AxeReportEntry::getResults).map(List::size).findFirst().orElse(0);
                softAssert.assertThat(String.format(AXE_CORE_MESSAGE, axeOptionsAsString, currentUrl), failures,
                        equalTo(0L));
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractAccessibilityCheckOptions> void perform(ExamplesTable checkOptions, Class<T> type,
            Consumer<T> onOption)
    {
        checkOptions.getRowsAsParameters(true)
                    .stream()
                    .map(p -> (T) p.as(type))
                    .filter(o -> null != o.getElementsToCheck())
                    .forEach(onOption);
    }

    private Map<String, Map<String, List<AccessibilityViolation>>>
            convertResult(List<AccessibilityViolation> accessibilityViolations)
    {
        Map<String, Map<String, List<AccessibilityViolation>>> result =
                accessibilityViolations.stream()
                                       .collect(Collectors.groupingBy(v -> v.getType().toString(),
                                                Collectors.groupingBy(AccessibilityViolation::getCode, TreeMap::new,
                                                        Collectors.toList())));
        Stream.of(ViolationLevel.values())
              .map(Object::toString)
              .forEach(l -> result.computeIfAbsent(l, k -> Map.of()));

        return result;
    }

    private void publishAttachment(String standard, String pageUrl, Object result)
    {
        attachmentPublisher.publishAttachment(accessibilityEngine.getReportTemplate(), result,
                String.format("[%s] Accessibility report for page: %s", standard, pageUrl));
    }

    public void setAccessibilityEngine(AccessibilityEngine accessibilityEngine)
    {
        this.accessibilityEngine = accessibilityEngine;
    }
}

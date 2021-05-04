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

package org.vividus.accessibility;

import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.annotations.Then;
import org.vividus.accessibility.engine.AccessibilityTestEngine;
import org.vividus.accessibility.model.AccessibilityCheckOptions;
import org.vividus.accessibility.model.AccessibilityViolation;
import org.vividus.accessibility.model.ViolationLevel;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.UriUtils;

public class AccessibilitySteps
{
    private static final String MESSAGE = "Number of accessibility violations of level %s"
            + " and above at the page %s";

    private final AccessibilityTestEngine accessibilityTestEngine;
    private final IWebDriverProvider webDriverProvider;
    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;

    public AccessibilitySteps(AccessibilityTestEngine accessibilityTestEngine,
                              IWebDriverProvider webDriverProvider,
                              IAttachmentPublisher attachmentPublisher,
                              ISoftAssert softAssert)
    {
        this.accessibilityTestEngine = accessibilityTestEngine;
        this.webDriverProvider = webDriverProvider;
        this.attachmentPublisher = attachmentPublisher;
        this.softAssert = softAssert;
    }

    /**
     * Executes accessibility compliance check of the currently opened page, using specified criteria.
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
    @Then("I test accessibility:$options")
    public void checkAccessibility(List<AccessibilityCheckOptions> checkOptions)
    {
        URI currentUrl = UriUtils.createUri(webDriverProvider.get().getCurrentUrl());
        checkOptions.stream()
                    .filter(o -> null == o.getElementsToCheck() || !o.getElementsToCheck().isEmpty())
                    .forEach(options -> {
                        List<AccessibilityViolation>  violations = accessibilityTestEngine.analyze(options);
                        ViolationLevel level = options.getLevel();
                        publishAttachment(currentUrl, violations);
                        softAssert.assertThat(String.format(MESSAGE, level, currentUrl),
                            violations.stream().filter(v -> v.getTypeCode() <= level.getCode()).count(), equalTo(0L));
                    });
    }

    private void publishAttachment(URI pageUrl, List<AccessibilityViolation> accessibilityViolations)
    {
        Map<String, Map<String, List<AccessibilityViolation>>> result =
                accessibilityViolations.stream()
                                       .collect(Collectors.groupingBy(v -> v.getType().toString(),
                                                Collectors.groupingBy(AccessibilityViolation::getCode, TreeMap::new,
                                                        Collectors.toList())));
        Stream.of(ViolationLevel.values())
              .map(Object::toString)
              .forEach(l -> result.computeIfAbsent(l, k -> Map.of()));
        attachmentPublisher.publishAttachment("/org/vividus/accessibility/violations-table.ftl",
                result, "Accessibility violations at page " + pageUrl);
    }
}

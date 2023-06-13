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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.accessibility.model.htmlcs.AccessibilityStandard.WCAG2A;
import static org.vividus.accessibility.model.htmlcs.ViolationLevel.ERROR;
import static org.vividus.accessibility.model.htmlcs.ViolationLevel.NOTICE;
import static org.vividus.accessibility.model.htmlcs.ViolationLevel.WARNING;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.accessibility.executor.AccessibilityEngine;
import org.vividus.accessibility.executor.AccessibilityTestExecutor;
import org.vividus.accessibility.model.axe.AxeCheckOptions;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.accessibility.model.axe.AxeReportEntry;
import org.vividus.accessibility.model.axe.Result;
import org.vividus.accessibility.model.axe.ResultType;
import org.vividus.accessibility.model.htmlcs.AccessibilityStandard;
import org.vividus.accessibility.model.htmlcs.AccessibilityViolation;
import org.vividus.accessibility.model.htmlcs.HtmlCsCheckOptions;
import org.vividus.accessibility.model.htmlcs.ViolationLevel;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class AccessibilityStepsTests
{
    private static final String MATCHER = "<0L>";
    private static final String PAGE = "https://fus-ro.dah";
    private static final String TEMPLATE_NAME = "/org/vividus/accessibility/htmlcs-accessibility-report.ftl";
    private static final String NOTICE2 = "Notice";
    private static final String WARNING2 = "Warning";
    private static final String ERROR2 = "Error";
    private static final String CODE = "code";
    private static final String ASSERTION_MESSAGE =
            "Number of accessibility violations of level Warning and above at the page https://fus-ro.dah";

    @Mock private WebElement elementToCheck;
    @Mock private AccessibilityTestExecutor accessibilityTestExecutor;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;

    @InjectMocks private AccessibilitySteps accessibilitySteps;

    @BeforeEach
    void beforeEach()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(PAGE);
    }

    @Test
    void shouldPerformHtmlCsAccessibilityScan()
    {
        accessibilitySteps.setAccessibilityEngine(AccessibilityEngine.HTML_CS);
        HtmlCsCheckOptions first = createOptions(WCAG2A, WARNING, List.of());
        HtmlCsCheckOptions second = createOptions(WCAG2A, WARNING, List.of(elementToCheck));
        HtmlCsCheckOptions third = createOptions(WCAG2A, WARNING, null);
        Parameters params = mock(Parameters.class);
        when(params.as(HtmlCsCheckOptions.class)).thenReturn(first).thenReturn(second).thenReturn(third);
        ExamplesTable checkOptions = mock(ExamplesTable.class);
        when(checkOptions.getRowsAsParameters(true)).thenReturn(List.of(params, params, params));

        AccessibilityViolation warning = createViolation(WARNING, 2);
        AccessibilityViolation notice = createViolation(NOTICE, 1);
        AccessibilityViolation error = createViolation(ERROR, 3);

        when(accessibilityTestExecutor.execute(AccessibilityEngine.HTML_CS, first, AccessibilityViolation.class))
                .thenReturn(List.of(warning, notice, error));
        when(accessibilityTestExecutor.execute(AccessibilityEngine.HTML_CS, second, AccessibilityViolation.class))
                .thenReturn(List.of());

        accessibilitySteps.performAccessibilityScan(checkOptions);

        InOrder ordered = Mockito.inOrder(softAssert, attachmentPublisher);
        String title = "[WCAG2A] Accessibility report for page: " + PAGE;
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(WARNING2, Map.of(CODE, List.of(warning)),
                       NOTICE2, Map.of(CODE, List.of(notice)),
                       ERROR2, Map.of(CODE, List.of(error))), title);
        ordered.verify(softAssert).assertThat(
                eq(ASSERTION_MESSAGE),
                eq(2L),
                argThat(m -> MATCHER.equals(m.toString())));
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(WARNING2, Map.of(),
                       ERROR2, Map.of(),
                       NOTICE2, Map.of()), title);
        ordered.verify(softAssert).assertThat(
                eq(ASSERTION_MESSAGE),
                eq(0L),
                argThat(m -> MATCHER.equals(m.toString())));
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void shouldExecuteAxeCoreAccessibilityTest()
    {
        accessibilitySteps.setAccessibilityEngine(AccessibilityEngine.AXE_CORE);
        AxeOptions axeRun = AxeOptions.forStandard("WCAG2A");
        AxeCheckOptions options = mock(AxeCheckOptions.class);
        when(options.getRunOnly()).thenReturn(axeRun);
        Parameters params = mock(Parameters.class);
        when(params.as(AxeCheckOptions.class)).thenReturn(options);
        ExamplesTable checkOptions = mock(ExamplesTable.class);
        when(checkOptions.getRowsAsParameters(true)).thenReturn(List.of(params));

        AxeReportEntry failedEntry = createEntry(ResultType.FAILED);
        AxeReportEntry passedEntry = createEntry(ResultType.PASSED);
        when(accessibilityTestExecutor.execute(AccessibilityEngine.AXE_CORE, options, AxeReportEntry.class))
                .thenReturn(List.of(passedEntry, failedEntry));

        accessibilitySteps.performAccessibilityScan(checkOptions);

        verify(attachmentPublisher).publishAttachment(
            "/org/vividus/accessibility/axe-accessibility-report.ftl",
            Map.of("entries", List.of(passedEntry, failedEntry), "url", PAGE, "run", axeRun),
            "[WCAG2A standard] Accessibility report for page: " + PAGE
        );
        verify(softAssert).assertThat(eq("[WCAG2A standard] Number of accessibility violations at the page " + PAGE),
                eq(1L), argThat(m -> MATCHER.equals(m.toString())));
    }

    @Test
    void shouldFailIfAxeCodeDidntReturnAnyResults()
    {
        accessibilitySteps.setAccessibilityEngine(AccessibilityEngine.AXE_CORE);
        AxeOptions axeRun = AxeOptions.forStandard("WCAG2AA");
        AxeCheckOptions options = mock(AxeCheckOptions.class);
        when(options.getRunOnly()).thenReturn(axeRun);
        Parameters params = mock(Parameters.class);
        when(params.as(AxeCheckOptions.class)).thenReturn(options);
        ExamplesTable checkOptions = mock(ExamplesTable.class);
        when(checkOptions.getRowsAsParameters(true)).thenReturn(List.of(params));

        AxeReportEntry failedEntry = createEntry(ResultType.FAILED);
        failedEntry.setResults(List.of());
        when(accessibilityTestExecutor.execute(AccessibilityEngine.AXE_CORE, options, AxeReportEntry.class))
                .thenReturn(List.of(failedEntry));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> accessibilitySteps.performAccessibilityScan(checkOptions));
        assertEquals("Axe scan has not returned any results for the provided WCAG2AA standard, please make "
                + "sure the configuration is valid", thrown.getMessage());

        verifyNoInteractions(attachmentPublisher);
    }

    private AxeReportEntry createEntry(ResultType type)
    {
        AxeReportEntry entry = new AxeReportEntry();
        entry.setType(type);
        Result result = new Result();
        entry.setResults(List.of(result));
        return entry;
    }

    private HtmlCsCheckOptions createOptions(AccessibilityStandard standard, ViolationLevel level,
            List<WebElement> elementsToCheck)
    {
        HtmlCsCheckOptions options = new HtmlCsCheckOptions(standard);
        options.setElementsToCheck(elementsToCheck);
        options.setLevel(level);
        return options;
    }

    private AccessibilityViolation createViolation(ViolationLevel level, int typeCode)
    {
        AccessibilityViolation violation = new AccessibilityViolation();
        violation.setCode(CODE);
        violation.setType(level);
        violation.setTypeCode(typeCode);
        return violation;
    }
}

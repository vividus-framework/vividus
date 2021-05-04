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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.vividus.accessibility.model.AccessibilityStandard.WCAG2A;
import static org.vividus.accessibility.model.ViolationLevel.ERROR;
import static org.vividus.accessibility.model.ViolationLevel.NOTICE;
import static org.vividus.accessibility.model.ViolationLevel.WARNING;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.vividus.accessibility.engine.AccessibilityTestEngine;
import org.vividus.accessibility.model.AccessibilityCheckOptions;
import org.vividus.accessibility.model.AccessibilityStandard;
import org.vividus.accessibility.model.AccessibilityViolation;
import org.vividus.accessibility.model.ViolationLevel;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class AccessibilityStepsTests
{
    private static final String MATCHER = "<0L>";
    private static final String TITLE = "Accessibility violations at page https://fus-ro.dah";
    private static final String TEMPLATE_NAME = "/org/vividus/accessibility/violations-table.ftl";
    private static final String NOTICE2 = "Notice";
    private static final String WARNING2 = "Warning";
    private static final String ERROR2 = "Error";
    private static final String CODE = "code";
    private static final String ASSERTION_MESSAGE =
            "Number of accessibility violations of level Warning and above at the page https://fus-ro.dah";

    @Mock private AccessibilityTestEngine accessibilityTestEngine;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;

    @InjectMocks private AccessibilitySteps accessibilitySteps;

    @BeforeEach
    void beforeEach()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn("https://fus-ro.dah");
    }

    @Test
    void shouldExecuteAccessibilityTest()
    {
        AccessibilityCheckOptions first = createOptions(WCAG2A, WARNING, null);
        AccessibilityCheckOptions second = createOptions(WCAG2A, WARNING, "a");
        AccessibilityCheckOptions third = createOptions(WCAG2A, WARNING, "");
        List<AccessibilityCheckOptions> checkOptions = List.of(first, second, third);
        AccessibilityViolation warning = createViolation(WARNING, 2);
        AccessibilityViolation notice = createViolation(NOTICE, 1);
        AccessibilityViolation error = createViolation(ERROR, 3);
        when(accessibilityTestEngine.analyze(first)).thenReturn(List.of(warning, notice, error));
        when(accessibilityTestEngine.analyze(second)).thenReturn(List.of());
        accessibilitySteps.checkAccessibility(checkOptions);
        InOrder ordered = Mockito.inOrder(softAssert, attachmentPublisher);
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(WARNING2, Map.of(CODE, List.of(warning)),
                       NOTICE2, Map.of(CODE, List.of(notice)),
                       ERROR2, Map.of(CODE, List.of(error))), TITLE);
        ordered.verify(softAssert).assertThat(
                eq(ASSERTION_MESSAGE),
                eq(2L),
                argThat(m -> MATCHER.equals(m.toString())));
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE_NAME,
                Map.of(WARNING2, Map.of(),
                       ERROR2, Map.of(),
                       NOTICE2, Map.of()), TITLE);
        ordered.verify(softAssert).assertThat(
                eq(ASSERTION_MESSAGE),
                eq(0L),
                argThat(m -> MATCHER.equals(m.toString())));
    }

    private AccessibilityCheckOptions createOptions(AccessibilityStandard standard, ViolationLevel level,
        String elementsToCheck)
    {
        AccessibilityCheckOptions options = new AccessibilityCheckOptions(standard);
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

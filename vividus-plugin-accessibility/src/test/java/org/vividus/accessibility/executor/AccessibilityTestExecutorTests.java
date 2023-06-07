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

package org.vividus.accessibility.executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.accessibility.model.axe.AxeCheckOptions;
import org.vividus.accessibility.model.axe.AxeOptions;
import org.vividus.accessibility.model.axe.AxeReportEntry;
import org.vividus.accessibility.model.axe.CheckResult;
import org.vividus.accessibility.model.axe.Node;
import org.vividus.accessibility.model.axe.Result;
import org.vividus.accessibility.model.axe.ResultType;
import org.vividus.accessibility.model.htmlcs.AccessibilityStandard;
import org.vividus.accessibility.model.htmlcs.AccessibilityViolation;
import org.vividus.accessibility.model.htmlcs.HtmlCsCheckOptions;
import org.vividus.accessibility.model.htmlcs.ViolationLevel;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AccessibilityTestExecutorTests
{
    @Mock private WebElement rootElement;
    @Mock private WebElement elementToCheck;
    @Mock private WebElement elementToIgnore;

    @Mock private WebJavascriptActions webJavaScriptActions;

    @InjectMocks private AccessibilityTestExecutor executor;

    @Test
    void shouldExecuteHtmlCsAccessibilityCheck()
    {
        HtmlCsCheckOptions options = new HtmlCsCheckOptions(AccessibilityStandard.WCAG2AAA);
        options.setHideElements(List.of(elementToIgnore));
        options.setIgnore(List.of("error"));
        options.setInclude(List.of("no_error"));
        options.setLevel(ViolationLevel.ERROR);
        options.setRootElement(Optional.of(rootElement));
        options.setElementsToCheck(List.of(elementToCheck));

        AccessibilityEngine engine = AccessibilityEngine.HTML_CS;
        when(webJavaScriptActions.executeAsyncScript(engine.getScript() + engine.getRunner()
                + "injectAccessibilityCheck(window, {\"standard\":\"WCAG2AAA\",\"ignore\":[\"error\"],\"include\":"
                + "[\"no_error\"]}, arguments[0], arguments[1], arguments[2], arguments[arguments.length - 1]);",
                rootElement, List.of(elementToCheck), List.of(elementToIgnore)))
            .thenReturn("[{\"code\":\"WCAG2AAA\","
                        + "\"context\":\"</a>\","
                        + "\"message\":\"BadPuppy\","
                        + "\"type\":\"error\","
                        + "\"typeCode\":1,"
                        + "\"selector\":\"#id\"}]");

        List<AccessibilityViolation> result = executor.execute(engine, options, AccessibilityViolation.class);

        assertThat(result, hasSize(1));
        AccessibilityViolation violation = result.get(0);
        Assertions.assertAll(
            () -> assertEquals("WCAG2AAA", violation.getCode()),
            () -> assertEquals("</a>", violation.getContext()),
            () -> assertEquals("BadPuppy", violation.getMessage()),
            () -> assertEquals(ViolationLevel.ERROR, violation.getType()),
            () -> assertEquals(1, violation.getTypeCode()),
            () -> assertEquals("#id", violation.getSelector()));
    }

    @Test
    void shouldExecuteAxeCoreAccessibilityCheck()
    {
        AxeCheckOptions options = new AxeCheckOptions();
        options.setHideElements(List.of(elementToIgnore));
        options.setElementsToCheck(List.of(elementToCheck));
        String standard = "wcag2a";
        options.setRunOnly(AxeOptions.forStandard(standard));
        String output = ResourceUtils.loadResource(getClass(), "axe-core.json");

        AccessibilityEngine engine = AccessibilityEngine.AXE_CORE;
        when(webJavaScriptActions.executeAsyncScript(engine.getScript() + engine.getRunner()
            + "injectAccessibilityCheck(window, {\"runOnly\":{\"type\":\"tag\",\"values\":[\"wcag2a\"]},"
            + "\"reporter\":\"v2\"}, arguments[0], arguments[1], arguments[2], arguments[arguments.length - 1]);",
            null, List.of(elementToCheck), List.of(elementToIgnore))).thenReturn(output);

        List<AxeReportEntry> entries = executor.execute(engine, options, AxeReportEntry.class);
        assertThat(entries, hasSize(1));
        AxeReportEntry entry = entries.get(0);
        assertEquals(ResultType.FAILED, entry.getType());
        List<Result> results = entry.getResults();
        assertThat(results, hasSize(1));
        Result result = results.get(0);
        assertEquals("button-name", result.getId());
        String impact = "critical";
        assertEquals(impact, result.getImpact());
        assertEquals(Set.of(standard, "section508"), result.getTags());
        assertEquals("Ensures buttons have discernible text", result.getDescription());
        assertEquals("Buttons must have discernible text", result.getHelp());
        assertEquals("https://dequeuniversity.com/rules/axe/4.7/button-name?application=axeAPI", result.getHelpUrl());
        List<Node> nodes = result.getNodes();
        assertThat(nodes, hasSize(1));
        Node node = nodes.get(0);
        assertEquals(impact, node.getImpact());
        assertEquals("<button class=\"ui-datepicker-trigger\" type=\"button\">...</button>", node.getHtml());
        assertEquals(List.of(".departure-date > .ui-datepicker-trigger:nth-child(4)"), node.getTarget());
        assertThat(node.getAll(), empty());
        assertThat(node.getNone(), empty());
        List<CheckResult> anys = node.getAny();
        assertThat(anys, hasSize(1));
        CheckResult any = anys.get(0);
        assertEquals("presentational-role", any.getId());
        assertEquals("minor", any.getImpact());
        assertEquals("Element's default semantics were not overridden with role=\"none\" or role=\"presentation\"",
                any.getMessage());
    }
}

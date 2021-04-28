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

package org.vividus.accessibility.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.accessibility.model.AccessibilityCheckOptions;
import org.vividus.accessibility.model.AccessibilityStandard;
import org.vividus.accessibility.model.AccessibilityViolation;
import org.vividus.accessibility.model.ViolationLevel;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class HtmlCsAccessibilityTestEngineTests
{
    private static final String HTML_CS_JS =
            ResourceUtils.loadResource(HtmlCsAccessibilityTestEngine.class, "HTMLCS.js");
    private static final String PA11Y_JS =
            ResourceUtils.loadResource(HtmlCsAccessibilityTestEngine.class, "pa11y.js");

    @Mock private WebJavascriptActions webJavaScriptActions;

    @InjectMocks private HtmlCsAccessibilityTestEngine engine;

    @Test
    void shouldExecuteAccessibilityCheck()
    {
        AccessibilityCheckOptions options = new AccessibilityCheckOptions(AccessibilityStandard.WCAG2AAA);
        options.setHideElements("a,div");
        options.setIgnore(List.of("error"));
        options.setInclude(List.of("no_error"));
        options.setLevel(ViolationLevel.ERROR);
        options.setRootElement("html");
        options.setElementsToCheck("body");
        when(webJavaScriptActions.executeAsyncScript(HTML_CS_JS + PA11Y_JS
                + "injectPa11y(window, {\"standard\":\"WCAG2AAA\",\"ignore\":[\"error\"],\"include\":[\"no_error\"],"
                + "\"rootElement\":\"html\",\"elementsToCheck\":\"body\",\"hideElements\":\"a,div\"},"
                + " arguments[arguments.length - 1]);"))
            .thenReturn("[{\"code\":\"WCAG2AAA\","
                        + "\"context\":\"</a>\","
                        + "\"message\":\"BadPuppy\","
                        + "\"type\":\"error\","
                        + "\"typeCode\":1,"
                        + "\"selector\":\"#id\"}]");

        List<AccessibilityViolation> result = engine.analyze(options);

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
}

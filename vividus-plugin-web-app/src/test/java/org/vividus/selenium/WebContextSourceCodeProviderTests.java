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

package org.vividus.selenium;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.CssSelectorFactory;
import org.vividus.ui.web.action.WebJavascriptActions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class})
class WebContextSourceCodeProviderTests
{
    private static final String OUTER_HTML_SCRIPT = "return arguments[0].outerHTML;";
    private static final String SHADOW_DOM_SCRIPT = "const sources = new Map();\n"
            + "function getShadowSource(element) {\n"
            + "    Array.from(element.querySelectorAll('*'))"
            + "         .filter(node => node.shadowRoot)"
            + "         .forEach(e => {\n"
            + "                           sources.set('Shadow dom sources. Selector: '"
            + "                               + getCssSelectorForElement(e),"
            + "                           e.shadowRoot.innerHTML);\n"
            + "                           getShadowSource(e.shadowRoot);\n"
            + "          })\n"
            + "}\n"
            + "getShadowSource(%s);\n"
            + "return Object.fromEntries(sources)";
    private static final String APPLICATION_SOURCE_CODE = "Application source code";

    @Mock private IUiContext uiContext;
    @Mock private WebJavascriptActions webJavascriptActions;

    @InjectMocks private WebContextSourceCodeProvider sourceCodeProvider;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WebContextSourceCodeProvider.class);

    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    @Test
    void shouldReturnWholePageForDriverContext()
    {
        var webDriver = mock(WebDriver.class);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        var pageSource = "<html/>";
        when(webDriver.getPageSource()).thenReturn(pageSource);
        var sourceTitle = "Shadow dom sources. Selector: div";
        var script = CssSelectorFactory.CSS_SELECTOR_FACTORY_SCRIPT
                + String.format(SHADOW_DOM_SCRIPT, "document.documentElement");
        when(webJavascriptActions.executeScript(script)).thenReturn(Map.of(sourceTitle, pageSource));
        assertEquals(Map.of(APPLICATION_SOURCE_CODE, pageSource, sourceTitle, pageSource),
                sourceCodeProvider.getSourceCode());
    }

    @Test
    void shouldReturnElementSourceForElementContext()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        var elementSource = "<div/>";
        var sourceTitle = "Shadow dom sources. Selector: a";
        mockShadowDomSourcesRetrieval(webElement, Map.of(sourceTitle, elementSource));
        when(webJavascriptActions.executeScript(OUTER_HTML_SCRIPT, webElement)).thenReturn(elementSource);
        assertEquals(Map.of(APPLICATION_SOURCE_CODE, elementSource, sourceTitle, elementSource),
                sourceCodeProvider.getSourceCode());
    }

    @Test
    void shouldHandleStaleElementsCorrectly()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webJavascriptActions.executeScript(OUTER_HTML_SCRIPT, webElement)).thenThrow(
                StaleElementReferenceException.class);
        mockShadowDomSourcesRetrieval(webElement, Map.of());
        assertEquals(Map.of(), sourceCodeProvider.getSourceCode());
        assertEquals(logger.getLoggingEvents(), List.of(debug("Unable to get sources of the stale element")));
    }

    @Test
    void shouldReturnEmptyValueForNullSearchContext()
    {
        when(uiContext.getSearchContext()).thenReturn(null);
        when(webJavascriptActions.executeScript(any(String.class))).thenReturn(Map.of());
        assertEquals(Map.of(), sourceCodeProvider.getSourceCode());
    }

    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private void mockShadowDomSourcesRetrieval(WebElement webElement, Map<String, String> sources)
    {
        var script = CssSelectorFactory.CSS_SELECTOR_FACTORY_SCRIPT + String.format(SHADOW_DOM_SCRIPT, "arguments[0]");
        when(webJavascriptActions.executeScript(script, webElement)).thenReturn(sources);
    }
}

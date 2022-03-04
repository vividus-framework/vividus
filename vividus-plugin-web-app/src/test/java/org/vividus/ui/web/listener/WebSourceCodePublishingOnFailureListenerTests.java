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

package org.vividus.ui.web.listener;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class})
class WebSourceCodePublishingOnFailureListenerTests
{
    private static final String INNER_HTML = "innerHTML";
    private static final String SHADOW_DOM_SCRIPT = CssSelectorFactory.CSS_SELECTOR_FACTORY_SCRIPT
            + "const sources = new Map();\n"
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

    @InjectMocks private WebSourceCodePublishingOnFailureListener listener;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WebSourceCodePublishingOnFailureListener.class);

    @Test
    void shouldReturnWholePageForDriverContext()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        String pageSource = "<html/>";
        when(webDriver.getPageSource()).thenReturn(pageSource);
        var sourceTitle = "Shadow dom sources. Selector: div";
        when(webJavascriptActions.executeScript(eq(String.format(SHADOW_DOM_SCRIPT, "document.documentElement")),
            any())).thenReturn(Map.of(sourceTitle, pageSource));
        assertEquals(Map.of(APPLICATION_SOURCE_CODE, pageSource, sourceTitle, pageSource), listener.getSourceCode());
    }

    @Test
    void shouldReturnElementSourceForElementContext()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        String elementSource = "<div/>";
        var sourceTitle = "Shadow dom sources. Selector: a";
        when(webJavascriptActions.executeScript(eq(String.format(SHADOW_DOM_SCRIPT, "arguments[0]")),
                any())).thenReturn(Map.of(sourceTitle, elementSource));
        when(webElement.getAttribute(INNER_HTML)).thenReturn(elementSource);
        assertEquals(Map.of(APPLICATION_SOURCE_CODE, elementSource, sourceTitle, elementSource),
            listener.getSourceCode());
    }

    @Test
    void shouldHandleStaleElementsCorrectly()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webElement.getAttribute(INNER_HTML)).thenThrow(StaleElementReferenceException.class);
        when(webJavascriptActions.executeScript(any(String.class), any())).thenReturn(Map.of());
        assertEquals(Map.of(), listener.getSourceCode());
        assertEquals(logger.getLoggingEvents(), List.of(debug("Unable to get sources of the stale element")));
    }

    @Test
    void shouldReturnEmptyValueForNullSearchContext()
    {
        when(uiContext.getSearchContext()).thenReturn(null);
        when(webJavascriptActions.executeScript(any(String.class), any())).thenReturn(Map.of());
        assertEquals(Map.of(), listener.getSourceCode());
    }
}

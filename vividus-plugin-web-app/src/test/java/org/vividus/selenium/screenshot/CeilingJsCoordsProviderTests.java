/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.WebJavascriptActions;

import ru.yandex.qatools.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class CeilingJsCoordsProviderTests
{
    private static final String RESULT_COORDS = "{"
            + "  x: 1,"
            + "  y: 1,"
            + "  width: 1,"
            + "  height: 1"
           + "}";

    private static final String GET_COORDINATES =
            "var rect = arguments[0].getBoundingClientRect();"
          + "var style = window.getComputedStyle(arguments[0]);"
          + "var margin ="
          + " {"
          + "  left: getAttributeValue(style, \"margin-left\"),"
          + "  top: getAttributeValue(style, \"margin-top\"),"
          + "  right: getAttributeValue(style, \"margin-right\"),"
          + "  bottom: getAttributeValue(style, \"margin-bottom\")"
          + " };"
          + "function getAttributeValue(style, attributeName){return Math.max(0, parseInt(style[attributeName]))}"
          + "var scrollTop = %s;"
          + "return JSON.stringify({"
          + "  x: Math.ceil(rect.x),"
          + "  y: Math.ceil(rect.y + scrollTop),"
          + "  width: Math.trunc(rect.width + margin.left + margin.right),"
          + "  height: Math.trunc(rect.height + margin.top + margin.bottom)"
          + "});";

    @Mock
    private WebJavascriptActions javascriptActions;

    @Mock
    private WebElement webElement;

    @Test
    void shoulGetCoordsViaJsForScrollAdjusted()
    {
        when(javascriptActions.executeScript(String.format(GET_COORDINATES, "window.scrollY || window.scrollTop || "
                + "document.getElementsByTagName('html')[0].scrollTop"), webElement)).thenReturn(RESULT_COORDS);
        assertEquals(new Coords(1, 1, 1, 1), CeilingJsCoordsProvider.getScrollAdjusted(javascriptActions)
                .ofElement(null, webElement));
    }

    @Test
    void shoulGetCoordsViaJsForSimple()
    {
        when(javascriptActions.executeScript(String.format(GET_COORDINATES, "0"), webElement)).thenReturn(
                RESULT_COORDS);
        assertEquals(new Coords(1, 1, 1, 1), CeilingJsCoordsProvider.getSimple(javascriptActions)
                .ofElement(null, webElement));
    }
}

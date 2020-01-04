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

import com.google.gson.Gson;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;

public final class CeilingJsCoordsProvider extends CoordsProvider
{
    private static final long serialVersionUID = 681633181202991672L;

    private final String getCoordinatesJs;

    private transient IJavascriptActions javascriptActions;

    private CeilingJsCoordsProvider(String scrollTopJs, IJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
        getCoordinatesJs = String.format("var rect = arguments[0].getBoundingClientRect();"
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
                + "return JSON.stringify("
                + "{"
                + "  x: Math.ceil(rect.x),"
                + "  y: Math.ceil(rect.y + scrollTop),"
                + "  width: Math.trunc(rect.width + margin.left + margin.right),"
                + "  height: Math.trunc(rect.height + margin.top + margin.bottom)"
                + "});", scrollTopJs);
    }

    public static CeilingJsCoordsProvider getSimple(IJavascriptActions javascriptActions)
    {
        return new CeilingJsCoordsProvider("0", javascriptActions);
    }

    public static CeilingJsCoordsProvider getScrollAdjusted(IJavascriptActions javascriptActions)
    {
        return new CeilingJsCoordsProvider("window.scrollY || window.scrollTop || "
                + "document.getElementsByTagName('html')[0].scrollTop", javascriptActions);
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        String rect = javascriptActions.executeScript(getCoordinatesJs, element);
        return new Gson().fromJson(rect, Coords.class);
    }
}

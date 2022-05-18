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

package org.vividus.ui.web.util;

import org.openqa.selenium.By;
import org.vividus.ui.util.XpathLocatorUtils;

public final class WebXpathLocatorUtils
{
    private static final String ANY = "*";

    private WebXpathLocatorUtils()
    {
    }

    public static By getXPathLocatorByInnerTextWithTagName(String tagName, String text)
    {
        return By.xpath(getXPathByInnerTextWithTagName(tagName, text));
    }

    public static By getXPathLocatorByFullInnerText(String text)
    {
        return By.xpath(XpathLocatorUtils.getXPath(true,
            String.format(".//%1$s[.=%%1$s and not(.//%1$s[.=%%1$s])]", ANY), text));
    }

    public static By getXPathLocatorByInnerText(String text)
    {
        return By.xpath(getXPathByInnerTextWithTagName(ANY, text));
    }

    private static String getXPathByInnerTextWithTagName(String tagName, String text)
    {
        return XpathLocatorUtils.getXPath(true,
                String.format(".//%1$s[contains(., %%1$s) and not(.//%1$s[contains(., %%1$s)])]", tagName), text);
    }
}

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

package org.vividus.ui.web.playwright.locator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PlaywrightLocatorTypeTests
{
    private static final String CSS_CLASS_NAME_LOCATOR = ".class";

    @Test
    void shouldCreateLocatorByCss()
    {
        var cssSelector = CSS_CLASS_NAME_LOCATOR;
        assertEquals(createCssLocator(cssSelector), PlaywrightLocatorType.CSS.createLocator(cssSelector));
    }

    @Test
    void shouldCreateLocatorByClassName()
    {
        var className = "class";
        assertEquals(createCssLocator(CSS_CLASS_NAME_LOCATOR),
                PlaywrightLocatorType.CLASS_NAME.createLocator(className));
    }

    @Test
    void shouldCreateLocatorByXpath()
    {
        var xpath = "//div";
        assertEquals(createXpathLocator(xpath), PlaywrightLocatorType.XPATH.createLocator(xpath));
    }

    @Test
    void shouldCreateLocatorById()
    {
        var id = "id_locator";
        assertEquals(new PlaywrightLocator("id", id), PlaywrightLocatorType.ID.createLocator(id));
    }

    @Test
    void shouldCreateLocatorByTagName()
    {
        var tagName = "div";
        assertEquals(createCssLocator(tagName), PlaywrightLocatorType.TAG_NAME.createLocator(tagName));
    }

    @Test
    void shouldCreateLocatorByLinkText()
    {
        var expectedXpath = ".//a[text()='linkText' or @*='linkText' or *='linkText']";
        assertEquals(createXpathLocator(expectedXpath), PlaywrightLocatorType.LINK_TEXT.createLocator("linkText"));
    }

    @Test
    void shouldCreateLocatorByLinkUrl()
    {
        var linkUrl = "linkUrl";
        assertEquals(createCssLocator("a[href='linkUrl']"), PlaywrightLocatorType.LINK_URL.createLocator(linkUrl));
    }

    @Test
    void shouldCreateLocatorByLinkUrlPart()
    {
        var linkUrlPart = "linkUrlPart";
        assertEquals(createCssLocator("a[href*='linkUrlPart']"),
                PlaywrightLocatorType.LINK_URL_PART.createLocator(linkUrlPart));
    }

    @Test
    void shouldCreateLocatorByImageSrc()
    {
        var imageSrc = "/image/src";
        assertEquals(createCssLocator("img[src='/image/src']"),
                PlaywrightLocatorType.IMAGE_SRC.createLocator(imageSrc));
    }

    @Test
    void shouldCreateLocatorByImageSrcPart()
    {
        var imageSrcPart = "src";
        assertEquals(createCssLocator("img[src*='src']"),
                PlaywrightLocatorType.IMAGE_SRC_PART.createLocator(imageSrcPart));
    }

    @Test
    void shouldCreateLocatorByElementName()
    {
        var elementName = "elementName";
        assertEquals(createXpathLocator(".//*[@*='elementName' or text()='elementName']"),
                PlaywrightLocatorType.NAME.createLocator(elementName));
    }

    @Test
    void shouldCreateLocatorByFieldName()
    {
        var fieldName = "fieldName";
        var expectedXpath = ".//*[(local-name() = 'input' or local-name() = 'textarea' or local-name()='body') and "
                            + "((@* | text())='fieldName' or @id=(//label[text() = 'fieldName']/@for))]";
        assertEquals(createXpathLocator(expectedXpath), PlaywrightLocatorType.FIELD_NAME.createLocator(fieldName));
    }

    @Test
    void shouldCreateLocatorByRadioButton()
    {
        var radioButton = "radioButton";
        var expectedXpath = ".//input[@type='radio' and ((@* | text())='radioButton' or "
                            + "@id=(//label[text() = 'radioButton']/@for))]";
        assertEquals(createXpathLocator(expectedXpath), PlaywrightLocatorType.RADIO_BUTTON.createLocator(radioButton));
    }

    private PlaywrightLocator createXpathLocator(String value)
    {
        return new PlaywrightLocator("xpath", value);
    }

    private PlaywrightLocator createCssLocator(String value)
    {
        return new PlaywrightLocator("css", value);
    }
}

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

package org.vividus.ui.web.action.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import com.codeborne.selenide.selector.ByShadow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.vividus.ui.action.search.ByLocatorSearch;

class WebLocatorTypeTests
{
    @Test
    void shouldBuildByCssSelector()
    {
        var cssSelector = ".class";
        assertEquals(By.cssSelector(cssSelector), WebLocatorType.CSS_SELECTOR.buildBy(cssSelector));
    }

    @Test
    void shouldBuildByClassName()
    {
        var className = "class";
        assertEquals(By.className(className), WebLocatorType.CLASS_NAME.buildBy(className));
    }

    @Test
    void shouldBuildByXpath()
    {
        var xpath = "//xpath";
        assertEquals(By.xpath(xpath), WebLocatorType.XPATH.buildBy(xpath));
    }

    @Test
    void shouldBuildById()
    {
        var id = "id";
        assertEquals(By.id(id), WebLocatorType.ID.buildBy(id));
    }

    @Test
    void shouldBuildByImageSrc()
    {
        var imageSrc = "img.png";
        assertEquals(By.xpath(".//img[normalize-space(@src)=\"img.png\"]"), WebLocatorType.IMAGE_SRC.buildBy(imageSrc));
    }

    @Test
    void shouldBuildByImageSrcPart()
    {
        var imageSrcPart = "a.png";
        assertEquals(By.xpath(".//img[contains(normalize-space(@src),\"a.png\")]"),
                WebLocatorType.IMAGE_SRC_PART.buildBy(imageSrcPart));
    }

    @Test
    void shouldBuildByTagName()
    {
        var tagName = "div";
        assertEquals(By.tagName(tagName), WebLocatorType.TAG_NAME.buildBy(tagName));
    }

    @Test
    void shouldBuildByPartialLinkText()
    {
        var value = "text";
        assertEquals(By.xpath(".//a[contains(normalize-space(.), \"text\") and not(.//a[contains(normalize-space(.), "
                + "\"text\")])]"), WebLocatorType.PARTIAL_LINK_TEXT.buildBy(value));
    }

    @Test
    void shouldBuildByFieldName()
    {
        var value = "field";
        assertEquals(By.xpath(".//*[(local-name() = 'input' or local-name() = 'textarea' or local-name()='body') and "
                + "((@* | text())=\"field\")]"), WebLocatorType.FIELD_NAME.buildBy(value));
    }

    @Test
    void shouldBuildByCaseInsensitiveText()
    {
        var value = "locator";
        var expected = By.xpath(
                ".//*[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))"
                        + "=\"locator\"] or @*[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', "
                        + "'abcdefghijklmnopqrstuvwxyz'))=\"locator\"] or *[normalize-space(translate(., "
                        + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=\"locator\"] and not("
                        + ".//*[text()"
                        + "[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))="
                        + "\"locator\"] or @*[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', "
                        + "'abcdefghijklmnopqrstuvwxyz'))=\"locator\"] or *[normalize-space(translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))=\"locator\"]])]");
        assertEquals(expected, WebLocatorType.CASE_INSENSITIVE_TEXT.buildBy(value));
    }

    @Test
    void shouldBuildByTooltip()
    {
        var tooltip = "tooltip";
        assertEquals(By.xpath(".//*[normalize-space(@title)=\"tooltip\"]"), WebLocatorType.TOOLTIP.buildBy(tooltip));
    }

    @Test
    void shouldBuildByShadowDomCss()
    {
        assertEquals(ByShadow.cssSelector("h1", "div1", "div2"),
                WebLocatorType.SHADOW_CSS_SELECTOR.buildBy("div1; div2; h1"));
    }

    static Stream<Arguments> dataSet()
    {
        return Stream.of(
                arguments(WebLocatorType.ID, "Id", ByLocatorSearch.class),
                arguments(WebLocatorType.TAG_NAME, "Tag name", ByLocatorSearch.class)
        );
    }

    @MethodSource("dataSet")
    @ParameterizedTest
    void testWebAttributeType(WebLocatorType type, String name, Class<?> actionClass)
    {
        assertEquals(type.getAttributeName(), name);
        assertEquals(type.getActionClass(), actionClass);
        assertThat(type.getCompetingTypes(), empty());
        assertEquals(type.name(), type.getKey());
    }
}

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

package org.vividus.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;

@SuppressWarnings("checkstyle:methodcount")
class LocatorUtilTests
{
    private static final String FIELD_WITH_VALUE_PATTERN = ".//*[(local-name() = 'input' or local-name() = 'textarea'"
            + " or local-name()='body') and @*='%1$s' and (@value='%2$s' or text()='%2$s')]";

    private static final String LOCATOR_WITH_PARENT_NODE = "//*[.//@id='main-content']//h1[text()]";
    private static final String BUTTON_WITH_ANY_ATTRIBUTE_NAME_PART = "*[(local-name()='button' and "
            + "(@*='%1$s' or text()='%1$s')) or (local-name()='input' and ((@type='submit' or "
            + "'button') and (@*='%1$s' or text()='%1$s')))]";
    private static final String BUTTON_WITH_ANY_ATTRIBUTE_NAME = ".//" + BUTTON_WITH_ANY_ATTRIBUTE_NAME_PART;
    private static final String DIV_WITH_CHILD_BUTTON = ".//div[" + BUTTON_WITH_ANY_ATTRIBUTE_NAME_PART + "]";
    private static final String LINK_WITH_ANY_ATTRIBUTE_OR_TEXT = ".//a[text()='%1$s' or *='%1$s']";
    private static final String SECTION_PATTERN = ".//*[text()='%1$s'"
            + " and (local-name()='h1' or local-name()='h2' or local-name()='h3')]/../..";
    private static final String LINK_IMAGE_TOOLTIP_PATTERN = ".//a[./img[@alt='%1$s' or @title='%1$s']]";
    private static final String FIELD_WITH_ANY_ATTRIBUTE_NAME_PATTERN = ".//*[(local-name() = 'input' "
            + "or local-name() = 'textarea' or local-name()='body') and (@*='%1$s' or text()='%1$s')]";
    private static final String ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN = ".//*[@*='%1$s' or text()='%1$s']";
    private static final String TRANSLATE_TO_LOWER_CASE = "translate(.,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    private static final String ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE = ".//[text()["
            + TRANSLATE_TO_LOWER_CASE + "='%1$s'] or *[" + TRANSLATE_TO_LOWER_CASE + "='%1$s']]";
    private static final String XPATH_PART_2 = " text()[normalize-space()='text %'])) or (local-name()='input'"
            + " and ((@type[normalize-space()='submit']";
    private static final String XPATH_INPUT_TEXTAREA = ".//*[(local-name() = 'input' or local-name() = 'textarea' or";
    private static final String FULL_INNER_TEXT_XPATH_FORMAT = ".//%1$s[normalize-space(.)=\"%2$s\" and "
            + "not(.//%1$s[normalize-space(.)=\"%2$s\"])]";
    private static final String INNER_TEXT_XPATH_FORMAT = ".//%1$s[contains(normalize-space(.), \"%2$s\") and "
            + "not(.//%1$s[contains(normalize-space(.), \"%2$s\")])]";
    private static final String ANY_ELEMENT = "*";
    private static final String TEXT = "text";
    private static final String TEXT_WITH_PERCENT = "text %";
    private static final String TEXT_WITH_NON_BREAKING_SPACE = "text with space";
    private static final String A = "a";
    private static final String ATTR = "attr";

    @Test
    void testGetXPathLocatorByInnerTextWithTagName()
    {
        String tagName = "tagName";
        By expectedLocator = By.xpath(String.format(INNER_TEXT_XPATH_FORMAT, tagName, TEXT_WITH_PERCENT));
        By actualLocator = LocatorUtil.getXPathLocatorByInnerTextWithTagName(tagName, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByFullInnerText()
    {
        By expectedLocator = By.xpath(String.format(FULL_INNER_TEXT_XPATH_FORMAT, ANY_ELEMENT, TEXT_WITH_PERCENT));
        By actualLocator = LocatorUtil.getXPathLocatorByFullInnerText(TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByFullInnerTextWithBothQuotes()
    {
        By expectedLocator = By.xpath(".//*[normalize-space(.)=concat(\"This 'text' with both \", '\"', \"quotes\", "
                + "'\"', \" %\") and not(.//*[normalize-space(.)=concat(\"This 'text' with both \", '\"', \"quotes\", "
                + "'\"', \" %\")])]");
        String text = "This 'text' with both \"quotes\" %";
        By actualLocator = LocatorUtil.getXPathLocatorByFullInnerText(text);
        assertEquals(expectedLocator.toString(), actualLocator.toString());
    }

    @Test
    void testGetXPathLocatorByInnerTextWithoutTagName()
    {
        By expectedLocator = By.xpath(String.format(INNER_TEXT_XPATH_FORMAT, ANY_ELEMENT, TEXT_WITH_PERCENT));
        By actualLocator = LocatorUtil.getXPathLocatorByInnerText(TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorByInnerTextWithoutTagNameAndWithSingleQuote()
    {
        By expectedLocator = By.xpath(".//*[contains(normalize-space(.), concat(\"This action rebuilds your site's XML "
                + "sitemap and regenerates the cached files, and may be a lengthy process. If you just installed \", "
                + "'\"', \"XML sitemap\", '\"', \", this can be helpful to import all your site's content into the "
                + "sitemap. Otherwise, this should only be used in emergencies. %\")) and not(.//*[contains("
                + "normalize-space(.), concat(\"This action rebuilds your site's XML sitemap and regenerates the cached"
                + " files, and may be a lengthy process. If you just installed \", '\"', \"XML sitemap\", '\"', \", "
                + "this can be helpful to import all your site's content into the sitemap. Otherwise, this should "
                + "only be used in emergencies. %\"))])]");
        String text = "This action rebuilds your site's XML sitemap and regenerates the cached files, "
                + "and may be a lengthy process. If you just installed \"XML sitemap\", this can be helpful to import "
                + "all your site's content into the sitemap. Otherwise, this should only be used in emergencies. %";
        By actualLocator = LocatorUtil.getXPathLocatorByInnerText(text);
        assertEquals(expectedLocator.toString(), actualLocator.toString());
    }

    @Test
    void testGetXpathPatternWithNormalization()
    {
        By expectedLocator = By.xpath(".//*[@*[normalize-space()='attr'] or text()[normalize-space()='attr']]");
        By actualLocator = LocatorUtil.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN, ATTR);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithoutNormalization()
    {
        By expectedLocator = By.xpath(".//*[@*='text %' or text()='text %']");
        By actualLocator = LocatorUtil.getXPathLocator(false, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
                TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalizationAndTraslateFromWebElement()
    {
        By expectedLocator = By.xpath(".//a[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'))=\"what's great about it\"]"
                + " or *[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'))=\"what's great about it\"]]");
        By actualLocator = LocatorUtil.getXPathLocator(
                ".//a[text()[translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '%1$s'] or *[translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '%1$s']]",
                "what's great about it");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGenerateXpathWithQuites()
    {
        By expectedLocator = By.xpath(".//div[contains(normalize-space(@class),\"error\") and contains"
                + "(normalize-space(.),\"That choice is invalid. If registration is required then "
                + "'Display on Registration' must be enabled.\")]");
        By actualLocator = LocatorUtil.getXPathLocator(".//div[contains(@class,%1$s) and contains(.,%2$s)]", "error",
                "That choice is invalid. If registration is required then 'Display on Registration' must be enabled.",
                true);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization1()
    {
        By expectedLocator = By.xpath(".//*[(local-name()='button' and (@*[normalize-space()='text %'] or"
                + XPATH_PART_2
                + " or 'button') and (@*[normalize-space()='text %'] or text()[normalize-space()='text %'])))]");
        By actualLocator = LocatorUtil.getXPathLocator(true, BUTTON_WITH_ANY_ATTRIBUTE_NAME, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization2()
    {
        By expectedLocator = By
                .xpath(".//div[*[(local-name()='button' and (@*[normalize-space()='text %'] or" + XPATH_PART_2
                    + " or 'button') and (@*[normalize-space()='text %'] or text()[normalize-space()='text %'])))]]");
        By actualLocator = LocatorUtil.getXPathLocator(true, DIV_WITH_CHILD_BUTTON, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization3()
    {
        By expectedLocator = By.xpath(".//a[text()[normalize-space()='text %'] or *[normalize-space()='text %']]");
        By actualLocator = LocatorUtil.getXPathLocator(true, LINK_WITH_ANY_ATTRIBUTE_OR_TEXT, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization4()
    {
        By expectedLocator = By.xpath(".//*[text()[normalize-space()='text %'] and (local-name()='h1' or"
                + " local-name()='h2' or local-name()='h3')]/../..");
        By actualLocator = LocatorUtil.getXPathLocator(true, SECTION_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization5()
    {
        By expectedLocator = By
                .xpath(".//a[./img[@alt[normalize-space()='text %']" + " or @title[normalize-space()='text %']]]");
        By actualLocator = LocatorUtil.getXPathLocator(true, LINK_IMAGE_TOOLTIP_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization6()
    {
        By expectedLocator = By.xpath(XPATH_INPUT_TEXTAREA
                + " local-name()='body') and (@*[normalize-space()='text %'] or text()[normalize-space()='text %'])]");
        By actualLocator = LocatorUtil.getXPathLocator(true, FIELD_WITH_ANY_ATTRIBUTE_NAME_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization7()
    {
        By expectedLocator = By.xpath(".//*[@*[normalize-space()='text %'] or text()[normalize-space()='text %']]");
        By actualLocator = LocatorUtil.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
                TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization8()
    {
        By expectedLocator = By.xpath(XPATH_INPUT_TEXTAREA
                + " local-name()='body') and @*[normalize-space()='submit'] and (@value[normalize-space()='text']"
                + " or text()[normalize-space()='text'])]");
        By actualLocator = LocatorUtil.getXPathLocator(true, FIELD_WITH_VALUE_PATTERN, "submit", TEXT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization9()
    {
        By expectedLocator = By.xpath(".//[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'))='text %'] or *[normalize-space(translate(.,"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='text %']]");
        By actualLocator = LocatorUtil.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE,
                TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization10()
    {
        By expectedLocator = By.xpath(".//a[text()[normalize-space()=\"Read Anne-Catherine's Story %\"] "
                + "or *[normalize-space()=\"Read Anne-Catherine's Story %\"]");
        By actualLocator = LocatorUtil.getXPathLocator(".//a[text()=%1$s or *=%1$s", "Read Anne-Catherine's Story %");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathByAttributeWithSpecialSymbol()
    {
        String expectedLocator = ".//*[@text[normalize-space()=\"How & When to Use 1%\"] or "
                + "@text[normalize-space()=\"How &amp; When to Use 1%\"]]";
        assertEquals(expectedLocator, LocatorUtil.getXPathByAttribute(TEXT, "How & When to Use 1%"));
    }

    @Test
    void testGetXPathByAttributeWithoutSpecialSymbol()
    {
        assertEquals(".//*[normalize-space(@text)=\"When to Use 1%\"]",
                LocatorUtil.getXPathByAttribute(TEXT, "When to Use 1%"));
    }

    @Test
    void testGetXPathByTagNameAndAttributeNameAndAttributeValue()
    {
        assertEquals(".//p[normalize-space(@attr)=\"x\"]", LocatorUtil.getXPathByTagNameAndAttribute("p", ATTR, "x"));
    }

    @Test
    void testGetXPathLocatorWithParentNode()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@id)='main-content']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator(LOCATOR_WITH_PARENT_NODE);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorWithParentNodeNormalizationOff()
    {
        By expectedLocator = By.xpath(LOCATOR_WITH_PARENT_NODE);
        By actualLocator = LocatorUtil.getXPathLocator(false, LOCATOR_WITH_PARENT_NODE);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode2()
    {
        By expectedLocator = By.xpath("//*[normalize-space(./@a-b.c_de2)='main-content']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator("//*[./@a-b.c_de2='main-content']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode3()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@id)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator("//*[.//@id='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode4()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@a-b.C_de2)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator("//*[.//@a-b.C_de2='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode5()
    {
        By expectedLocator = By.xpath("//*[normalize-space(//a/@id)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator("//*[//a/@id='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode6()
    {
        By expectedLocator = By.xpath("//*[normalize-space(./a/@a-b.c_de2)='main-content a-b.c_de2']//h1[text()]");
        By actualLocator = LocatorUtil.getXPathLocator("//*[./a/@a-b.c_de2='main-content a-b.c_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathWithNonBreakingSpace()
    {
        By expectedLocator = By.xpath(
                ".//*[@*[normalize-space()=\"text with space\"] or text()[normalize-space()=\"text with space\"]]");
        By actualLocator = LocatorUtil.getXPathLocator(ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
                TEXT_WITH_NON_BREAKING_SPACE);
        assertEquals(expectedLocator, actualLocator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"//*[@href='']", "//*[@class=\"\"]"})
    void testDoNotNormalizeSpaceForEmptyAttributeValue(String xpath)
    {
        By expectedLocator = By.xpath(xpath);
        By actualLocator = LocatorUtil.getXPathLocator(xpath);
        assertEquals(expectedLocator, actualLocator);
    }
}

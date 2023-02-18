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

package org.vividus.ui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;

@SuppressWarnings("checkstyle:methodcount")
class XpathLocatorUtilsTests
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
    private static final String TEXT = "text";
    private static final String TEXT_WITH_PERCENT = "text %";
    private static final String TEXT_WITH_NON_BREAKING_SPACE = "text with space";
    private static final String ATTR = "attr";

    @Test
    void testGetXpathPatternWithNormalization()
    {
        By expectedLocator = By.xpath(".//*[@*[normalize-space()='attr'] or text()[normalize-space()='attr']]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN, ATTR);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithoutNormalization()
    {
        By expectedLocator = By.xpath(".//*[@*='text %' or text()='text %']");
        By actualLocator = XpathLocatorUtils.getXPathLocator(false, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
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
        By actualLocator = XpathLocatorUtils.getXPathLocator(
                ".//a[text()[translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '%1$s'] or *[translate(.,"
                        + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz') = '%1$s']]",
                "what's great about it");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalizationAndSequenceOfNestedFunctionsWithContains()
    {
        By expectedLocator = By.xpath("//*[contains(normalize-space(translate(@class, \"abcdefghijklmnopqrstuvwxyz\", "
                + "\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\")), 'MAIN') "
                + "or contains(normalize-space(translate(concat(text(), 'page'), \"abcdefghijklmnopqrstuvwxyz\", "
                + "\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\")), 'MAIN PAGE')]"
                + "//*[contains(normalize-space(text()), 'Tachi')]"
                + "/parent::body/label[contains(normalize-space(text()), 'MCRN')]");
        By actualLocator = XpathLocatorUtils.getXPathLocator("//*[contains(translate(@class, %1$s, %2$s), 'MAIN') "
                + "or contains(translate(concat(text(), 'page'), %1$s, %2$s), 'MAIN PAGE')]"
                        + "//*[contains(text(), 'Tachi')]/parent::body/label[contains(text(), 'MCRN')]",
                "abcdefghijklmnopqrstuvwxyz", "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGenerateXpathWithQuites()
    {
        By expectedLocator = By.xpath(".//div[contains(normalize-space(@class),\"error\") and contains"
                + "(normalize-space(.),\"That choice is invalid. If registration is required then "
                + "'Display on Registration' must be enabled.\")]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(".//div[contains(@class,%1$s) and contains(.,%2$s)]",
                "error",
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
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, BUTTON_WITH_ANY_ATTRIBUTE_NAME, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization2()
    {
        By expectedLocator = By
                .xpath(".//div[*[(local-name()='button' and (@*[normalize-space()='text %'] or" + XPATH_PART_2
                    + " or 'button') and (@*[normalize-space()='text %'] or text()[normalize-space()='text %'])))]]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, DIV_WITH_CHILD_BUTTON, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization3()
    {
        By expectedLocator = By.xpath(".//a[text()[normalize-space()='text %'] or *[normalize-space()='text %']]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, LINK_WITH_ANY_ATTRIBUTE_OR_TEXT, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization4()
    {
        By expectedLocator = By.xpath(".//*[text()[normalize-space()='text %'] and (local-name()='h1' or"
                + " local-name()='h2' or local-name()='h3')]/../..");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, SECTION_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization5()
    {
        By expectedLocator = By
                .xpath(".//a[./img[@alt[normalize-space()='text %']" + " or @title[normalize-space()='text %']]]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, LINK_IMAGE_TOOLTIP_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization6()
    {
        By expectedLocator = By.xpath(XPATH_INPUT_TEXTAREA
                + " local-name()='body') and (@*[normalize-space()='text %'] or text()[normalize-space()='text %'])]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true,
                FIELD_WITH_ANY_ATTRIBUTE_NAME_PATTERN, TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization7()
    {
        By expectedLocator = By.xpath(".//*[@*[normalize-space()='text %'] or text()[normalize-space()='text %']]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
                TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization8()
    {
        By expectedLocator = By.xpath(XPATH_INPUT_TEXTAREA
                + " local-name()='body') and @*[normalize-space()='submit'] and (@value[normalize-space()='text']"
                + " or text()[normalize-space()='text'])]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, FIELD_WITH_VALUE_PATTERN, "submit", TEXT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization9()
    {
        By expectedLocator = By.xpath(".//[text()[normalize-space(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ',"
                + " 'abcdefghijklmnopqrstuvwxyz'))='text %'] or *[normalize-space(translate(.,"
                + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'))='text %']]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(true, ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE,
                TEXT_WITH_PERCENT);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathPatternWithNormalization10()
    {
        By expectedLocator = By.xpath(".//a[text()[normalize-space()=\"Read Anne-Catherine's Story %\"] "
                + "or *[normalize-space()=\"Read Anne-Catherine's Story %\"]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(".//a[text()=%1$s or *=%1$s",
                "Read Anne-Catherine's Story %");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathByAttributeWithSpecialSymbol()
    {
        String expectedLocator = ".//*[@text[normalize-space()=\"How & When to Use 1%\"] or "
                + "@text[normalize-space()=\"How &amp; When to Use 1%\"]]";
        assertEquals(expectedLocator, XpathLocatorUtils.getXPathByAttribute(TEXT, "How & When to Use 1%"));
    }

    @Test
    void testGetXPathByAttributeWithoutSpecialSymbol()
    {
        assertEquals(".//*[normalize-space(@text)=\"When to Use 1%\"]",
                XpathLocatorUtils.getXPathByAttribute(TEXT, "When to Use 1%"));
    }

    @Test
    void testGetXPathByTagNameAndAttributeNameAndAttributeValue()
    {
        assertEquals(".//p[normalize-space(@attr)=\"x\"]", XpathLocatorUtils.getXPathByTagNameAndAttribute("p",
                ATTR, "x"));
    }

    @Test
    void testGetXPathLocatorWithParentNode()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@id)='main-content']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(LOCATOR_WITH_PARENT_NODE);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorWithParentNodeNormalizationOff()
    {
        By expectedLocator = By.xpath(LOCATOR_WITH_PARENT_NODE);
        By actualLocator = XpathLocatorUtils.getXPathLocator(false, LOCATOR_WITH_PARENT_NODE);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode2()
    {
        By expectedLocator = By.xpath("//*[normalize-space(./@a-b.c_de2)='main-content']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator("//*[./@a-b.c_de2='main-content']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode3()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@id)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator("//*[.//@id='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode4()
    {
        By expectedLocator = By.xpath("//*[normalize-space(.//@a-b.C_de2)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator("//*[.//@a-b.C_de2='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode5()
    {
        By expectedLocator = By.xpath("//*[normalize-space(//a/@id)='main-content a-b.C_de2']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator("//*[//a/@id='main-content a-b.C_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXPathLocatorThroughParentNode6()
    {
        By expectedLocator = By.xpath("//*[normalize-space(./a/@a-b.c_de2)='main-content a-b.c_de2']//h1[text()]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(
            "//*[./a/@a-b.c_de2='main-content a-b.c_de2']//h1[text()]");
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void testGetXpathWithNonBreakingSpace()
    {
        By expectedLocator = By.xpath(
                ".//*[@*[normalize-space()=\"text with space\"] or text()[normalize-space()=\"text with space\"]]");
        By actualLocator = XpathLocatorUtils.getXPathLocator(ELEMENT_WITH_ANY_ATTRIBUTE_NAME_PATTERN,
                TEXT_WITH_NON_BREAKING_SPACE);
        assertEquals(expectedLocator, actualLocator);
    }

    @Test
    void shouldGenerateValidXpath()
    {
        assertEquals(".//*[@*[normalize-space()=concat(\"Don't be a \", '\"', \"fool\", '\"')] or text()"
                + "[normalize-space()=concat(\"Don't be a \", '\"', \"fool\", '\"')]]",
            XpathLocatorUtils.getXPath(".//*[@*='%s' or text()=\"%1$s\"]", "Don't be a \"fool\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"//*[@href='']", "//*[@class=\"\"]"})
    void testDoNotNormalizeSpaceForEmptyAttributeValue(String xpath)
    {
        By expectedLocator = By.xpath(xpath);
        By actualLocator = XpathLocatorUtils.getXPathLocator(xpath);
        assertEquals(expectedLocator, actualLocator);
    }
}

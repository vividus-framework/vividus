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

package org.vividus.bdd.steps.ui.web;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.TextNode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.model.JsArgument;
import org.vividus.bdd.steps.ui.web.model.JsArgumentType;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class CodeStepsTests
{
    private static final String JS_ARGUMENT_ERROR_MESSAGE = "Please, specify command argument values and types";
    private static final String BODY = "body";
    private static final String FAVICON = "Favicon";
    private static final String FIELD_VALUE_FROM_THE_JSON_OBJECT_IS_EQUAL_TO_VALUE =
            "Field value from the JSON object is equal to 'value'";
    private static final String FIELD_WITH_THE_NAME_NAME_WAS_FOUND_IN_THE_JSON_OBJECT =
            "Field with the name 'name' was found in the JSON object";
    private static final String EXISTS = " exists";
    private static final String THE_FAVICON_WITH_THE_SRC_CONTAINING = "The favicon with the src containing ";
    private static final String SCRIPT = "script";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String FAVICON_CONTAINS_HREF_ATTRIBUTE = "Favicon contains 'href' attribute";
    private static final String SITE = "www.promo1dev/site/";
    private static final String HREF = "href";
    private static final String HEAD_LINK_CONTAINS_REL_SHORTCUT_ICON_ICON = "//head/link[@rel='shortcut icon' "
            + "or @rel='icon']";
    private static final String FAVICON_IMG_PNG = "faviconImg.png";
    private static final String XPATH = "xpath";

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IBaseValidations mockedBaseValidations;

    @Mock
    private WebElement mockedWebElement;

    @Mock
    private WebJavascriptActions javascriptActions;

    @InjectMocks
    private CodeSteps codeSteps;

    @Test
    void testCheckJsonFieldValue() throws IOException
    {
        mockScriptActions(true);
        codeSteps.checkJsonFieldValue(SCRIPT, NAME, VALUE);
        verify(softAssert).assertNotNull(FIELD_WITH_THE_NAME_NAME_WAS_FOUND_IN_THE_JSON_OBJECT,
                TextNode.valueOf(VALUE));
        verify(softAssert).assertEquals(FIELD_VALUE_FROM_THE_JSON_OBJECT_IS_EQUAL_TO_VALUE, VALUE, VALUE);
    }

    @Test
    void testCheckJsonFieldValueNull() throws IOException
    {
        mockScriptActions(false);
        codeSteps.checkJsonFieldValue(SCRIPT, NAME, VALUE);
        verify(softAssert).assertNotNull(FIELD_WITH_THE_NAME_NAME_WAS_FOUND_IN_THE_JSON_OBJECT,
                TextNode.valueOf(VALUE));
        verify(softAssert, never())
                .assertEquals(FIELD_VALUE_FROM_THE_JSON_OBJECT_IS_EQUAL_TO_VALUE,
                VALUE, VALUE);
    }

    @Test
    void testFaviconExistsUrlPartExists()
    {
        String hrefAttribute = SITE + FAVICON_IMG_PNG;
        mockFavicon(hrefAttribute);
        when(softAssert.assertNotNull(FAVICON_CONTAINS_HREF_ATTRIBUTE, hrefAttribute)).thenReturn(true);
        codeSteps.ifFaviconWithSrcExists(FAVICON_IMG_PNG);
        verify(softAssert).assertNotNull(FAVICON_CONTAINS_HREF_ATTRIBUTE, hrefAttribute);
        verify(softAssert).assertThat(eq(THE_FAVICON_WITH_THE_SRC_CONTAINING + FAVICON_IMG_PNG + EXISTS),
                eq(hrefAttribute), any());
    }

    @Test
    void testFaviconExistsUrlPartDifferent()
    {
        String hrefAttribute = SITE;
        mockFavicon(hrefAttribute);
        when(softAssert.assertNotNull(FAVICON_CONTAINS_HREF_ATTRIBUTE, hrefAttribute)).thenReturn(true);
        codeSteps.ifFaviconWithSrcExists(FAVICON_IMG_PNG);
        verify(softAssert).assertThat(eq(THE_FAVICON_WITH_THE_SRC_CONTAINING + FAVICON_IMG_PNG + EXISTS),
                eq(hrefAttribute), any());
    }

    @Test
    void testFaviconDoesntExist()
    {
        codeSteps.ifFaviconWithSrcExists(FAVICON_IMG_PNG);
        verifyNoInteractions(softAssert);
        verify(mockedBaseValidations).assertIfElementExists(FAVICON, new Locator(WebLocatorType.XPATH,
                new SearchParameters(LocatorUtil.getXPath(HEAD_LINK_CONTAINS_REL_SHORTCUT_ICON_ICON, FAVICON_IMG_PNG),
                        Visibility.ALL)));
    }

    @Test
    void testFaviconExistWithNoHref()
    {
        mockFavicon(SITE + FAVICON_IMG_PNG);
        when(softAssert.assertNotNull(FAVICON_CONTAINS_HREF_ATTRIBUTE, SITE + FAVICON_IMG_PNG))
                .thenReturn(false);
        codeSteps.ifFaviconWithSrcExists(FAVICON_IMG_PNG);
        verify(softAssert, never()).assertThat(
                eq(THE_FAVICON_WITH_THE_SRC_CONTAINING + FAVICON_IMG_PNG + EXISTS), eq(SITE),
                containsString(anyString()));
    }

    @Test
    void testDoesInvisibleQuantityOfElementsExists()
    {
        Locator locator = new Locator(WebLocatorType.XPATH,
                    new SearchParameters(XPATH));
        codeSteps.doesInvisibleQuantityOfElementsExists(locator,  ComparisonRule.EQUAL_TO, 1);
        verify(mockedBaseValidations).assertIfNumberOfElementsFound(eq("The number of found invisible elements"),
                    eq(locator), eq(1), eq(ComparisonRule.EQUAL_TO));
    }

    static Stream<Arguments> executeJavascriptWithArguments()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
            Arguments.of("document.querySelector(arguments[0])", createJsArgument(JsArgumentType.STRING, BODY),                        BODY                       ),
            Arguments.of("remote:throttle",                      createJsArgument(JsArgumentType.OBJECT, "{\"condition\": \"Wifi\"}"), Map.of("condition", "Wifi"))
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("executeJavascriptWithArguments")
    void testExecuteJavascriptWithStringArguments(String jsCode, JsArgument argument, Object arg)
    {
        codeSteps.executeJavascriptWithArguments(jsCode, Collections.singletonList(argument));
        verify(javascriptActions).executeScript(jsCode, arg);
    }

    @Test
    void testExecuteJavascriptWithEmptyArguments()
    {
        String jsCode = "document.readyState";
        codeSteps.executeJavascriptWithArguments(jsCode, List.of());
        verify(javascriptActions).executeScript(jsCode);
    }

    @ParameterizedTest
    @CsvSource({
        ",       body",
        "OBJECT,     "
    })
    void testExecuteJavascriptWithArgumentsNoType(JsArgumentType type, String value)
    {
        JsArgument argument = createJsArgument(type, value);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> codeSteps.executeJavascriptWithArguments("document.querySelector(arguments[0])",
                    Collections.singletonList(argument)));
        assertEquals(JS_ARGUMENT_ERROR_MESSAGE, exception.getMessage());
    }

    private static JsArgument createJsArgument(JsArgumentType type, String value)
    {
        JsArgument argument = new JsArgument();
        argument.setType(type);
        argument.setValue(value);
        return argument;
    }

    private void mockScriptActions(boolean isValueExist)
    {
        when(javascriptActions.executeScript("return JSON.stringify(script)")).thenReturn("{\"name\": \"value\"}");
        when(softAssert.assertNotNull(FIELD_WITH_THE_NAME_NAME_WAS_FOUND_IN_THE_JSON_OBJECT,
                TextNode.valueOf(VALUE))).thenReturn(isValueExist);
    }

    private void mockFavicon(String hrefAttribute)
    {
        when(mockedBaseValidations.assertIfElementExists(FAVICON, new Locator(WebLocatorType.XPATH,
                new SearchParameters(LocatorUtil.getXPath(HEAD_LINK_CONTAINS_REL_SHORTCUT_ICON_ICON, FAVICON_IMG_PNG),
                        Visibility.ALL)))).thenReturn(mockedWebElement);
        when(mockedWebElement.getAttribute(HREF)).thenReturn(hrefAttribute);
    }
}

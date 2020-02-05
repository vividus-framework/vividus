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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class SetVariableStepsTests
{
    private static final String VALID_URL = "http://www.example.com/relative/path";
    private static final String PATH = "/relative/path";
    private static final String JS_RESULT_ASSERTION_MESSAGE = "Returned result is not null";
    private static final String JS_CODE = "return 'value'";
    private static final String THE_SRC_VALUE_WAS_FOUND = "The 'src' attribute value was found";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);
    private static final String NAME = "name";
    private static final String VARIABLE = "variable";
    private static final String URL_VARIABLE = "urlVariable";
    private static final String VALUE = "value";
    private static final String NUMBER_BY_XPATH = "numberByXpath";
    private static final SearchAttributes VIDEO_IFRAME_SEARCH = new SearchAttributes(ActionAttributeType.XPATH,
            LocatorUtil.getXPath("div[contains(@class,'video')]/iframe"));
    private static final String TEXT = "text";
    private static final String VARIABLE_NAME = "variableName";
    private static final String XPATH = "xpath";
    private static final String SRC = "src";
    private static final String NUMBER_FOUND_VIDEO_MESSAGE = "The number of found video frames";
    private static final String THE_ELEMENT_TO_EXTRACT_THE_ATTRIBUTE = "The element to extract the attribute";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private IWebElementActions webElementActions;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private WebDriver webDriver;

    @InjectMocks
    private SetVariableSteps setVariableSteps;

    @Test
    void testGettingValueFromUrl()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn("http://testurl.com/testvalue");
        setVariableSteps.gettingValueFromUrl(VARIABLE_SCOPE, VARIABLE);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE, "testvalue");
    }

    @Test
    void testAssertIfValueExistsNoValue()
    {
        testAssertIfValueExistsNoValue("http://testurl.com/");
    }

    @Test
    void testAssertIfValueExistsWhenNoValidUrl()
    {
        testAssertIfValueExistsNoValue("data;");
    }

    private void testAssertIfValueExistsNoValue(String url)
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(url);
        setVariableSteps.gettingValueFromUrl(VARIABLE_SCOPE, VARIABLE);
        verify(softAssert).recordFailedAssertion("Any appropriate value wasn't found in the URL: " + url);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testGetUrlValueOfVideoWithNumber()
    {
        WebElement videoFrame = mock(WebElement.class);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(NUMBER_FOUND_VIDEO_MESSAGE, VIDEO_IFRAME_SEARCH, 1))
                .thenReturn(Collections.singletonList(videoFrame));
        when(videoFrame.getAttribute(SRC)).thenReturn(VALUE);
        when(softAssert.assertNotNull(THE_SRC_VALUE_WAS_FOUND, VALUE)).thenReturn(Boolean.TRUE);
        setVariableSteps.getUrlValueOfVideoWithNumber(1, VARIABLE_SCOPE, URL_VARIABLE);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, URL_VARIABLE, VALUE);
    }

    @Test
    void testGetNullUrlValueOfVideoWithNumber()
    {
        setVariableSteps.getUrlValueOfVideoWithNumber(1, VARIABLE_SCOPE, URL_VARIABLE);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testGetUrlValueOfNullVideoWithNumber()
    {
        setVariableSteps.getUrlValueOfVideoWithNumber(1, VARIABLE_SCOPE, URL_VARIABLE);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testGetUrlValueOfVideoWithName()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement videoFrame = mock(WebElement.class);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(NUMBER_FOUND_VIDEO_MESSAGE, VIDEO_IFRAME_SEARCH, 1))
                .thenReturn(Collections.singletonList(videoFrame));
        when(searchActions.findElements(eq(webDriver), any(SearchAttributes.class)))
                .thenReturn(Collections.singletonList(mock(WebElement.class)));
        when(videoFrame.getAttribute(SRC)).thenReturn(VALUE);
        when(softAssert.assertNotNull(THE_SRC_VALUE_WAS_FOUND, VALUE)).thenReturn(Boolean.TRUE);
        TargetLocator mockedTargetLocator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(mockedTargetLocator);
        when(mockedTargetLocator.frame(videoFrame)).thenReturn(webDriver);
        setVariableSteps.getUrlValueOfVideoWithName(NAME, VARIABLE_SCOPE, URL_VARIABLE);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, URL_VARIABLE, VALUE);
    }

    @Test
    void testGetUrlValueOfNullVideoWithName()
    {
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(NUMBER_FOUND_VIDEO_MESSAGE, VIDEO_IFRAME_SEARCH, 1))
                .thenReturn(List.of());
        setVariableSteps.getUrlValueOfVideoWithName(NAME, VARIABLE_SCOPE, URL_VARIABLE);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testGetUrlValueOfVideoWithNullName()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement videoFrame = mock(WebElement.class);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(NUMBER_FOUND_VIDEO_MESSAGE, VIDEO_IFRAME_SEARCH, 1))
                .thenReturn(Collections.singletonList(videoFrame));
        when(searchActions.findElements(eq(webDriver), any(SearchAttributes.class)))
                .thenReturn(List.of());
        TargetLocator mockedTargetLocator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(mockedTargetLocator);
        when(mockedTargetLocator.frame(videoFrame)).thenReturn(webDriver);
        setVariableSteps.getUrlValueOfVideoWithName(NAME, VARIABLE_SCOPE, URL_VARIABLE);
        verify(softAssert).recordFailedAssertion("A video with the " + NAME + " 'name' was not found");
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testCheckNumberOfOpenWindow()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getWindowHandles()).thenReturn(Collections.singleton(VARIABLE));
        setVariableSteps.saveNumberOfOpenWindow(VARIABLE_SCOPE, VARIABLE);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE, 1);
    }

    @Test
    void testGetNullUrlValueOfVideoWithName()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement videoFrame = mock(WebElement.class);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(NUMBER_FOUND_VIDEO_MESSAGE, VIDEO_IFRAME_SEARCH, 1))
                .thenReturn(Collections.singletonList(videoFrame));
        when(searchActions.findElements(eq(webDriver), any(SearchAttributes.class)))
                .thenReturn(Collections.singletonList(mock(WebElement.class)));
        when(videoFrame.getAttribute(SRC)).thenReturn(null);
        when(softAssert.assertNotNull(THE_SRC_VALUE_WAS_FOUND, null)).thenReturn(Boolean.FALSE);
        TargetLocator mockedTargetLocator = mock(TargetLocator.class);
        when(webDriver.switchTo()).thenReturn(mockedTargetLocator);
        when(mockedTargetLocator.frame(videoFrame)).thenReturn(webDriver);
        setVariableSteps.getUrlValueOfVideoWithName(NAME, VARIABLE_SCOPE, URL_VARIABLE);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, URL_VARIABLE, null);
    }

    @Test
    void testGetNumberOfElementsByXpathToScenarioVariable()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(webUiContext.getSearchContext()).thenReturn(webDriver);
        WebElement webElement = mock(WebElement.class);
        when(searchActions.findElements(webDriver, searchAttributes)).thenReturn(Collections.singletonList(webElement));
        setVariableSteps.getNumberOfElementsByXpathToVariable(searchAttributes, VARIABLE_SCOPE, NUMBER_BY_XPATH);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, NUMBER_BY_XPATH, 1);
    }

    @Test
    void testGetNumberOfElementsByXpathToScenarioVariable2()
    {
        when(webUiContext.getSearchContext()).thenReturn(webDriver);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        WebElement webElement = mock(WebElement.class);
        when(searchActions.findElements(webDriver, searchAttributes)).thenReturn(Collections.singletonList(webElement));
        setVariableSteps.getNumberOfElementsByXpathToVariable(searchAttributes, VARIABLE_SCOPE, NUMBER_BY_XPATH);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, NUMBER_BY_XPATH, 1);
    }

    @Test
    void testGetNumberOfElementsByAttributeValueToStoryVariable()
    {
        when(webUiContext.getSearchContext()).thenReturn(webDriver);
        WebElement webElement = mock(WebElement.class);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH,
                ".//*[normalize-space(@attributeType)=\"attributeValue\"]");
        when(searchActions.findElements(webDriver, searchAttributes)).thenReturn(Collections.singletonList(webElement));
        setVariableSteps.getNumberOfElementsByAttributeValueToVariable("attributeType", "attributeValue",
                VARIABLE_SCOPE, NUMBER_BY_XPATH);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, NUMBER_BY_XPATH, 1);
    }

    @Test
    void testGetTextOfContentToVariable()
    {
        when(webUiContext.getSearchContext()).thenReturn(webDriver);
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(webElementActions.getElementText(webElement)).thenReturn(TEXT);
        setVariableSteps.getTextOfContentToVariable(VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, TEXT);
    }

    @Test
    void testGetTextOfContentToVariableNoContext()
    {
        when(webUiContext.getSearchContext()).thenReturn(null);
        setVariableSteps.getTextOfContentToVariable(VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, null);
    }

    @Test
    void testSetAttributeValueToVariable()
    {
        WebElement webElement = mock(WebElement.class);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        String attributeName = "attribute";
        String attributeValue = TEXT;
        when(webElement.getAttribute(attributeName)).thenReturn(attributeValue);
        when(softAssert.assertNotNull("The '" + attributeName + "' attribute value was found", attributeValue))
                .thenReturn(Boolean.TRUE);
        setVariableSteps.setAttributeValueToVariable(attributeName, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, attributeValue);
    }

    @Test
    void testGettingValueFromJS()
    {
        when(javascriptActions.executeScript(JS_CODE)).thenReturn(VALUE);
        when(softAssert.assertNotNull(JS_RESULT_ASSERTION_MESSAGE, VALUE)).thenReturn(true);
        setVariableSteps.gettingValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, VALUE);
    }

    @Test
    void testGettingValueFromJSNullIsReturned()
    {
        setVariableSteps.gettingValueFromJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(softAssert).assertNotNull(JS_RESULT_ASSERTION_MESSAGE, null);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testGettingValueFromAsyncJS()
    {
        when(javascriptActions.executeAsyncScript(JS_CODE)).thenReturn(VALUE);
        when(softAssert.assertNotNull(JS_RESULT_ASSERTION_MESSAGE, VALUE)).thenReturn(true);
        setVariableSteps.gettingValueFromAsyncJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, VALUE);
    }

    @Test
    void testGettingValueFromAsyncJSNullIsReturned()
    {
        setVariableSteps.gettingValueFromAsyncJS(JS_CODE, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(softAssert).assertNotNull(JS_RESULT_ASSERTION_MESSAGE, null);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testSavePathFromUrl()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.getCurrentUrl()).thenReturn(VALID_URL);

        setVariableSteps.savePathFromUrl(VARIABLE_SCOPE, VARIABLE_NAME);

        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, PATH);
    }

    @Test
    void testSavePathFromUrlException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        String url = "data.;";
        when(webDriver.getCurrentUrl()).thenReturn(url);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> setVariableSteps.savePathFromUrl(VARIABLE_SCOPE, VARIABLE_NAME));
        assertEquals("Scheme is missing in URL: " + url, exception.getMessage());
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void shouldCallJSScriptAndSaveValueToContext()
    {
        WebElement table = mock(WebElement.class);
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(table);
        List<Map<String, String>> listOfTables = List.of(Map.of("key", VALUE));
        when(javascriptActions.executeScriptFromResource(SetVariableSteps.class, "parse-table.js", table))
                .thenReturn(listOfTables);
        setVariableSteps.saveTableToContext(VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, listOfTables);
    }

    @Test
    void testSetAttributeValueBySelectorToVariable()
    {
        SearchAttributes searchAttributes = mock(SearchAttributes.class);
        SearchParameters searchParameters = mock(SearchParameters.class);
        WebElement webElement = mock(WebElement.class);
        when(baseValidations.assertIfElementExists(THE_ELEMENT_TO_EXTRACT_THE_ATTRIBUTE,
                searchAttributes)).thenReturn(webElement);
        when(searchAttributes.getSearchParameters()).thenReturn(searchParameters);
        when(searchParameters.setVisibility(Visibility.ALL)).thenReturn(searchParameters);
        when(webElement.getAttribute(NAME)).thenReturn(VALUE);
        setVariableSteps.setAttributeValueBySelectorToVariable(NAME, searchAttributes,
                VARIABLE_SCOPE, NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, NAME, VALUE);
    }
}

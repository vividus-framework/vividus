/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.vividus.ui.web.action.search.ActionAttributeType.CASE_SENSITIVE_TEXT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.model.ExamplesTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.bdd.steps.ui.web.validation.IHighlightingSoftAssert;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.ClickActions;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@RunWith(PowerMockRunner.class)
public class ElementStepsTests
{
    private static final String PARENT_ELEMENT_HAS_CHILD = "Parent element has number of child elements which";
    private static final String ELEMENT_WITH_ELEMENT_NAME_AND_TEXT =
            "An element with the name 'elementName' and text 'text'";
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String APOSTROPHE = "'";
    private static final String AN_ELEMENT_WITH_THE_LOCATOR = "An element with the locator '";
    private static final String RADIO_BUTTON = "Radio button";
    private static final String A_RADIO_BUTTON_LABEL_WITH_TEXT_VALUE_1 = "A radio button label with text 'value 1'";
    private static final String AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME = "An element with the name 'elementName'";
    private static final String AN_ELEMENT_WITH_THE_NAME_AND_TEXT = AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME
            + " and text 'text'";
    private static final String AN_ELEMENT_WITH_THE_TAG_AND_TEXT = "An element with the tag 'tag' and text 'text'";
    private static final String A_STATE_ELEMENT_WITH_THE_NAME_AND_TEXT = "A ENABLED element with the name"
            + " 'elementName' and text 'text'";
    private static final String A_STATE_ELEMENT_WITH_THE_NAME = "A ENABLED element with the name 'elementName";
    private static final String AN_ELEMENT = "An element";
    private static final String FILE_FILE_PATH_EXISTS = "File filePath exists";
    private static final String ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE = "Element has correct css property value";
    private static final String TAG = "tag";
    private static final String PATTERN_RADIO_XPATH = "input[@type='radio' and @id='%s']";
    private static final String TABLE = "|radioOption|\n |value 1|";
    private static final String LABEL_VALUE = "value 1";
    private static final String CSS_PART_VALUE = "Value";
    private static final String CSS_VALUE = "cssValue";
    private static final String CSS_NAME = "cssName";
    private static final String ELEMENT_NAME = "elementName";
    private static final String XPATH = ".//xpath";
    private static final String TEXT = "text";
    private static final String FILE_PATH = "filePath";
    private static final String ABSOLUTE_PATH = "file:/D:/file.txt";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String LOCATOR_BY_ATTRIBUTE = LocatorUtil.getXPathByAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
    private static final String ELEMENT_XPATH = "elementXpath";
    private static final String CHILD_XPATH = "childXpath";
    private static final String THE_FOUND_BUTTON_STATE = "The found button is " + State.ENABLED;
    private static final String THE_NUMBER_OF_PARENT_ELEMENTS = "The number of parent elements";
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";
    private static final String THE_FOUND_ELEMENT_IS = "The found element is ";
    private static final String STATE_ENABLED = "ENABLED";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private ClickActions clickActions;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IElementValidations elementValidations;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement webElement;

    @Mock
    private WebElement bodyElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private IMouseActions mouseActions;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private ElementSteps elementSteps;

    @Mock
    private IHighlightingSoftAssert softAssert;

    private List<WebElement> elementsList;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        elementsList = new ArrayList<>();
        elementsList.add(webElement);
    }

    @Test
    public void testContextClickElementByXpath()
    {
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK,
                new SearchAttributes(ActionAttributeType.XPATH, XPATH))).thenReturn(webElement);
        elementSteps.contextClickElementByXpath(XPATH);
        verify(mouseActions).contextClick(webElement);
    }

    @Test
    public void testClickAllElementsByXpath()
    {
        when(baseValidations
                .assertIfElementsExist("The elements to click", new SearchAttributes(ActionAttributeType.XPATH, XPATH)))
                .thenReturn(Arrays.asList(webElement, webElement));
        elementSteps.clickEachElementByXpath(XPATH);
        verify(clickActions, times(2)).click(webElement);
    }

    @Test
    public void testClickAllElementsByXpathNoElements()
    {
        when(baseValidations.assertIfElementsExist("Elements to click",
                new SearchAttributes(ActionAttributeType.XPATH, XPATH))).thenReturn(List.of());
        elementSteps.clickEachElementByXpath(XPATH);
        verifyZeroInteractions(clickActions);
    }

    @Test
    public void testIsElementHasRightWidth()
    {
        int widthInPerc = 50;
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters("//body").setVisibility(Visibility.ALL)))).thenReturn(bodyElement);
        elementSteps.isElementHasRightWidth(widthInPerc);
        verify(elementValidations).assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
    }

    @Test
    public void testIfElementWithNameExists()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME, attributes))
                .thenReturn(webElement);
        elementSteps.ifElementWithNameExists(ELEMENT_NAME);
        verify(baseValidations).assertIfElementExists(AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME, attributes);
    }

    @Test
    public void testIfElementWithCertainAttributeAndStateFound()
    {
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LOCATOR_BY_ATTRIBUTE))).thenReturn(webElement);
        elementSteps.isElementWithCertainAttributeAndStateFound(State.ENABLED, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(baseValidations).assertElementState(THE_FOUND_ELEMENT_IS + State.ENABLED, State.ENABLED, webElement);
    }

    @Test
    public void testisElementWithCertainAttributeFound()
    {
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LOCATOR_BY_ATTRIBUTE))).thenReturn(webElement);
        assertEquals(webElement, elementSteps.isElementWithCertainAttributeFound(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE));
    }

    @Test
    public void testIfStateElementWithNameExists()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(ActionAttributeType.STATE, STATE_ENABLED);
        when(baseValidations.assertIfElementExists(A_STATE_ELEMENT_WITH_THE_NAME, attributes)).thenReturn(webElement);
        elementSteps.ifElementWithNameExists(State.ENABLED, ELEMENT_NAME);
        verify(baseValidations).assertIfElementExists(A_STATE_ELEMENT_WITH_THE_NAME, attributes);
    }

    @Test
    public void testDoesNotContainElementExist()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        when(baseValidations.assertIfElementDoesNotExist(AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME, attributes))
                .thenReturn(true);
        elementSteps.doesNotContainElementExist(ELEMENT_NAME);
        verify(baseValidations).assertIfElementDoesNotExist(AN_ELEMENT_WITH_THE_NAME_ELEMENT_NAME, attributes);
    }

    @Test
    public void testIsElemenWithPartialAttributetFound()
    {
        elementSteps.isElemenWithPartialAttributetFound(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(baseValidations).assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                ".//*[contains(normalize-space(@attributeType),\"attributeValue\")]"));
    }

    @Test
    public void testDoesElementContainRadioOptions()
    {
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_VALUE_1, new SearchAttributes(
                ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, LABEL_VALUE))))
                    .thenReturn(webElement);
        when(webElement.getAttribute("for")).thenReturn(LABEL_VALUE);
        ExamplesTable radioOptions = new ExamplesTable(TABLE);
        elementSteps.doesElementContainRadioOptions(radioOptions);
        verify(baseValidations).assertIfElementExists(RADIO_BUTTON, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN,
                        LABEL_VALUE)));
    }

    @Test
    public void testElementDoesNotContainRadioOptions()
    {
        when(baseValidations.assertIfElementExists(A_RADIO_BUTTON_LABEL_WITH_TEXT_VALUE_1,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN,
                        LABEL_VALUE)))).thenReturn(null);
        ExamplesTable radioOptions = new ExamplesTable(TABLE);
        elementSteps.doesElementContainRadioOptions(radioOptions);
        verify(baseValidations, never()).assertIfElementExists(RADIO_BUTTON,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(PATTERN_RADIO_XPATH,
                LABEL_VALUE)));
    }

    @Test
    public void testIsElementHasWidthRelativeToTheParentElement()
    {
        int width = 50;
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(baseValidations.assertIfElementExists("Parent element", new SearchAttributes(ActionAttributeType.XPATH,
                "./.."))).thenReturn(webElement);
        elementSteps.isElementHasWidthRelativeToTheParentElement(width);
        verify(elementValidations).assertIfElementHasWidthInPerc(webElement, webElement, width);
    }

    @Test
    public void testIsElementWithTagAndTextFound()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.TAG_NAME, TAG);
        attributes.addFilter(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_TAG_AND_TEXT, attributes))
                .thenReturn(webElement);
        WebElement element = elementSteps.isElementWithTagAndTextFound(TAG, TEXT);
        assertEquals(webElement, element);
    }

    @Test
    public void testIsCertainElementFound()
    {
        elementSteps.isCertainElementFound(TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(baseValidations).assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPathByTagNameAndAttribute(TAG, ATTRIBUTE_TYPE, ATTRIBUTE_VALUE)));
    }

    @Test
    public void testIsElementWithNameAndTextFoundState()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(ActionAttributeType.STATE, STATE_ENABLED);
        attributes.addFilter(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(A_STATE_ELEMENT_WITH_THE_NAME_AND_TEXT, attributes))
                .thenReturn(webElement);
        WebElement element = elementSteps.isElementWithNameAndTextFound(State.ENABLED, ELEMENT_NAME, TEXT);
        assertEquals(webElement, element);
    }

    @Test
    public void testIsElementWithNameAndTextFoundStateWrong()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(ActionAttributeType.STATE, State.ENABLED.toString());
        attributes.addFilter(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, attributes)).thenReturn(webElement);
        when(baseValidations.assertElementState(THE_FOUND_BUTTON_STATE, State.ENABLED, webElement)).thenReturn(false);
        WebElement element = elementSteps.isElementWithNameAndTextFound(State.ENABLED, ELEMENT_NAME, TEXT);
        assertNotEquals(webElement, element);
    }

    @Test
    public void testIsElementWithNameAndTextFound()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_NAME_AND_TEXT, attributes))
                .thenReturn(webElement);
        WebElement element = elementSteps.isElementWithNameAndTextFound(ELEMENT_NAME, TEXT);
        assertEquals(webElement, element);
    }

    @Test
    public void testDoesElementWithNameContainingTextExist()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(ActionAttributeType.TEXT_PART, TEXT);
        when(baseValidations.assertIfElementExists("An element with the name 'elementName' containing text 'text'",
                attributes)).thenReturn(webElement);
        assertEquals(webElement, elementSteps.doesElementWithNameContainingTextExist(ELEMENT_NAME, TEXT));
    }

    @Test
    public void testIsElementWithNameAndTextFoundNull()
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.ELEMENT_NAME, ELEMENT_NAME);
        attributes.addFilter(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(
                ELEMENT_WITH_ELEMENT_NAME_AND_TEXT, attributes)).thenReturn(null);
        assertNull(elementSteps.isElementWithNameAndTextFound(ELEMENT_NAME, TEXT));
    }

    @Test
    public void testIsElementHasRightCss()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verify(softAssert).assertEquals(ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE, CSS_VALUE, CSS_VALUE);
    }

    @Test
    public void testIsNullElementHasRightCss()
    {
        when(webUiContext.getSearchContext()).thenReturn(null);
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);
        elementSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verify(softAssert).assertEquals(ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE, CSS_VALUE, null);
    }

    @Test
    public void testIsElementHasRightCssPart()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightPartOfCssValue(CSS_NAME, CSS_PART_VALUE);
    }

    @Test
    public void testThePageContainsQuatityElements()
    {
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        int number = 1;
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        elementSteps.thePageContainsQuatityElements(searchAttributes, comparisonRule, number);
        verify(baseValidations).assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                searchAttributes, number, comparisonRule);
    }

    @Test
    public void testDoesElementByXpathExist()
    {
        elementSteps.doesElementByXpathExist(XPATH);
        verify(baseValidations).assertIfElementExists(AN_ELEMENT_WITH_THE_LOCATOR + XPATH + APOSTROPHE,
                new SearchAttributes(ActionAttributeType.XPATH, XPATH));
    }

    @Test
    public void testDoesElementByXpathExist2()
    {
        String xpath = "//*[contains(@style, 'width: 100%')]";
        elementSteps.doesElementByXpathExist(xpath);
        verify(baseValidations).assertIfElementExists(AN_ELEMENT_WITH_THE_LOCATOR + xpath + APOSTROPHE,
                new SearchAttributes(ActionAttributeType.XPATH, xpath));
    }

    @Test
    public void doesEachElementByXpathHasChildWithTheXpathSuccess()
    {
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(ELEMENT_XPATH))))
                    .thenReturn(Arrays.asList(webElement, webElement));
        elementSteps.doesEachElementByXpathHasChildWithTheXpath(ELEMENT_XPATH, 2, CHILD_XPATH);
        verify(baseValidations, times(2)).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(CHILD_XPATH)), 2);
    }

    @Test
    public void doesEachElementByXpathHasChildWithTheXpathNoElements()
    {
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(ELEMENT_XPATH))))
                    .thenReturn(List.of());
        elementSteps.doesEachElementByXpathHasChildWithTheXpath(ELEMENT_XPATH, 2, CHILD_XPATH);
        verify(baseValidations, never()).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(CHILD_XPATH)), 2);
    }

    @Test
    public void testIsEachElementByXpathHasSameDimension()
    {
        Dimension dimension = Dimension.HEIGHT;
        elementsList.add(webElement);
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist(THE_NUMBER_OF_FOUND_ELEMENTS, searchAttributes, 2))
                .thenReturn(elementsList);
        elementSteps.doesEachElementByXpathHaveSameDimension(XPATH, dimension);
        verify(elementValidations).assertAllWebElementsHaveEqualDimension(elementsList, dimension);
    }

    @Test
    public void doesAtLeastOneELementByXpathExist()
    {
        elementSteps.doesAtLeastOneELementByXpathExist(XPATH);
        verify(baseValidations).assertIfAtLeastOneElementExists("The number of elemants by the xpath: './/xpath'",
                new SearchAttributes(ActionAttributeType.XPATH, LocatorUtil.getXPath(XPATH)));
    }

    @Test
    public void testHoverMouseOverAnElementByXpaht()
    {
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_LOCATOR + XPATH + APOSTROPHE,
                new SearchAttributes(ActionAttributeType.XPATH, XPATH))).thenReturn(webElement);
        elementSteps.hoverMouseOverAnElementByXpaht(XPATH);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    public void testClickElementWithText()
    {
        String text = "TEXT";
        SearchAttributes attributes = new SearchAttributes(CASE_SENSITIVE_TEXT, text);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, attributes)).thenReturn(webElement);
        elementSteps.clickElementByText(text);
        verify(clickActions).click(webElement);
    }

    @Test
    public void testClickElementWithTextElementNull()
    {
        elementSteps.clickElementByText("");
        verify(webElement, never()).click();
    }

    @Test
    public void testClickElementWithTextPageNotRefresh()
    {
        ClickResult clickResult = new ClickResult();
        clickResult.setNewPageLoaded(false);
        SearchAttributes attributes = new SearchAttributes(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, attributes)).thenReturn(webElement);
        when(clickActions.click(webElement)).thenReturn(clickResult);
        elementSteps.clickElementPageNotRefresh(attributes);
        verify(softAssert).assertTrue(eq("Page has not been refreshed after clicking on the element located by "
                + "Case sensitive text: 'text'; Visibility: VISIBLE;"), eq(!clickResult.isNewPageLoaded()));
    }

    @Test
    public void testLoadFileNotRemote() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(false);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(webElement);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    @PrepareForTest({ ElementSteps.class, FileUtils.class })
    public void testLoadFileFromJar() throws Exception
    {
        File file = mockFileForUpload();
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.whenNew(File.class).withArguments(FILE_PATH).thenReturn(file);
        Resource resource = mockResource("jar:file:/D:/archive.jar!/file.txt", file, true);
        InputStream inputStream = new ByteArrayInputStream(TEXT.getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(inputStream);
        mockResourceLoader(resource);
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(webElement);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
        PowerMockito.verifyStatic(FileUtils.class);
        FileUtils.copyInputStreamToFile(eq(resource.getInputStream()), any(File.class));
    }

    @Test
    public void testLoadFileRemote() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        mockRemoteWebDriver();
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(WrapsDriver.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(true);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(webElement);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileFromFilesystem() throws IOException
    {
        Resource resource = mockResource(ABSOLUTE_PATH, mockFileForUpload(), false);
        ResourceLoader resourceLoader = mockResourceLoader(resource);
        when(resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + FILE_PATH)).thenReturn(resource);
        mockRemoteWebDriver();
        WebDriver webDriver = mock(WebDriver.class, withSettings().extraInterfaces(WrapsDriver.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(true);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(webElement);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileRemoteSimpleDriver() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        mockRemoteWebDriver();
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(true);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(webElement);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileNoFile() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileNoElement() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(false);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new SearchAttributes(ActionAttributeType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL)))).thenReturn(null);
        elementSteps.uploadFile(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void clickElementByLocator()
    {
        SearchAttributes elementAttributes = new SearchAttributes(ActionAttributeType.XPATH, XPATH);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, elementAttributes)).thenReturn(webElement);
        elementSteps.clickElementByLocator(elementAttributes);
        verify(clickActions).click(webElement);
    }

    @Test
    public void clickElementWithCertanAttribute()
    {
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, new SearchAttributes(ActionAttributeType.XPATH,
                        LOCATOR_BY_ATTRIBUTE))).thenReturn(webElement);
        elementSteps.clickElementWithCertanAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(clickActions).click(webElement);
    }

    @Test
    public void atLeastOneElementWithAttributeExists()
    {
        elementSteps.atLeastOneElementWithAttributeExists(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
        verify(baseValidations).assertIfAtLeastOneElementExists(
                "The number of elements with the attribute 'attributeType'='attributeValue'",
                new SearchAttributes(ActionAttributeType.XPATH, LOCATOR_BY_ATTRIBUTE));
    }

    @Test
    public void testDoesElementByCssExist()
    {
        String cssSelector = ".class";
        elementSteps.doesElementByCssSelectorExist(cssSelector);
        verify(baseValidations).assertIfElementExists("An element with the selector '" + cssSelector + APOSTROPHE,
                new SearchAttributes(ActionAttributeType.CSS_SELECTOR, cssSelector));
    }

    private void mockWebElementCssValue()
    {
        when(webUiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);
    }

    private ResourceLoader mockResourceLoader(Resource resource)
    {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        elementSteps.setResourceLoader(resourceLoader);
        when(resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + FILE_PATH)).thenReturn(resource);
        return resourceLoader;
    }

    private File mockFileForUpload()
    {
        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
        return file;
    }

    private Resource mockResource(String resourceURL, File file, boolean fileExists)
            throws IOException
    {
        Resource resource = mock(Resource.class);
        when(resource.exists()).thenReturn(fileExists);
        when(resource.getURL()).thenReturn(new URL(resourceURL));
        when(resource.getFile()).thenReturn(file);
        return resource;
    }

    private void mockRemoteWebDriver()
    {
        RemoteWebDriver remoteDriver = mock(RemoteWebDriver.class);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteDriver);
    }
}

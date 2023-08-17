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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.WebLocatorType.CASE_SENSITIVE_TEXT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class ElementStepsTests
{
    private static final String PARENT_ELEMENT_HAS_CHILD = "Parent element has number of child elements which";
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String AN_ELEMENT_WITH_THE_ATTRIBUTES =
            "An element with attributes Element name: 'elementName'; Visibility: VISIBLE;";
    private static final String AN_ELEMENT = "An element";
    private static final String FILE_FILE_PATH_EXISTS = "File filePath exists";
    private static final String ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE = "Element has correct css property value";
    private static final String CSS_PROPERTY_VALUE_PART_IS_CORRECT = "Css property value part is correct";
    private static final String CSS_PART_VALUE = "Value";
    private static final String CSS_VALUE = "cssValue";
    private static final String CSS_NAME = "cssName";
    private static final String ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE =
            "Element has CSS property '" + CSS_NAME + "' containing value '" + CSS_PART_VALUE + "'";
    private static final String ELEMENT_NAME = "elementName";
    private static final String XPATH = ".//xpath";
    private static final String TEXT = "text";
    private static final String FILE_PATH = "filePath";
    private static final String ABSOLUTE_PATH = "file:/D:/file.txt";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_TYPE = "attributeType";
    private static final String LOCATOR_BY_ATTRIBUTE = XpathLocatorUtils.getXPathByAttribute(ATTRIBUTE_TYPE,
        ATTRIBUTE_VALUE);
    private static final String ELEMENT_XPATH = "elementXpath";
    private static final String CHILD_XPATH = "childXpath";
    private static final String THE_NUMBER_OF_PARENT_ELEMENTS = "The number of parent elements";
    private static final String ELEMENT_TO_CLICK = "Element to click";

    @Mock private IBaseValidations baseValidations;
    @Mock private IMouseActions mouseActions;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriverManager webDriverManager;
    @Mock private IElementValidations elementValidations;
    @Mock private IUiContext uiContext;
    @Mock private WebElementActions webElementActions;
    @Mock private WebElement webElement;
    @Mock private IDescriptiveSoftAssert softAssert;
    @InjectMocks private ElementSteps elementSteps;

    @Test
    void testContextClickElementByLocator()
    {
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        elementSteps.contextClickElementByLocator(locator);
        verify(mouseActions).contextClick(webElement);
    }

    @Test
    void shouldClickOnElement()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));

        elementSteps.clickOnElement(locator);

        verify(mouseActions).click(webElement);
    }

    @Test
    void shouldRetryClickOnElementIfStaleElementReferenceExceptionIsThrown()
    {
        var exception = mock(StaleElementReferenceException.class);
        var locator = mock(Locator.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));
        doThrow(exception).doReturn(new ClickResult()).when(mouseActions).click(webElement);

        elementSteps.clickOnElement(locator);

        verify(mouseActions, times(2)).click(webElement);
        verify(baseValidations, times(2)).assertElementExists(ELEMENT_TO_CLICK, locator);
    }

    @Test
    void testIsElementHasRightWidth()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        var widthInPerc = 50;
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        WebElement bodyElement = mock(WebElement.class);
        when(baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                new Locator(WebLocatorType.XPATH, new SearchParameters("//body", Visibility.ALL))))
                .thenReturn(bodyElement);
        elementSteps.isElementHasRightWidth(widthInPerc);
        verify(elementValidations).assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
    }

    @Test
    void testIsElementHasWidthRelativeToTheParentElement()
    {
        var width = 50;
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        when(baseValidations.assertIfElementExists("Parent element", new Locator(WebLocatorType.XPATH,
                "./.."))).thenReturn(webElement);
        elementSteps.isElementHasWidthRelativeToTheParentElement(width);
        verify(elementValidations).assertIfElementHasWidthInPerc(webElement, webElement, width);
    }

    @Test
    void testIsElementHasRightCss()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verify(softAssert).assertEquals(ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE, CSS_VALUE, CSS_VALUE);
    }

    @Test
    void testIsNullElementHasRightCss()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.empty());
        elementSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verifyNoInteractions(webElementActions, softAssert);
    }

    @Test
    void testIsElementHasRightCssPart()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightPartOfCssValue(CSS_NAME, CSS_PART_VALUE);
        verify(softAssert).assertThat(eq(CSS_PROPERTY_VALUE_PART_IS_CORRECT),
                eq(ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE), eq(CSS_VALUE),
                argThat(matcher -> matcher.toString().contains(CSS_PART_VALUE)));
    }

    @Test
    void doesEachElementByLocatorHaveChildWithLocatorSuccess()
    {
        var elementLocator = new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(ELEMENT_XPATH));
        var childLocator = new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(CHILD_XPATH));
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS, elementLocator)).thenReturn(
                List.of(webElement, webElement));
        elementSteps.doesEachElementByLocatorHaveChildWithLocator(elementLocator, 2, childLocator);
        verify(baseValidations, times(2)).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                childLocator, 2);
    }

    @Test
    void doesEachElementByLocatorHaveChildWithLocatorNoElements()
    {
        var elementLocator = new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(ELEMENT_XPATH));
        var childLocator = new Locator(WebLocatorType.XPATH, XpathLocatorUtils.getXPath(CHILD_XPATH));
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS, elementLocator))
                .thenReturn(List.of());
        elementSteps.doesEachElementByLocatorHaveChildWithLocator(elementLocator, 2, childLocator);
        verify(baseValidations, never()).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                childLocator, 2);
    }

    @Test
    void testIsEachElementByXpathHasSameDimension()
    {
        var dimension = Dimension.HEIGHT;
        var elements = List.of(webElement, webElement);
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist("The number of found elements", locator, 2))
                .thenReturn(elements);
        elementSteps.doesEachElementByLocatorHaveSameDimension(locator, dimension);
        verify(elementValidations).assertAllWebElementsHaveEqualDimension(elements, dimension);
    }

    @Test
    void testHoverMouseOverAnElementByLocator()
    {
        var locator = new Locator(WebLocatorType.ELEMENT_NAME, ELEMENT_NAME);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTES, locator))
                .thenReturn(webElement);
        elementSteps.hoverMouseOverElement(locator);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testClickElementWithTextPageNotRefresh()
    {
        var clickResult = new ClickResult();
        clickResult.setNewPageLoaded(false);
        var locator = new Locator(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        when(mouseActions.click(webElement)).thenReturn(clickResult);
        elementSteps.clickElementPageNotRefresh(locator);
        verify(softAssert).assertTrue("Page has not been refreshed after clicking on the element located by "
                + "Case sensitive text: 'text'; Visibility: VISIBLE;", !clickResult.isNewPageLoaded());
    }

    @Test
    void testLoadFileNotRemote() throws IOException
    {
        var file = mockFileForUpload();
        mockResourceLoader(mockResource(file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(false);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testLoadFileFromJar() throws IOException
    {
        try (var fileUtils = mockStatic(FileUtils.class);
             var fileMock = mockConstruction(File.class,
                 (mock, context) -> {
                     assertEquals(List.of(FILE_PATH), context.arguments());
                     when(mock.exists()).thenReturn(true);
                     when(mock.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
                 })
        )
        {
            var resource = mock(Resource.class);
            when(resource.exists()).thenReturn(true);
            when(resource.getURL()).thenReturn(new URL("jar:file:/D:/archive.jar!/file.txt"));
            InputStream inputStream = new ByteArrayInputStream(TEXT.getBytes(StandardCharsets.UTF_8));
            when(resource.getInputStream()).thenReturn(inputStream);
            mockResourceLoader(resource);
            when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
            var locator = new Locator(WebLocatorType.XPATH,
                    new SearchParameters(XPATH).setVisibility(Visibility.ALL));
            when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
            elementSteps.uploadFile(locator, FILE_PATH);
            verify(webElement).sendKeys(ABSOLUTE_PATH);
            var file = fileMock.constructed().get(0);
            fileUtils.verify(() -> FileUtils.copyInputStreamToFile(resource.getInputStream(), file));
        }
    }

    @Test
    void testLoadFileRemote() throws IOException
    {
        var file = mockFileForUpload();
        mockResourceLoader(mockResource(file, true));
        mockRemoteWebDriver();
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(true);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testLoadFileFromFilesystem() throws IOException
    {
        var resource = mockResource(mockFileForUpload(), false);
        var resourceLoader = mockResourceLoader(resource);
        when(resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + FILE_PATH)).thenReturn(resource);
        mockRemoteWebDriver();
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(true);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testLoadFileRemoteSimpleDriver() throws IOException
    {
        var file = mockFileForUpload();
        mockResourceLoader(mockResource(file, true));
        mockRemoteWebDriver();
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(true);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testLoadFileNoFile() throws IOException
    {
        var file = mock(File.class);
        when(file.exists()).thenReturn(true);
        mockResourceLoader(mockResource(file, true));
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testLoadFileNoElement() throws IOException
    {
        var file = mockFileForUpload();
        mockResourceLoader(mockResource(file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(false);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(null);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    private void mockWebElementCssValue()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);
    }

    private ResourceLoader mockResourceLoader(Resource resource)
    {
        var resourceLoader = mock(ResourceLoader.class);
        elementSteps.setResourceLoader(resourceLoader);
        when(resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + FILE_PATH)).thenReturn(resource);
        return resourceLoader;
    }

    private File mockFileForUpload()
    {
        var file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
        return file;
    }

    private Resource mockResource(File file, boolean fileExists) throws IOException
    {
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(fileExists);
        when(resource.getURL()).thenReturn(new URL(ABSOLUTE_PATH));
        when(resource.getFile()).thenReturn(file);
        return resource;
    }

    private void mockRemoteWebDriver()
    {
        var remoteDriver = mock(RemoteWebDriver.class);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteDriver);
    }
}

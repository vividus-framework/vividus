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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.vividus.ui.web.action.search.WebLocatorType.CASE_SENSITIVE_TEXT;

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
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.util.LocatorUtil;

@RunWith(PowerMockRunner.class)
public class ElementStepsTests
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
    private static final String LOCATOR_BY_ATTRIBUTE = LocatorUtil.getXPathByAttribute(ATTRIBUTE_TYPE, ATTRIBUTE_VALUE);
    private static final String ELEMENT_XPATH = "elementXpath";
    private static final String CHILD_XPATH = "childXpath";
    private static final String THE_NUMBER_OF_PARENT_ELEMENTS = "The number of parent elements";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IMouseActions mouseActions;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IElementValidations elementValidations;

    @Mock
    private IUiContext uiContext;

    @Mock
    private WebElementActions webElementActions;

    @Mock
    private WebElement webElement;

    @Mock
    private WebElement bodyElement;

    @Mock
    private WebDriver webDriver;

    @Mock
    private SearchContext searchContext;

    @InjectMocks
    private ElementSteps elementSteps;

    @Mock
    private IDescriptiveSoftAssert softAssert;

    private List<WebElement> elementsList;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webDriverProvider.get()).thenReturn(webDriver);
        elementsList = new ArrayList<>();
        elementsList.add(webElement);
    }

    @Test
    public void testContextClickElementByLocator()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        elementSteps.contextClickElementByLocator(locator);
        verify(mouseActions).contextClick(webElement);
    }

    @Test
    public void testClickOnElementByLocator()
    {
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, ELEMENT_NAME);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTES, locator))
                .thenReturn(webElement);
        elementSteps.clickOnElement(locator);
        verify(mouseActions).click(webElement);
    }

    @Test
    public void testClickAllElementsByLocator()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations
                .assertIfElementsExist("The elements to click", locator))
                .thenReturn(Arrays.asList(webElement, webElement));
        elementSteps.clickOnAllElements(locator);
        verify(mouseActions, times(2)).click(webElement);
    }

    @Test
    public void testClickAllElementsByLocatorNoElements()
    {
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations.assertIfElementsExist("Elements to click",
                locator)).thenReturn(List.of());
        elementSteps.clickOnAllElements(locator);
        verifyNoInteractions(mouseActions);
    }

    @Test
    public void testIsElementHasRightWidth()
    {
        int widthInPerc = 50;
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(baseValidations.assertIfElementExists("'Body' element", webDriverProvider.get(),
                new Locator(WebLocatorType.XPATH, new SearchParameters("//body", Visibility.ALL))))
                .thenReturn(bodyElement);
        elementSteps.isElementHasRightWidth(widthInPerc);
        verify(elementValidations).assertIfElementHasWidthInPerc(bodyElement, webElement, widthInPerc);
    }

    @Test
    public void testIsElementHasWidthRelativeToTheParentElement()
    {
        int width = 50;
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(baseValidations.assertIfElementExists("Parent element", new Locator(WebLocatorType.XPATH,
                "./.."))).thenReturn(webElement);
        elementSteps.isElementHasWidthRelativeToTheParentElement(width);
        verify(elementValidations).assertIfElementHasWidthInPerc(webElement, webElement, width);
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
        when(uiContext.getSearchContext()).thenReturn(null);
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);
        elementSteps.doesElementHaveRightCss(CSS_NAME, CSS_VALUE);
        verify(softAssert).assertEquals(ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE, CSS_VALUE, null);
    }

    @Test
    public void testIsElementHasRightCssPart()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightPartOfCssValue(CSS_NAME, CSS_PART_VALUE);
        verify(softAssert).assertThat(eq(CSS_PROPERTY_VALUE_PART_IS_CORRECT),
                eq(ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE), eq(CSS_VALUE),
                argThat(matcher -> matcher.toString().contains(CSS_PART_VALUE)));
    }

    @Test
    public void doesEachElementByLocatorHaveChildWithLocatorSuccess()
    {
        Locator elementLocator = new Locator(WebLocatorType.XPATH, LocatorUtil.getXPath(ELEMENT_XPATH));
        Locator childLocator = new Locator(WebLocatorType.XPATH, LocatorUtil.getXPath(CHILD_XPATH));
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS, elementLocator))
                .thenReturn(Arrays.asList(webElement, webElement));
        elementSteps.doesEachElementByLocatorHaveChildWithLocator(elementLocator, 2, childLocator);
        verify(baseValidations, times(2)).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                childLocator, 2);
    }

    @Test
    public void doesEachElementByLocatorHaveChildWithLocatorNoElements()
    {
        Locator elementLocator = new Locator(WebLocatorType.XPATH, LocatorUtil.getXPath(ELEMENT_XPATH));
        Locator childLocator = new Locator(WebLocatorType.XPATH, LocatorUtil.getXPath(CHILD_XPATH));
        when(baseValidations.assertIfElementsExist(THE_NUMBER_OF_PARENT_ELEMENTS, elementLocator))
                .thenReturn(List.of());
        elementSteps.doesEachElementByLocatorHaveChildWithLocator(elementLocator, 2, childLocator);
        verify(baseValidations, never()).assertIfExactNumberOfElementsFound(PARENT_ELEMENT_HAS_CHILD, webElement,
                childLocator, 2);
    }

    @Test
    public void testIsEachElementByXpathHasSameDimension()
    {
        Dimension dimension = Dimension.HEIGHT;
        elementsList.add(webElement);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Locator locator = new Locator(WebLocatorType.XPATH, XPATH);
        when(baseValidations.assertIfAtLeastNumberOfElementsExist("The number of found elements", locator, 2))
                .thenReturn(elementsList);
        elementSteps.doesEachElementByLocatorHaveSameDimension(locator, dimension);
        verify(elementValidations).assertAllWebElementsHaveEqualDimension(elementsList, dimension);
    }

    @Test
    public void testHoverMouseOverAnElementByLocator()
    {
        Locator locator = new Locator(WebLocatorType.ELEMENT_NAME, ELEMENT_NAME);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_WITH_THE_ATTRIBUTES, locator))
                .thenReturn(webElement);
        elementSteps.hoverMouseOverElement(locator);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    public void testClickElementWithTextPageNotRefresh()
    {
        ClickResult clickResult = new ClickResult();
        clickResult.setNewPageLoaded(false);
        Locator locator = new Locator(CASE_SENSITIVE_TEXT, TEXT);
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        when(mouseActions.click(webElement)).thenReturn(clickResult);
        elementSteps.clickElementPageNotRefresh(locator);
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
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
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
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
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
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
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
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
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
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileNoFile() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    public void testLoadFileNoElement() throws IOException
    {
        File file = mockFileForUpload();
        mockResourceLoader(mockResource(ABSOLUTE_PATH, file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverProvider.isRemoteExecution()).thenReturn(false);
        when(baseValidations.assertIfElementExists(AN_ELEMENT, new Locator(WebLocatorType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE, Visibility.ALL)))).thenReturn(null);
        Locator locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    private void mockWebElementCssValue()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
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

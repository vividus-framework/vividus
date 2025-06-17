/*
 * Copyright 2019-2025 the original author or authors.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import org.vividus.context.VariableContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.util.XpathLocatorUtils;
import org.vividus.ui.web.action.ResourceFileLoader;
import org.vividus.ui.web.action.WebElementActions;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.ui.web.validation.ScrollValidations;
import org.vividus.variable.VariableScope;

@SuppressWarnings({ "checkstyle:MethodCount", "PMD.UnnecessaryBooleanAssertion" })
@ExtendWith(MockitoExtension.class)
class ElementStepsTests
{
    private static final String PARENT_ELEMENT_HAS_CHILD = "Parent element has number of child elements which";
    private static final String AN_ELEMENT = "An element";
    private static final String FILE_FILE_PATH_EXISTS = "File filePath exists";
    private static final String ELEMENT_HAS_CORRECT_CSS_PROPERTY_VALUE = "Element has correct css property value";
    private static final String CSS_PROPERTY_VALUE_PART_IS_CORRECT = "Css property value part is correct";
    private static final String CSS_PART_VALUE = "Value";
    private static final String CSS_VALUE = "cssValue";
    private static final String CSS_NAME = "cssName";
    private static final String ELEMENT_HAS_CSS_PROPERTY_CONTAINING_VALUE =
            "Element has CSS property '" + CSS_NAME + "' containing value '" + CSS_PART_VALUE + "'";
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
    private static final String PARENT_ELEMENT_XPATH = "./..";
    private static final String JAR_ARCHIVE_FILE_TXT = "jar:file:/D:/archive.jar!/file.txt";
    private static final String FILE_INPUT_ELEMENT = "A file input element";
    private static final String VARIABLE_NAME = "variableName";
    private static final String ELEMENT_WITH_CSS_PROPERTY = "The element to get the CSS property value";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);
    private static final String ELEMENT_TO_CHECK = "Element to check";

    @Mock private IBaseValidations baseValidations;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriverManager webDriverManager;
    @Mock private IElementValidations elementValidations;
    @Mock private IUiContext uiContext;
    @Mock private WebElementActions webElementActions;
    @Mock private WebElement webElement;
    @Mock private IDescriptiveSoftAssert softAssert;
    @Mock private VariableContext variableContext;
    @Mock private ResourceFileLoader resourceFileLoader;
    @Mock private ScrollValidations<WebElement> scrollValidations;
    @InjectMocks private ElementSteps elementSteps;

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
                PARENT_ELEMENT_XPATH))).thenReturn(webElement);
        elementSteps.isElementHasWidthRelativeToTheParentElement(width);
        verify(elementValidations).assertIfElementHasWidthInPerc(webElement, webElement, width);
    }

    @Test
    void testIsElementHasWidthRelativeToParentElement()
    {
        var width = 50;
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        when(baseValidations.assertElementExists("The parent element", new Locator(WebLocatorType.XPATH,
                PARENT_ELEMENT_XPATH))).thenReturn(Optional.of(webElement));
        elementSteps.elementHasWidthRelativeToParentElement(width);
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
    void testDoesElementHasRightCss()
    {
        mockWebElementCssValue();
        elementSteps.doesElementHaveRightCss(CSS_NAME, StringComparisonRule.CONTAINS, CSS_PART_VALUE);
        verify(softAssert).assertThat(eq("Element css property value is"), eq(CSS_VALUE),
                argThat(matcher -> matcher.toString().contains(CSS_PART_VALUE)));
    }

    @ParameterizedTest
    @CsvSource({
        "'rgb(0, 0, 0)',           'rgb(0, 0, 0)',          true,  1",
        "'rgb(10, 10, 10)',        'rgba(10, 10, 10, 1)',   true,  1",
        "'rgb(10, 10, 10)',        'rgba(10, 10, 10, 0.5)', false, 1",
        "'rgb(10, 10, 10)',        'rgba(10, 10, 5, 1)',    false, 1",
        "'rgba(255, 255, 255, 1)', 'rgb(255, 255, 255)',    true,  1",
        "'rgba(1, 1, 1, 0)',       'rgba(1, 1, 1, 0)',      true,  1",
        "'rgba(1, 1, 1, 1)',       'rgba(1, 1, 1, 1)',      true,  1",
        "'rgba(0, 0, 0, 1)',        black,                  true,  0",
        " white,                   'rgb(255, 255, 255)',    true,  0"
    })
    void testDoesElementHasRightCssColorValue(String expected, String actual, boolean result, int times)
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(actual);
        elementSteps.doesElementHaveRightCss(CSS_NAME, StringComparisonRule.IS_EQUAL_TO, expected);
        String description = String.format("The value of CSS property 'cssName'"
                + " [Expected: '%s' Actual: was '%s']", expected, actual);
        verify(softAssert, times(times)).recordAssertion(eq(result), eq(description));
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
    void testLoadFileNotRemote() throws IOException
    {
        var file = mockFileForUpload();
        mockResourceLoader(mockResource(file, true));
        when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
        when(webDriverManager.isRemoteExecution()).thenReturn(false);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testUploadFileNotRemote() throws IOException
    {
        File file = mock();
        when(resourceFileLoader.loadFile(FILE_PATH)).thenReturn(file);
        when(file.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
        when(webDriverManager.isRemoteExecution()).thenReturn(false);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertElementExists(FILE_INPUT_ELEMENT, locator)).thenReturn(Optional.of(webElement));
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
                 });
             InputStream inputStream = new ByteArrayInputStream(TEXT.getBytes(StandardCharsets.UTF_8))
        )
        {
            var resource = mock(Resource.class);
            when(resource.exists()).thenReturn(true);
            when(resource.getURL()).thenReturn(new URL(JAR_ARCHIVE_FILE_TXT));
            when(resource.getInputStream()).thenReturn(inputStream);
            mockResourceLoader(resource);
            when(softAssert.assertTrue(FILE_FILE_PATH_EXISTS, true)).thenReturn(true);
            var locator = new Locator(WebLocatorType.XPATH,
                    new SearchParameters(XPATH).setVisibility(Visibility.ALL));
            when(baseValidations.assertIfElementExists(AN_ELEMENT, locator)).thenReturn(webElement);
            elementSteps.uploadFileDeprecated(locator, FILE_PATH);
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
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
        verify(webElement).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testUploadFileRemote() throws IOException
    {
        File file = mock();
        when(resourceFileLoader.loadFile(FILE_PATH)).thenReturn(file);
        when(file.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
        mockRemoteWebDriver();
        when(webDriverManager.isRemoteExecution()).thenReturn(true);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(XPATH).setVisibility(Visibility.ALL));
        when(baseValidations.assertElementExists(FILE_INPUT_ELEMENT, locator)).thenReturn(Optional.of(webElement));
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
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
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
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
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
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void testUploadFileNoFile() throws IOException
    {
        File file = mock();
        when(resourceFileLoader.loadFile(FILE_PATH)).thenReturn(file);
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
        elementSteps.uploadFileDeprecated(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void shouldSaveCssPropertyValue()
    {
        var locator = mock(Locator.class);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator))
                .thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(CSS_VALUE);

        elementSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, CSS_VALUE);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldNotSaveCssPropertyValueIfTheValueIsNotFound()
    {
        var locator = mock(Locator.class);
        var webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator))
                .thenReturn(Optional.of(webElement));
        when(webElementActions.getCssValue(webElement, CSS_NAME)).thenReturn(StringUtils.EMPTY);

        elementSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verify(softAssert).recordFailedAssertion(String.format("The '%s' CSS property does not exist", CSS_NAME));
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldNotSaveCssPropertyValueIfTheElementIsNotFound()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertElementExists(ELEMENT_WITH_CSS_PROPERTY, locator)).thenReturn(Optional.empty());

        elementSteps.saveCssPropertyValue(CSS_NAME, locator, VARIABLE_SCOPE, VARIABLE_NAME);
        verifyNoInteractions(softAssert, variableContext);
    }

    @Test
    void testUploadFileNoElement() throws IOException
    {
        File file = mock();
        when(resourceFileLoader.loadFile(FILE_PATH)).thenReturn(file);
        when(webDriverManager.isRemoteExecution()).thenReturn(false);
        var locator = new Locator(WebLocatorType.XPATH,
                new SearchParameters(LOCATOR_BY_ATTRIBUTE).setVisibility(Visibility.ALL));
        when(baseValidations.assertElementExists(FILE_INPUT_ELEMENT, locator)).thenReturn(Optional.ofNullable(null));
        elementSteps.uploadFile(locator, FILE_PATH);
        verify(webElement, never()).sendKeys(ABSOLUTE_PATH);
    }

    @Test
    void shouldCheckElementViewportPresence()
    {
        WebElement element = mock();
        Locator locator = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CHECK, locator)).thenReturn(Optional.of(element));

        elementSteps.checkElementViewportPresence(locator, ViewportPresence.IS);

        verify(scrollValidations).assertElementPositionAgainstViewport(element, ViewportPresence.IS);
    }

    @Test
    void shouldCheckElementViewportPresenceNoElementsFound()
    {
        Locator locator = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CHECK, locator)).thenReturn(Optional.empty());

        elementSteps.checkElementViewportPresence(locator, ViewportPresence.IS);

        verifyNoInteractions(scrollValidations, softAssert);
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

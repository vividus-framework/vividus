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

package org.vividus.ui.web.action.search;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.vividus.selenium.locator.Locator;
import org.vividus.spring.StringToLocatorConverter;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class RelativeElementSearchTests
{
    private static final String RELATIVE_ELEMENT = "relative element";
    private static final String ID_LOCATOR = "id(someId)";
    private static final String XPATH_LOCATOR = "xpath(/div)";
    private static final String DIV = "/div";
    private static final String ROOT_ELEMENTS = "root elements";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(RelativeElementSearch.class);

    @Mock private StringToLocatorConverter converter;
    @Mock private IBaseValidations baseValidations;
    @Mock private SearchContext searchContext;
    @Mock private Locator rootElementLocator;

    @InjectMocks
    private RelativeElementSearch relativeElementSearch;

    @Mock private WebElement webElement;

    private List<WebElement> webElements;

    @BeforeEach
    void beforeEach()
    {
        webElements = new ArrayList<>();
        webElements.add(webElement);
    }

    @Test
    void testNoRootElementsFound()
    {
        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>leftOf(id(someId))");
        when(converter.convert(XPATH_LOCATOR)).thenReturn(rootElementLocator);
        when(baseValidations.assertIfElementsExist(ROOT_ELEMENTS, rootElementLocator)).thenReturn(List.of());

        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, empty());
        verifyNoMoreInteractions(baseValidations);
    }

    @Test
    void testWrongRelativePosition()
    {
        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>behind(id(someId))");
        when(converter.convert(XPATH_LOCATOR)).thenReturn(rootElementLocator);
        when(baseValidations.assertIfElementsExist(ROOT_ELEMENTS, rootElementLocator)).thenReturn(webElements);

        var locatorType = mock(LocatorType.class);
        when(rootElementLocator.getLocatorType()).thenReturn(locatorType);
        var rootElementLocatorSearchParams = mock(SearchParameters.class);
        when(rootElementLocator.getSearchParameters()).thenReturn(rootElementLocatorSearchParams);
        when(rootElementLocatorSearchParams.getValue()).thenReturn(XPATH_LOCATOR);
        when(locatorType.buildBy(XPATH_LOCATOR)).thenReturn(By.xpath(DIV));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> relativeElementSearch.search(searchContext, searchParameters));
        assertEquals("Unsupported relative element position: behind", exception.getMessage());
    }

    @Test
    void testNoRelativeElementsFound()
    {
        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>below(id(someId))");
        when(converter.convert(XPATH_LOCATOR)).thenReturn(rootElementLocator);
        when(baseValidations.assertIfElementsExist(ROOT_ELEMENTS, rootElementLocator)).thenReturn(webElements);

        var locatorType = mock(LocatorType.class);
        when(rootElementLocator.getLocatorType()).thenReturn(locatorType);
        var rootElementLocatorSearchParams = mock(SearchParameters.class);
        when(rootElementLocator.getSearchParameters()).thenReturn(rootElementLocatorSearchParams);
        when(rootElementLocatorSearchParams.getValue()).thenReturn(XPATH_LOCATOR);
        when(locatorType.buildBy(XPATH_LOCATOR)).thenReturn(By.xpath(DIV));
        var relativeElementLocator = mock(Locator.class);
        when(converter.convert(ID_LOCATOR)).thenReturn(relativeElementLocator);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator))
                .thenReturn(Optional.empty());

        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, empty());
    }

    @Test
    void testNoElementsByFullRelativeLocatorFound()
    {
        By rootBy = mockRootElement();

        var relativeElementLocator = mock(Locator.class);
        var relativeElement = mock(WebElement.class);
        when(converter.convert(ID_LOCATOR)).thenReturn(relativeElementLocator);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator))
                .thenReturn(Optional.of(relativeElement));

        RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(rootBy).toLeftOf(relativeElement);
        when(searchContext.findElements(relativeBy)).thenReturn(List.of());

        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>toRightOf(id(someId))",
                Visibility.ALL, false);
        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, empty());
        assertThat(logger.getLoggingEvents(), contains(
                info("Element located {} was found. But it doesn't place in proper position"
                        + " relative to other element in locator", rootBy)
        ));
    }

    @Test
    void testSuccessSimple()
    {
        By rootBy = mockRootElement();

        var relativeElementLocator = mock(Locator.class);
        var relativeElement = mock(WebElement.class);
        when(converter.convert(ID_LOCATOR)).thenReturn(relativeElementLocator);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator))
                .thenReturn(Optional.of(relativeElement));

        RelativeLocator.RelativeBy relativeBy1 = RelativeLocator.with(rootBy).toLeftOf(relativeElement);
        when(searchContext.findElements(relativeBy1)).thenReturn(webElements);

        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>above(id(someId))",
                Visibility.ALL, false);
        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, hasSize(1));
    }

    @Test
    void testWrapsElement()
    {
        By rootBy = mockRootElement();

        var relativeElementLocator = mock(Locator.class);
        var relativeTwiceWrappedElement = mock(WebElement.class, withSettings().extraInterfaces(WrapsElement.class));
        var relativeWrappedElement = mock(WebElement.class, withSettings().extraInterfaces(WrapsElement.class));
        var relativeElement = mock(WebElement.class);
        when(((WrapsElement) relativeTwiceWrappedElement).getWrappedElement()).thenReturn(relativeWrappedElement);
        when(((WrapsElement) relativeWrappedElement).getWrappedElement()).thenReturn(relativeElement);
        when(converter.convert(ID_LOCATOR)).thenReturn(relativeElementLocator);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator))
                .thenReturn(Optional.of(relativeTwiceWrappedElement));

        RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(rootBy).toLeftOf(relativeElement);
        when(searchContext.findElements(relativeBy)).thenReturn(webElements);

        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>toLeftOf(id(someId))",
                Visibility.ALL, false);
        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, hasSize(1));
    }

    @Test
    void testSuccessNear()
    {
        By rootBy = mockRootElement();

        var relativeElementLocator1 = mock(Locator.class);
        var relativeElement1 = mock(WebElement.class);
        when(converter.convert("id(near1)")).thenReturn(relativeElementLocator1);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator1))
                .thenReturn(Optional.of(relativeElement1));

        var relativeElementLocator2 = mock(Locator.class);
        var relativeElement2 = mock(WebElement.class);
        when(converter.convert("id(near2)")).thenReturn(relativeElementLocator2);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator2))
                .thenReturn(Optional.of(relativeElement2));

        RelativeLocator.RelativeBy relativeBy = RelativeLocator.with(rootBy).near(relativeElement1)
                .near(relativeElement2, 100);
        when(searchContext.findElements(relativeBy)).thenReturn(webElements);

        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>near(id(near1))>>near100px(id(near2))",
                Visibility.ALL, false);
        List<WebElement> search = relativeElementSearch.search(searchContext, searchParameters);
        assertThat(search, hasSize(1));
    }

    @Test
    void testIncorrectNear()
    {
        mockRootElement();

        var relativeElementLocator1 = mock(Locator.class);
        var relativeElement1 = mock(WebElement.class);
        when(converter.convert("id(near3)")).thenReturn(relativeElementLocator1);
        when(baseValidations.assertElementExists(RELATIVE_ELEMENT, searchContext, relativeElementLocator1))
                .thenReturn(Optional.of(relativeElement1));

        SearchParameters searchParameters = new SearchParameters("xpath(/div)>>nearSomePx(id(near3))",
                Visibility.ALL, false);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> relativeElementSearch.search(searchContext, searchParameters));
        assertEquals("Invalid near position format. Expected matches [^near(?:(\\d+)(?:px|PX|Px))?$]."
                        + " Actual [nearSomePx]",
                exception.getMessage());
    }

    private By mockRootElement()
    {
        when(converter.convert(XPATH_LOCATOR)).thenReturn(rootElementLocator);
        when(baseValidations.assertIfElementsExist(ROOT_ELEMENTS, rootElementLocator)).thenReturn(webElements);

        var locatorType = mock(LocatorType.class);
        when(rootElementLocator.getLocatorType()).thenReturn(locatorType);
        var rootElementLocatorSearchParams = mock(SearchParameters.class);
        when(rootElementLocator.getSearchParameters()).thenReturn(rootElementLocatorSearchParams);
        when(rootElementLocatorSearchParams.getValue()).thenReturn(XPATH_LOCATOR);

        By rootBy = By.xpath(DIV);
        when(locatorType.buildBy(XPATH_LOCATOR)).thenReturn(rootBy);
        return rootBy;
    }

    @Test
    void testWrongRelativeLocatorFormat()
    {
        SearchParameters searchParameters = new SearchParameters("xpath->(/div).leftOf(id(someId))");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> relativeElementSearch.search(searchContext, searchParameters));
        assertEquals("Incorrect relative locator format - unable to parse root element locator",
                exception.getMessage());
    }
}

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_URL;
import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_URL_PART;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.ILinkValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@SuppressWarnings("checkstyle:methodcount")
@ExtendWith(MockitoExtension.class)
class LinkStepsTests
{
    private static final String THE_FOUND_LINK_IS_ENABLED = "The found link is ENABLED";
    private static final String HOME = "Home";
    private static final String URL_PART = "URLpart";
    private static final String TEXT = "text";
    private static final String TOOLTIP = "tooltip";
    private static final String URL = "url";
    private static final String LINK_TEXT = "linkText";
    private static final String SLASH = "/";

    @Mock
    private SearchContext searchContext;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private ILinkValidations linkValidations;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IMouseActions mouseActions;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private LinkSteps webUiLinkSteps;

    @Test
    void clickLinkWithTextAndURL()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT).addFilter(LINK_URL,
                URL);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(webElement);
        webUiLinkSteps.clickLinkWithTextAndURL(LINK_TEXT, URL);
        verify(mouseActions).click(webElement);
    }

    @Test
    void clickLinkWithTextAndURLElementNull()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT).addFilter(LINK_URL,
                URL);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(null);
        webUiLinkSteps.clickLinkWithTextAndURL(LINK_TEXT, URL);
        verify(mouseActions, never()).click(webElement);
    }

    @Test
    void clickLinkWithText()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTextExists(webElement, LINK_TEXT)).thenReturn(webElement);
        webUiLinkSteps.clickLinkWithText(LINK_TEXT);
        verify(mouseActions).click(webElement);
    }

    @Test
    void clickLinkWithTextElementNull()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTextExists(webElement, LINK_TEXT)).thenReturn(null);
        webUiLinkSteps.clickLinkWithText(LINK_TEXT);
        verify(mouseActions, never()).click(webElement);
    }

    @Test
    void testClickLinkWithUrl()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(LINK_URL, URL);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(webElement);
        webUiLinkSteps.clickLinkWithUrl(URL);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testClickLinkWithUrlPart()
    {
        LinkSteps spy = Mockito.spy(webUiLinkSteps);
        when(spy.assertLinkExists(new SearchAttributes(LINK_URL_PART, URL_PART)))
                .thenReturn(webElement);
        spy.clickLinkWithUrlPart(URL_PART);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testClickLinkWithPartUrlAndText()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL_PART, URL_PART);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(webElement);
        webUiLinkSteps.clickLinkWithPartUrlAndText(URL_PART, LINK_TEXT);
        verify(mouseActions).click(webElement);
    }

    @Test
    void ifLinkWithTextAndUrlExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextAndUrlExists(LINK_TEXT, URL);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT).addFilter(LINK_URL,
                URL);
        verify(linkValidations).assertIfLinkExists(webElement, attributes);
    }

    @Test
    void ifLinkWithTextAndUrlExistsState()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT).addFilter(LINK_URL,
                URL);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(webElement);
        State state = State.ENABLED;
        webUiLinkSteps.ifLinkWithTextAndUrlExists(state, LINK_TEXT, URL);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, state, webElement);
    }

    @Test
    void ifLinkWithTextAndUrlExistsStateElementNull()
    {
        webUiLinkSteps.ifLinkWithTextAndUrlExists(State.ENABLED, TEXT, URL);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void ifLinkWithTooltipExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTooltipExists(TOOLTIP);
        verify(linkValidations).assertIfLinkWithTooltipExists(webElement, TOOLTIP);
    }

    @Test
    void ifLinkWithTooltipExistState()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTooltipExists(webElement, TOOLTIP)).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTooltipExists(State.ENABLED, TOOLTIP);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void ifLinkWithTooltipExistStateElementNull()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTooltipExists(webElement, TOOLTIP)).thenReturn(null);
        webUiLinkSteps.ifLinkWithTooltipExists(State.ENABLED, TOOLTIP);
        verify(baseValidations, never()).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfLinkWithTextExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextExists(TEXT);
        verify(linkValidations).assertIfLinkWithTextExists(webElement, TEXT);
    }

    @Test
    void testIfStateLinkWithTextExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTextExists(webElement, TEXT)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithTextExists(State.ENABLED, TEXT);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullLinkWithTextExists()
    {
        WebElement webElement = null;
        State state = State.ENABLED;
        Mockito.lenient().when(linkValidations.assertIfLinkWithTextExists(webElement, TEXT)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithTextExists(state, TEXT);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                state.getExpectedCondition(webElement));
    }

    @Test
    void testIfLinkExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(LINK_URL, URL);
        webUiLinkSteps.assertLinkExists(searchAttributes);
        verify(linkValidations).assertIfLinkExists(webElement, searchAttributes);
    }

    @Test
    void testIfStateLinkWithUrlExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(LINK_URL, URL);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithUrlExists(State.ENABLED, URL);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullLinkWithUrlExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(LINK_URL, URL);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(null);
        webUiLinkSteps.ifStateLinkWithUrlExists(State.ENABLED, URL);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfLinkWithTextAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextAndTooltipExists(TEXT, TOOLTIP);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, TEXT)
                .addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        verify(linkValidations).assertIfLinkExists(webElement, attributes);
    }

    @Test
    void testIfStateLinkWithTextAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, TEXT)
                .addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithTextAndTooltipExists(State.ENABLED, TEXT, TOOLTIP);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullLinkWithTextAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, TEXT)
                .addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(null);
        webUiLinkSteps.ifStateLinkWithTextAndTooltipExists(State.ENABLED, TEXT, TOOLTIP);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfLinkWithUrlAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithUrlAndTooltipExists(URL, TOOLTIP);
        SearchAttributes attributes = new SearchAttributes(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP,
                TOOLTIP);
        verify(linkValidations).assertIfLinkExists(webElement, attributes);
    }

    @Test
    void testIfStateLinkWithUrlAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP,
                TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithUrlAndTooltipExists(State.ENABLED, URL, TOOLTIP);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullLinkWithUrlAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP,
                TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(null);
        webUiLinkSteps.ifStateLinkWithUrlAndTooltipExists(State.ENABLED, URL, TOOLTIP);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfLinkWithTextAndUriAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextAndUriAndTooltipExists(LINK_TEXT, URL, TOOLTIP);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        verify(linkValidations).assertIfLinkExists(webElement, attributes);
    }

    @Test
    void testIfStateLinkWithTextAndUriAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithTextAndUriAndTooltipExists(State.ENABLED, LINK_TEXT, URL, TOOLTIP);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullLinkWithTextAndUriAndTooltipExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        when(linkValidations.assertIfLinkExists(webElement, attributes)).thenReturn(null);
        webUiLinkSteps.ifStateLinkWithTextAndUriAndTooltipExists(State.ENABLED, LINK_TEXT, URL, TOOLTIP);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfStateLinkWithTextAndUrlPartExists()
    {
        LinkSteps spy = Mockito.spy(webUiLinkSteps);
        when(spy.ifLinkWithTextAndUrlPartExists(TEXT, URL_PART)).thenReturn(webElement);
        spy.ifStateLinkWithTextAndUrlPartExists(State.ENABLED, TEXT, URL_PART);
        verify(baseValidations).assertElementState(THE_FOUND_LINK_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfLinkWithTextAndUrlPartExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextAndUrlPartExists(LINK_TEXT, URL_PART);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL_PART, URL_PART);
        verify(linkValidations).assertIfLinkExists(webElement, searchAttributes);
    }

    @Test
    void testIfStateNullLinkWithTextAndUrlPartExists()
    {
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT)
                .addFilter(LINK_URL_PART, URL_PART);
        Mockito.lenient().when(linkValidations.assertIfLinkExists(searchContext, searchAttributes))
                .thenReturn(webElement);
        webUiLinkSteps.ifStateLinkWithTextAndUrlPartExists(State.ENABLED, LINK_TEXT, URL_PART);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_LINK_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testDoesNotLinkWithTextExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.doesNotLinkWithTextExist(TEXT);
        verify(linkValidations).assertIfLinkWithTextNotExists(webElement, TEXT);
    }

    @Test
    void testDoesNotLinkWithTextAndUrlExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.doesNotLinkWithTextAndUrlExist(LINK_TEXT, URL);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, LINK_TEXT).addFilter(LINK_URL,
                URL);
        verify(linkValidations).assertIfLinkDoesNotExist(webElement, attributes);
    }

    @Test
    void testDoesNotLinkWithUrlAndTooltipExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.doesNotLinkWithUrlAndTooltipExist(URL, TOOLTIP);
        SearchAttributes attributes = new SearchAttributes(LINK_URL, URL).addFilter(ActionAttributeType.TOOLTIP,
                TOOLTIP);
        verify(linkValidations).assertIfLinkDoesNotExist(webElement, attributes);
    }

    @Test
    void testDoesNotLinkWithTextAndUrlAndTooltipExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.doesNotLinkWithTextAndUrlAndTooltipExist(TEXT, URL, TOOLTIP);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, TEXT).addFilter(LINK_URL, URL)
                .addFilter(ActionAttributeType.TOOLTIP, TOOLTIP);
        verify(linkValidations).assertIfLinkDoesNotExist(webElement, attributes);
    }

    @Test
    void testIfTagLinkExists()
    {
        webUiLinkSteps.ifTagLinkExists("href");
        verify(baseValidations).assertIfElementExists("A link tag with href 'href'",
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(LocatorUtil.getXPath(".//link[@href=\"href\"]"))
                                .setVisibility(Visibility.ALL)));
    }

    @Test
    void testIfLinkWithUrlDoesntExist()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithUrlDoesntExist(URL);
        verify(linkValidations).assertIfLinkDoesNotExist(webElement, new SearchAttributes(LINK_URL, URL));
    }

    @Test
    void testMouseOverLihnkText()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkWithTextExists(webElement, LINK_TEXT)).thenReturn(webElement);
        webUiLinkSteps.mouseOverLinkText(LINK_TEXT);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testMouseOverUriLink()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(LINK_URL, URL);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(webElement);
        webUiLinkSteps.mouseOverUriLink(URL);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testClickByXSSValue()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        SearchAttributes searchAttributes = new SearchAttributes(ActionAttributeType.CSS_SELECTOR, TEXT);
        when(linkValidations.assertIfLinkExists(webElement, searchAttributes)).thenReturn(webElement);
        webUiLinkSteps.clickLinkByCss(TEXT);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testClickLinkImageTooltip()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        when(linkValidations.assertIfLinkExists(eq(webElement), any(SearchAttributes.class))).thenReturn(webElement);
        webUiLinkSteps.clickLinkImageTooltip(TOOLTIP);
        verify(mouseActions).click(webElement);
    }

    @Test
    void testIfLinkItemsWithTextAndLink()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        ExamplesTable expectedLinkItems = new ExamplesTable("|text|link|\n|Home|/|\n");
        webUiLinkSteps.ifLinkItemsWithTextAndLink(expectedLinkItems);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, HOME).addFilter(LINK_URL,
                SLASH);
        verify(linkValidations).assertIfLinkExists(webElement, attributes);
    }

    @Test
    void testIfLinkItemsWithTextExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        ExamplesTable expectedLinkItems = new ExamplesTable("|text|\n|Home|\n|");
        webUiLinkSteps.ifLinkItemsWithTextExists(expectedLinkItems);
        verify(linkValidations).assertIfLinkWithTextExists(webElement, HOME);
    }
}

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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.ActionAttributeType.LINK_URL;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.ILinkValidations;

import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;

@SuppressWarnings("checkstyle:methodcount")
@ExtendWith(MockitoExtension.class)
class LinkStepsTests
{
    private static final String HOME = "Home";
    private static final String TEXT = "text";
    private static final String URL = "url";
    private static final String LINK_TEXT = "linkText";
    private static final String SLASH = "/";

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private ILinkValidations linkValidations;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private LinkSteps webUiLinkSteps;

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
    void testIfLinkWithTextExists()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        webUiLinkSteps.ifLinkWithTextExists(TEXT);
        verify(linkValidations).assertIfLinkWithTextExists(webElement, TEXT);
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

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

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;

@ExtendWith(MockitoExtension.class)
class LinkStepsTests
{
    private static final String HOME = "Home";
    private static final String SLASH = "/";

    @Mock
    private IBaseValidations baseValidations;

    @InjectMocks
    private LinkSteps webUiLinkSteps;

    @Test
    void testIfLinkItemsWithTextAndLink()
    {
        ExamplesTable expectedLinkItems = new ExamplesTable("|text|link|\n|Home|/|\n");
        webUiLinkSteps.ifLinkItemsWithTextAndLink(expectedLinkItems);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, HOME).addFilter(
                ActionAttributeType.LINK_URL, SLASH);
        verify(baseValidations).assertIfElementExists("Link with attributes: " + attributes, attributes);
    }

    @Test
    void testIfLinkItemsWithTextExists()
    {
        ExamplesTable expectedLinkItems = new ExamplesTable("|text|\n|Home|\n|");
        webUiLinkSteps.ifLinkItemsWithTextExists(expectedLinkItems);
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.LINK_TEXT, HOME);
        verify(baseValidations).assertIfElementExists("Link with text '" + HOME + "'", attributes);
    }
}

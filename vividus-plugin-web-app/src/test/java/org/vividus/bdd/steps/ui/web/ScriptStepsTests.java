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
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;

@ExtendWith(MockitoExtension.class)
class ScriptStepsTests
{
    private static final String TEST = "test";

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private WebElement mockedScript;

    @InjectMocks
    private ScriptSteps scriptSteps;

    @Test
    void testThenJavascriptFileWithNameIsIncludedInTheSourceCode()
    {
        when(baseValidations.assertIfElementExists("Script with the name 'test'",
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(".//script[contains(normalize-space(@src), \"test\")]", Visibility.ALL))))
                .thenReturn(mockedScript);
        assertEquals(scriptSteps.thenJavascriptFileWithNameIsIncludedInTheSourceCode(TEST), mockedScript);
    }

    @Test
    void testThenJavascriptFileWithTextIsIncludedInTheSourceCode()
    {
        when(baseValidations.assertIfElementExists("Script with text 'test'",
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(".//script[text()[normalize-space()=\"test\"]]", Visibility.ALL))))
                .thenReturn(mockedScript);
        assertEquals(scriptSteps.thenJavascriptFileWithTextIsIncludedInTheSourceCode(TEST), mockedScript);
    }

    @Test
    void testThenJavascriptWithTextPartIsIncludedInTheSourceCode()
    {
        when(baseValidations.assertIfElementExists("Script with the text part 'test'",
                new SearchAttributes(ActionAttributeType.XPATH,
                        new SearchParameters(".//script[contains(normalize-space(text()),\"test\")]", Visibility.ALL))))
                .thenReturn(mockedScript);
        assertEquals(scriptSteps.thenJavascriptWithTextPartIsIncludedInTheSourceCode(TEST), mockedScript);
    }
}

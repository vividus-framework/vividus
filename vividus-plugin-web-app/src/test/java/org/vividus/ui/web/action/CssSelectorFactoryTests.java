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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class CssSelectorFactoryTests
{
    private static final String CSS_SELECTOR1 = ".css-selector1";

    private static final String SHARP_TWO = "#2";
    private static final List<String> CSS_SELECTORS = Arrays.asList(CSS_SELECTOR1, SHARP_TWO);

    private static final String CSS_SELECTOR_FACTORY_JS = "css-selector-factory.js";

    @Mock
    private IJavascriptActions javascriptActions;

    @InjectMocks
    private CssSelectorFactory cssSelectorFactory;

    @Test
    void testGetCssSelectorForElement()
    {
        WebElement element = mock(WebElement.class);
        when(javascriptActions.executeScriptFromResource(CssSelectorFactory.class, CSS_SELECTOR_FACTORY_JS, element))
                .thenReturn(SHARP_TWO);
        assertEquals(SHARP_TWO, cssSelectorFactory.getCssSelector(element));
    }

    @Test
    void testGetCssSelectorForElements()
    {
        WebElement element1 = mock(WebElement.class);
        WebElement element2 = mock(WebElement.class);
        doReturn(CSS_SELECTORS).when(javascriptActions).executeScriptFromResource(CssSelectorFactory.class,
                CSS_SELECTOR_FACTORY_JS, Arrays.asList(element1, element2));
        assertEquals(".css-selector1,#2", cssSelectorFactory.getCssSelector(Arrays.asList(element1, element2)));
    }

    @Test
    void testGetCssSelectorsOf()
    {
        WebElement webElement1 = mock(WebElement.class);
        WebElement webElement2 = mock(WebElement.class);
        doReturn(CSS_SELECTORS).when(javascriptActions).executeScriptFromResource(CssSelectorFactory.class,
                CSS_SELECTOR_FACTORY_JS, Arrays.asList(webElement1, webElement2));
        assertEquals(Arrays.asList(CSS_SELECTOR1, SHARP_TWO), cssSelectorFactory
                .getCssSelectors(Arrays.asList(webElement1, webElement2))
                .collect(Collectors.toList()));
    }
}

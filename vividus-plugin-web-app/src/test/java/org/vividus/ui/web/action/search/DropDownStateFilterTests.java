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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class DropDownStateFilterTests
{
    @Mock
    private WebElement webElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver webDriver;

    @InjectMocks
    private DropDownStateFilter filter;

    @Test
    void testStateFilter()
    {
        when(webElement.getTagName()).thenReturn("select");
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = filter.filter(webElements, "SINGLE_SELECT");
        assertEquals(webElements, foundElements);
    }

    @Test
    void testStateElementFilteredOut()
    {
        List<WebElement> webElements = List.of(webElement);
        List<WebElement> foundElements = filter.filter(webElements, "ENABLED");
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testStateFilterNull()
    {
        filter.filter(List.of(webElement), null);
        verifyNoInteractions(webDriver);
    }

    @Test
    void testStateFilterEmpty()
    {
        filter.filter(List.of(webElement), "");
        verifyNoInteractions(webDriver);
    }
}

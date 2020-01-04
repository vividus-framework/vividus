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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class StateFilterTests
{
    private List<WebElement> webElements;

    @Mock
    private WebElement webElement;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private WebDriver webDriver;

    @InjectMocks
    private StateFilter filter;

    @BeforeEach
    void beforeEach()
    {
        webElements = List.of(webElement);
    }

    @ParameterizedTest
    @CsvSource({"DISABLED", "NOT_SELECTED", "NOT_VISIBLE"})
    void testStateFilter(String stateName)
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> foundElements = filter.filter(webElements, stateName);
        assertEquals(webElements, foundElements);
    }

    @ParameterizedTest
    @CsvSource({"ENABLED", "SELECTED", "VISIBLE"})
    void testStateElementFilteredOut(String stateName)
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        List<WebElement> foundElements = filter.filter(webElements, stateName);
        assertNotEquals(webElements, foundElements);
    }

    @Test
    void testStateFilterNull()
    {
        filter.filter(webElements, null);
        verifyNoInteractions(webDriver);
    }
}

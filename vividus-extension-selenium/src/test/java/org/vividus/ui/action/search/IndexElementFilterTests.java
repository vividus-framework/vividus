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

package org.vividus.ui.action.search;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

class IndexElementFilterTests
{
    private final IndexElementFilter filter = new IndexElementFilter();

    @Test
    void shouldReturnLocatorType()
    {
        assertEquals(GenericLocatorType.INDEX, filter.getType());
    }

    @Test
    void shouldFilterElementByIndex()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> filteredElements = filter.filter(List.of(webElement), "1");
        assertThat(filteredElements, hasSize(1));
        assertEquals(webElement, filteredElements.get(0));
    }

    @Test
    void shouldReturnEmptyListIfElementByIndexDoesNotExist()
    {
        WebElement webElement = mock(WebElement.class);
        List<WebElement> filteredElements = filter.filter(List.of(webElement), "10");
        assertThat(filteredElements, empty());
    }

    @Test
    void shouldThrowExceptionIfIndexIsLessThanOne()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> filter.filter(List.of(), "0"));
        assertEquals("Index must be greater than zero", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfIndexIsNotANumber()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> filter.filter(List.of(), "Java"));
        assertEquals("Index must be a positive number", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfMatchesIsInvoked()
    {
        assertThrows(UnsupportedOperationException.class, () -> filter.matches(null, null));
    }
}

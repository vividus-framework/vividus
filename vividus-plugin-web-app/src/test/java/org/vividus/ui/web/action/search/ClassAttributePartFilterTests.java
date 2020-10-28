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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;

@ExtendWith(MockitoExtension.class)
class ClassAttributePartFilterTests
{
    private static final String TEXT = "text";
    private static final String CLASS_ATTRIBUTE = "class";

    private List<WebElement> elementsList;

    @Mock
    private WebElement element;

    @InjectMocks
    private ClassAttributePartFilter classAttributePartFilter;

    @BeforeEach
    void beforeEach()
    {
        elementsList = new ArrayList<>();
        elementsList.add(element);
    }

    @Test
    void testDirectionTypeFilter()
    {
        WebElement elementContainsDirectionType = Mockito.mock(WebElement.class);
        elementsList.add(elementContainsDirectionType);
        when(elementContainsDirectionType.getAttribute(CLASS_ATTRIBUTE)).thenReturn(TEXT);
        when(element.getAttribute(CLASS_ATTRIBUTE)).thenReturn("anyText");
        List<WebElement> filteredElements = classAttributePartFilter.filter(elementsList, TEXT);
        elementsList.remove(element);
        assertEquals(elementsList, filteredElements);
    }

    @Test
    void testDirectionTypeFilterNullDirectionType()
    {
        assertEquals(classAttributePartFilter.filter(elementsList, null), elementsList);
        verify(element, never()).getAttribute(CLASS_ATTRIBUTE);
    }

    @Test
    void testDirectionTypeFilterEmptyDirectionType()
    {
        assertEquals(classAttributePartFilter.filter(elementsList, ""), elementsList);
        verify(element, never()).getAttribute(CLASS_ATTRIBUTE);
    }

    @Test
    void shouldReturnLocatorType()
    {
        assertEquals(WebLocatorType.CLASS_ATTRIBUTE_PART, classAttributePartFilter.getType());
    }
}

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

package org.vividus.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.SearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.util.LocatorConversionUtils;

@ExtendWith(MockitoExtension.class)
class ElementUtilTests
{
    @Mock private LocatorConversionUtils conversionUtils;
    @InjectMocks private ElementUtil util;

    @Test
    void shouldCreateMemoizedSupplierForWebElement()
    {
        Optional<WebElement> expected = Optional.of(mock(WebElement.class));
        SearchActions searchActions = mock(SearchActions.class);
        Locator attributes = mock(Locator.class);
        when(searchActions.findElement(attributes)).thenReturn(expected);
        String value = "value";
        when(conversionUtils.convertToLocator(value)).thenReturn(attributes);
        Supplier<Optional<WebElement>> elementSupplier = util.getElement(value, searchActions);
        assertEquals(expected, elementSupplier.get());
        assertEquals(expected, elementSupplier.get());
        verify(searchActions).findElement(attributes);
    }

    @Test
    void shouldReturnRelativeToParentWidthInPercents()
    {
        WebElement child = mock(WebElement.class);
        WebElement parent = mock(WebElement.class);
        when(child.getSize()).thenReturn(new Dimension(10, 0));
        when(parent.getSize()).thenReturn(new Dimension(25, 0));
        assertEquals(40, ElementUtil.getElementWidthInPerc(parent, child));
    }
}

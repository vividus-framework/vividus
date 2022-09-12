/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.event.WebDriverCreateEvent;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverStartContextTests
{
    private static final String ANY_VALUE = "value";

    @Mock private WebDriverStartParameter parameter;

    private WebDriverStartContext webDriverStartContext;

    @BeforeEach
    void init()
    {
        webDriverStartContext = new WebDriverStartContext(new SimpleTestContext());
    }

    @Test
    void shouldGetAddedParameter()
    {
        webDriverStartContext.put(parameter, ANY_VALUE);
        assertEquals(ANY_VALUE, webDriverStartContext.get(parameter));
    }

    @Test
    void shouldGetParameterWithInitialValue()
    {
        assertEquals(ANY_VALUE, webDriverStartContext.get(parameter, () -> ANY_VALUE));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldFailWhenGetWithNullInitialValueSupplier()
    {
        Supplier<Object> supplier = mock(Supplier.class);
        when(parameter.getInitialValueSupplier()).thenReturn(supplier);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> webDriverStartContext.get(parameter, () -> ANY_VALUE));
        assertEquals("Initial value supplier for parameter '" + parameter + "' is not null",
                exception.getMessage());
    }

    @Test
    void shouldGetWithInitialValueSupplier()
    {
        Supplier<Object> valueSupplier = () -> ANY_VALUE;
        when(parameter.getInitialValueSupplier()).thenReturn(valueSupplier);
        assertEquals(ANY_VALUE, webDriverStartContext.get(parameter));
    }

    @Test
    void shouldResetOnWebDriverCreate()
    {
        webDriverStartContext.put(parameter, ANY_VALUE);
        assertEquals(ANY_VALUE, webDriverStartContext.get(parameter));
        WebDriverCreateEvent event = new WebDriverCreateEvent(null);
        webDriverStartContext.onWebDriverCreate(event);
        assertNull(webDriverStartContext.get(parameter));
    }
}

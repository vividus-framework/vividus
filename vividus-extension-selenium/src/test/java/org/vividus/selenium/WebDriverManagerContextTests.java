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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.selenium.manager.WebDriverManagerParameter;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerContextTests
{
    private static final String ANY_VALYE = "value";

    @Mock private TestContext testContext;
    @InjectMocks private WebDriverManagerContext webDriverManagerContext;

    @Test
    void testGetParameter()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.SCREEN_SIZE;
        webDriverManagerContext.getParameter(param);
        verify(testContext).get(param.getContextKey());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetParameterWithInitialValue()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.DESIRED_CAPABILITIES;
        webDriverManagerContext.getParameter(param);
        verify(testContext).get(eq(param.getContextKey()), any(Supplier.class));
    }

    @Test
    void testPutParameter()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.SCREEN_SIZE;
        webDriverManagerContext.putParameter(param, ANY_VALYE);
        verify(testContext).put(param.getContextKey(), ANY_VALYE);
    }

    @Test
    void testReset()
    {
        WebDriverManagerContext spy = Mockito.spy(webDriverManagerContext);
        spy.reset();
        Stream.of(WebDriverManagerParameter.values()).forEach(v -> verify(spy).reset(v));
    }

    @Test
    void testResetParameter()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.SCREEN_SIZE;
        webDriverManagerContext.reset(param);
        verify(testContext).remove(param.getContextKey());
    }

    @Test
    void testGetWithNullInitialValueSupplier()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.DESIRED_CAPABILITIES;
        var exception = assertThrows(IllegalArgumentException.class,
                () -> webDriverManagerContext.get(param, () -> ANY_VALYE));
        assertEquals("Initial value supplier for parameter '" + param.getContextKey() + "' is not null",
                exception.getMessage());
    }

    @Test
    void testGetWithInitialValueSupplier()
    {
        WebDriverManagerParameter param = WebDriverManagerParameter.DEVICE_PIXEL_RATIO;
        Supplier<String> valueSupplier = () -> ANY_VALYE;
        webDriverManagerContext.get(param, valueSupplier);
        verify(testContext).get(param.getContextKey(), valueSupplier);
    }

    @Test
    void testOnWebDriverQuit()
    {
        WebDriverManagerContext spy = Mockito.spy(webDriverManagerContext);
        AfterWebDriverQuitEvent event = new AfterWebDriverQuitEvent(StringUtils.EMPTY);
        spy.onWebDriverQuit(event);
        verify(spy).reset();
    }
}

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

package org.vividus.selenium;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.manager.WebDriverManagerParameter;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerContextTests
{
    @Mock
    private TestContext testContext;

    @InjectMocks
    private WebDriverManagerContext webDriverManagerContext;

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
        WebDriverManagerParameter param = WebDriverManagerParameter.ORIENTATION;
        String value = "value";
        webDriverManagerContext.putParameter(param, value);
        verify(testContext).put(param.getContextKey(), value);
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
        WebDriverManagerParameter param = WebDriverManagerParameter.ORIENTATION;
        webDriverManagerContext.reset(param);
        verify(testContext).remove(param.getContextKey());
    }
}

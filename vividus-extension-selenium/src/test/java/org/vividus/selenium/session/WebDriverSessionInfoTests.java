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

package org.vividus.selenium.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith(MockitoExtension.class)
class WebDriverSessionInfoTests
{
    private static final String ANY_VALUE = "value";

    @Mock private WebDriverSessionAttribute attribute;

    private WebDriverSessionInfo webDriverSessionInfo;

    @BeforeEach
    void init()
    {
        webDriverSessionInfo = new WebDriverSessionInfo(new SimpleTestContext());
    }

    @Test
    void shouldGetAddedAttribute()
    {
        webDriverSessionInfo.put(attribute, ANY_VALUE);
        assertEquals(ANY_VALUE, webDriverSessionInfo.get(attribute));
    }

    @Test
    void shouldResetAddedAttribute()
    {
        webDriverSessionInfo.put(attribute, ANY_VALUE);
        assertEquals(ANY_VALUE, webDriverSessionInfo.get(attribute));
        webDriverSessionInfo.reset(attribute);
        assertNull(webDriverSessionInfo.get(attribute));
    }

    @Test
    void shouldGetAttributeWithInitialValue()
    {
        assertEquals(ANY_VALUE, webDriverSessionInfo.get(attribute, () -> ANY_VALUE));
    }

    @Test
    void shouldResetOnWebDriverQuit()
    {
        webDriverSessionInfo.put(attribute, ANY_VALUE);
        assertEquals(ANY_VALUE, webDriverSessionInfo.get(attribute));
        AfterWebDriverQuitEvent event = new AfterWebDriverQuitEvent(StringUtils.EMPTY);
        webDriverSessionInfo.onWebDriverQuit(event);
        assertNull(webDriverSessionInfo.get(attribute));
    }
}

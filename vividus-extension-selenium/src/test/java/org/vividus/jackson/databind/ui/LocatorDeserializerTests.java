/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.jackson.databind.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.locator.LocatorConverter;

@ExtendWith(MockitoExtension.class)
class LocatorDeserializerTests
{
    @Mock private Locator locator;
    @Mock private JsonParser jsonParser;
    @Mock private LocatorConverter locatorConverter;
    @InjectMocks private LocatorDeserializer deserializer;

    @Test
    void shouldDeserialize() throws IOException
    {
        String input = "input-locator";
        when(jsonParser.getText()).thenReturn(input);
        when(locatorConverter.convertToLocator(input)).thenReturn(locator);
        assertEquals(locator, deserializer.deserialize(jsonParser, null));
    }
}

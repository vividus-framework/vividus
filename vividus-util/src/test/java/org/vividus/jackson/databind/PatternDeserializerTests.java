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

package org.vividus.jackson.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;

import org.junit.jupiter.api.Test;

class PatternDeserializerTests
{
    @Test
    void shouldCreatePattern() throws IOException
    {
        String regex = ".*";
        JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.getText()).thenReturn(regex);
        assertEquals(regex, new PatternDeserializer().deserialize(jsonParser, null).toString());
    }

    @Test
    void shouldThrowExceptionInCaseOfNullPattern()
    {
        JsonParser jsonParser = mock(JsonParser.class);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new PatternDeserializer().deserialize(jsonParser, null).toString());
        assertEquals("Pattern could not be empty", exception.getMessage());
    }
}

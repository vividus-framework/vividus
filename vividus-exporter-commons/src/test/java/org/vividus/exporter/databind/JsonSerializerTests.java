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

package org.vividus.exporter.databind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;

import org.junit.jupiter.api.Test;

class JsonSerializerTests
{
    private static final String FIELD = "field";
    private static final String VALUE = "value";

    @Test
    void writeJsonArrayWithNullValues() throws IOException
    {
        JsonGenerator generator = mock(JsonGenerator.class);
        JsonSerializer.writeJsonArray(generator, FIELD, null, false);
        verifyNoMoreInteractions(generator);
    }

    @Test
    void writeJsonArrayWithFalseWrapValuesAsObject() throws IOException
    {
        JsonGenerator generator = mock(JsonGenerator.class);
        JsonSerializer.writeJsonArray(generator, FIELD, Collections.singleton(VALUE), false);
        verify(generator, times(1)).writeString(VALUE);
    }

    @Test
    void writeJsonArrayWithTrueWrapValuesAsObject() throws IOException
    {
        JsonGenerator generator = mock(JsonGenerator.class);
        JsonSerializer.writeJsonArray(generator, FIELD, Collections.singleton(VALUE), true);
        verify(generator, times(1)).writeStartObject();
    }
}

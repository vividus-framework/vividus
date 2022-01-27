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

package org.vividus.exporter.config.databind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;

import org.junit.jupiter.api.Test;
import org.vividus.exporter.databind.SerializeJsonHelper;

class SerializeJsonHelperTests
{
    private static final  String NAME = "name";
    private static final String VALUE = "value";
    private static final String START_FIELD = "startField";
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final Collection<String> VALUES = Arrays.asList(FIRST, SECOND);

    private final JsonGenerator generator = mock(JsonGenerator.class);

    @Test
    void testSerializeJsonValueWithObjTrue() throws IOException
    {
        SerializeJsonHelper.writeJsonArray(generator, START_FIELD, VALUES, true);
        verify(generator).writeArrayFieldStart(START_FIELD);
        verify(generator).writeStringField(NAME, FIRST);
        verify(generator).writeStringField(NAME, SECOND);
        verify(generator).writeEndArray();
    }

    @Test
    void testSerializeJsonValueWithObjFalse() throws IOException
    {
        SerializeJsonHelper.writeJsonArray(generator, START_FIELD, VALUES, false);
        verify(generator).writeArrayFieldStart(START_FIELD);
        verify(generator).writeString(FIRST);
        verify(generator).writeString(SECOND);
        verify(generator).writeEndArray();
    }

    @Test
    void testSerializeJsonNullValues() throws IOException
    {
        SerializeJsonHelper.writeJsonArray(generator, START_FIELD, null, true);
        verifyNoMoreInteractions(generator);
    }

    @Test
    void testWriteObjectWithField() throws IOException
    {
        SerializeJsonHelper.writeObjectWithField(generator, START_FIELD, NAME, VALUE);
        verify(generator).writeObjectFieldStart(START_FIELD);
        verify(generator).writeStringField(NAME, VALUE);
        verify(generator).writeEndObject();
    }
}

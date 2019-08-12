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

package org.vividus.proxy.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.Test;

import net.lightbody.bmp.core.har.HarNameValuePair;

class HarNameValuePairDeserializerTests
{
    private static final String NAME = "name";
    private static final String VALUE = "value";

    @Test
    void testDeserialize() throws IOException
    {
        JsonParser jsonParser = mock(JsonParser.class);
        ObjectCodec objectCodec = mock(ObjectCodec.class);
        JsonNode jsonNode = mock(JsonNode.class);
        when(jsonParser.getCodec()).thenReturn(objectCodec);
        when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        when(jsonNode.get(NAME)).thenReturn(jsonNode);
        when(jsonNode.get(VALUE)).thenReturn(jsonNode);
        when(jsonNode.asText()).thenReturn(NAME).thenReturn(VALUE);
        HarNameValuePairDeserializer deserializer = new HarNameValuePairDeserializer();
        assertEquals(new HarNameValuePair(NAME, VALUE), deserializer.deserialize(jsonParser, null));
    }
}

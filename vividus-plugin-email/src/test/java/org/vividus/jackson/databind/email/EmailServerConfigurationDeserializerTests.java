/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.jackson.databind.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.email.model.EmailServerConfiguration;

@ExtendWith(MockitoExtension.class)
class EmailServerConfigurationDeserializerTests
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private JsonParser parser;

    @Mock
    private ObjectCodec objectCodec;

    private final EmailServerConfigurationDeserializer deserializer = new EmailServerConfigurationDeserializer();

    @BeforeEach
    void init()
    {
        when(parser.getCodec()).thenReturn(objectCodec);
    }

    @Test
    void testDeserialize() throws IOException
    {
        JsonNode root = MAPPER.readTree(
                "{\"username\":\"Bob\",\"password\":\"pass$123\",\"properties\":"
                        + "{\"simpleKey\":\"simpleValue\","
                        + "\"objectKey\":{\"subKey1\":\"subValue1\", \"subKey2\":\"subValue2\"}}}");

        when(objectCodec.readTree(parser)).thenReturn(root);

        EmailServerConfiguration config = deserializer.deserialize(parser, null);

        assertEquals("Bob", config.getUsername());
        assertEquals("pass$123", config.getPassword());
        assertEquals(
                Map.of("objectKey.subKey1", "subValue1", "objectKey.subKey2", "subValue2", "simpleKey", "simpleValue"),
                config.getProperties());
    }

    @Test
    void testDeserializeMissingProperties() throws IOException
    {
        JsonNode root = MAPPER.readTree("{\"username\":\"Jack\",\"password\":\"pass$228\"}");
        when(objectCodec.readTree(parser)).thenReturn(root);
        EmailServerConfiguration config = deserializer.deserialize(parser, null);
        assertEquals("Jack", config.getUsername());
        assertEquals("pass$228", config.getPassword());
        assertEquals(Map.of(), config.getProperties());
    }

    @ParameterizedTest
    @CsvSource({
        "password, {\"username\":\"-\"}",
        "username, {\"password\":\"-\"}"
    })
    void testRequiredPropertiesAreMissing(String property, String json) throws IOException
    {
        JsonNode root = MAPPER.readTree(json);
        when(objectCodec.readTree(parser)).thenReturn(root);
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> deserializer.deserialize(parser, null));
        String errorMessage = String.format("Required property '%s' is not set", property);
        assertEquals(errorMessage, exception.getMessage());
    }
}

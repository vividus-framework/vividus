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

package org.vividus.email.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.email.model.EmailServerConfiguration;

@ExtendWith(MockitoExtension.class)
class EmailServerConfigurationDeserializerTests
{
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    void testDeserialize() throws IOException
    {
        var config =  MAPPER.readValue(
                "{\"username\":\"Bob\",\"password\":\"pass$123\",\"properties\":"
                        + "{\"simpleKey\":\"simpleValue\","
                        + "\"objectKey\":{\"subKey1\":\"subValue1\", \"subKey2\":\"subValue2\"}}}",
                EmailServerConfiguration.class);

        assertEquals("Bob", config.getUsername());
        assertEquals("pass$123", config.getPassword());
        assertEquals(
                Map.of("objectKey.subKey1", "subValue1", "objectKey.subKey2", "subValue2", "simpleKey", "simpleValue"),
                config.getProperties());
    }

    @Test
    void testDeserializeMissingProperties() throws IOException
    {
        var config = MAPPER.readValue("{\"username\":\"Jack\",\"password\":\"pass$228\"}",
                EmailServerConfiguration.class);
        assertEquals("Jack", config.getUsername());
        assertEquals("pass$228", config.getPassword());
        assertEquals(Map.of(), config.getProperties());
    }

    @ParameterizedTest
    @CsvSource({
        "password, {\"username\":\"-\"}",
        "username, {\"password\":\"-\"}"
    })
    void testRequiredPropertiesAreMissing(String property, String json)
    {
        var exception = assertThrows(IllegalArgumentException.class,
            () -> MAPPER.readValue(json, EmailServerConfiguration.class));
        var errorMessage = String.format("Required property '%s' is not set", property);
        assertEquals(errorMessage, exception.getMessage());
    }
}

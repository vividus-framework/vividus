/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class JsonJackson3UtilsTests
{
    private static final String JSON_STRING = "{\"id\":\"1\",\"firstName\":\"name\",\"dateTime\":1621892175.000000000}";
    private static final JsonUtilsTests.TestClass TEST_OBJECT;

    static
    {
        TEST_OBJECT = new JsonUtilsTests.TestClass();
        TEST_OBJECT.setId("1");
        TEST_OBJECT.setFirstName("name");
        TEST_OBJECT.setDateTime(OffsetDateTime.of(2021, 5, 25, 0, 36, 15, 0, ZoneOffset.of("+3")));
    }

    private final JsonJackson3Utils jsonUtils = new JsonJackson3Utils();

    @Test
    void shouldReadJson()
    {
        assertEquals(String.format("{%n  \"key\" : \"value\"%n}"),
                jsonUtils.readTree("{\"key\" : \"value\"}").toPrettyString());
    }

    @Test
    void shouldConvertObjectToJsonString()
    {
        String actualJson = jsonUtils.toJson(TEST_OBJECT);
        assertEquals(JSON_STRING, actualJson);
    }
}

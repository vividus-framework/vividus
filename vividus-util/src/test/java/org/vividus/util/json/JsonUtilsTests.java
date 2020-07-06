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

package org.vividus.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JsonUtilsTests
{
    private static final String JSON_STRING = "{\"id\":\"1\",\"firstName\":\"name\"}";
    private static final String JSON_LIST_STRING = "[{\"id\":\"1\",\"first_name\":\"name1\"},"
            + " {\"id\":\"2\",\"first_name\":\"name2\"}]";

    private static final TestClass TEST_OBJECT;

    static
    {
        TEST_OBJECT = new TestClass();
        TEST_OBJECT.setId("1");
        TEST_OBJECT.setFirstName("name");
    }

    private JsonUtils jsonUtils = new JsonUtils(PropertyNamingStrategy.LOWER_CAMEL_CASE);

    @Test
    void testToJsonSuccessDefault()
    {
        String actualJson = jsonUtils.toJson(TEST_OBJECT);
        assertEquals(JSON_STRING, actualJson);
    }

    @Test
    void testToPrettyJsonSuccessDefault()
    {
        String actualJson = jsonUtils.toPrettyJson(TEST_OBJECT);
        assertEquals(String.format("{%n  \"id\" : \"1\",%n  \"firstName\" : \"name\"%n}"), actualJson);
    }

    @Test
    void testToObjectSuccess()
    {
        TestClass actualObj = jsonUtils.toObject(JSON_STRING, TestClass.class);
        assertEquals(TEST_OBJECT, actualObj);
    }

    @Test
    void testToObjectInputStreamSuccess()
    {
        InputStream jsonStream = new ByteArrayInputStream(JSON_STRING.getBytes(StandardCharsets.UTF_8));
        TestClass actualObj = jsonUtils.toObject(jsonStream, TestClass.class);
        assertEquals(TEST_OBJECT, actualObj);
    }

    @Test
    void testToObjectInvalidClass()
    {
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObject(JSON_STRING, JsonUtils.class));
    }

    @Test
    void testToObjectInputStreamInvalidClass()
    {
        InputStream jsonStream = new ByteArrayInputStream(JSON_STRING.getBytes(StandardCharsets.UTF_8));
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObject(jsonStream, JsonUtils.class));
    }

    @Test
    void testToObjectListSuccess()
    {
        jsonUtils = new JsonUtils(PropertyNamingStrategy.SNAKE_CASE);
        int expectedSize = 2;
        List<TestClass> actualList = jsonUtils.toObjectList(JSON_LIST_STRING, TestClass.class);
        assertEquals(actualList.size(), expectedSize);
    }

    @Test
    void testToObjectListInputStreamSuccess()
    {
        InputStream jsonListStream = new ByteArrayInputStream(JSON_LIST_STRING.getBytes(StandardCharsets.UTF_8));
        jsonUtils = new JsonUtils(PropertyNamingStrategy.SNAKE_CASE);
        int expectedSize = 2;
        List<TestClass> actualList = jsonUtils.toObjectList(jsonListStream, TestClass.class);
        assertEquals(actualList.size(), expectedSize);
    }

    @Test
    void testToObjectListNotList()
    {
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObjectList(JSON_STRING, TestClass.class));
    }

    @Test
    void testToObjectListInputStreamNotList()
    {
        InputStream jsonStream = new ByteArrayInputStream(JSON_STRING.getBytes(StandardCharsets.UTF_8));
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObjectList(jsonStream, TestClass.class));
    }

    @Test
    void testToObjectListInvalidClass()
    {
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObjectList(JSON_LIST_STRING, TestClass.class));
    }

    @Test
    void testToObjectListInputStreamInvalidClass()
    {
        InputStream jsonListStream = new ByteArrayInputStream(JSON_LIST_STRING.getBytes(StandardCharsets.UTF_8));
        assertThrows(JsonProcessingException.class, () -> jsonUtils.toObjectList(jsonListStream, TestClass.class));
    }

    @ParameterizedTest
    @CsvSource({
            "{},             true",
            "[],             true",
            "' {\"key\":1}', true",
            "{',             false",
            "1',             false"
    })
    void testIsJson(String str, boolean json)
    {
        assertEquals(json, jsonUtils.isJson(str));
    }

    public static class TestClass
    {
        private String id;
        private String firstName;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getFirstName()
        {
            return firstName;
        }

        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id, firstName);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof TestClass))
            {
                return false;
            }
            TestClass other = (TestClass) obj;
            return Objects.equals(id, other.id) && Objects.equals(firstName, other.firstName);
        }
    }
}

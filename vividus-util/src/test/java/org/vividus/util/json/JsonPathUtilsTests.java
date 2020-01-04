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

import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonPathUtilsTests
{
    private static final List<String> VALUES = List.of("value1", "value2");
    private static final String NAME_JSON_PATH = "$..name";

    @Test
    void testGetData()
    {
        String json = "{\"test\":{\"name\":\"value\"}}";
        String path = "$.test.name";
        Assertions.assertEquals("value", JsonPathUtils.getData(json, path));
    }

    @Test
    void testGetListData()
    {
        String json = "{\"test\":[{\"name\":\"value1\"},{\"name\":\"value2\"}]}";
        Assertions.assertEquals(VALUES, JsonPathUtils.getData(json, NAME_JSON_PATH));
    }

    @Test
    void testGetDataByJsonPaths()
    {
        String json = "{\"test\":[{\"name\":\"value1\"},{\"name\":\"value2\"}],\"number\":1,"
                + "\"boolean\":true,\"string\":\"data\"}";
        List<String> jsonPaths = List.of(NAME_JSON_PATH, "$.number", "$.boolean", "$.string");
        List<Object> data = JsonPathUtils.getData(json, jsonPaths);
        Assertions.assertEquals(data, List.of(VALUES, 1, true, "data"));
    }

    @Test
    void testConfiguration()
    {
        JsonPathUtils.setJacksonConfiguration();
        Configuration configuration = Configuration.defaultConfiguration();
        Assertions.assertTrue(configuration.jsonProvider() instanceof JacksonJsonProvider);
        Assertions.assertTrue(configuration.mappingProvider() instanceof JacksonMappingProvider);
    }
}

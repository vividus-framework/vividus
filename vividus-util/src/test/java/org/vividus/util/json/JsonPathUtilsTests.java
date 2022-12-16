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

package org.vividus.util.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.math.BigDecimal;
import java.util.List;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

import org.junit.jupiter.api.Test;

class JsonPathUtilsTests
{
    private static final String JSON = "{\"test\":[{\"name\":\"value1\"},{\"name\":\"value2\"}],"
            + "\"int\":1,"
            + "\"float\":485690.3866338789319252000000135498000000,"
            + "\"boolean\":true,"
            + "\"string\":\"data\""
            + "}";

    private static final String NAME_JSON_PATH = "$..name";
    private static final List<String> NAME_VALUES = List.of("value1", "value2");

    @Test
    void testGetData()
    {
        var json = "{\"test\":{\"name\":\"value\"}}";
        var path = "$.test.name";
        assertEquals("value", JsonPathUtils.getData(json, path));
    }

    @Test
    void testGetListData()
    {
        assertEquals(NAME_VALUES, JsonPathUtils.getData(JSON, NAME_JSON_PATH));
    }

    @Test
    void testGetDataByJsonPaths()
    {
        var jsonPaths = List.of(NAME_JSON_PATH, "$.int", "$.float", "$.boolean", "$.string");
        var expected = List.of(NAME_VALUES, 1, new BigDecimal("485690.3866338789319252000000135498000000"), true,
                "data");
        assertEquals(expected, JsonPathUtils.getData(JSON, jsonPaths));
    }

    @Test
    void testConfiguration()
    {
        JsonPathUtils.setJacksonConfiguration();
        var configuration = Configuration.defaultConfiguration();
        assertInstanceOf(JacksonJsonProvider.class, configuration.jsonProvider());
        assertInstanceOf(JacksonMappingProvider.class, configuration.mappingProvider());
    }
}

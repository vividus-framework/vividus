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

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public final class JsonPathUtils
{
    static
    {
        setJacksonConfiguration();
    }

    private JsonPathUtils()
    {
    }

    /**
     * Gets data from feed using jsonPath
     * @param <T> resulting type
     * @param json JSON String
     * @param jsonPath JSON path
     * @return desired data from JSON
     */
    public static <T> T getData(String json, String jsonPath)
    {
        return JsonPath.read(json, jsonPath);
    }

    /**
     * Gets data from feed using jsonPaths
     * @param <T> resulting type
     * @param json JSON String
     * @param jsonPaths collection of JSON paths
     * @return list with results found by JSON paths
     */
    public static <T> List<T> getData(String json, Collection<String> jsonPaths)
    {
        DocumentContext jsonPathContext = JsonPath.parse(json);
        return jsonPaths.stream().map(jsonPathContext::<T>read).collect(Collectors.toList());
    }

    public static void setJacksonConfiguration()
    {
        Configuration.setDefaults(new JacksonConfiguration());
    }

    private static final class JacksonConfiguration implements Configuration.Defaults
    {
        private final JsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        private final MappingProvider jacksonMappingProvider = new JacksonMappingProvider();

        @Override
        public JsonProvider jsonProvider()
        {
            return jacksonJsonProvider;
        }

        @Override
        public MappingProvider mappingProvider()
        {
            return jacksonMappingProvider;
        }

        @Override
        public Set<Option> options()
        {
            return EnumSet.noneOf(Option.class);
        }
    }
}

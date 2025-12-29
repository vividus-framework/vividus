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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

public class JsonJackson3Utils
{
    private final JsonMapper jsonMapper;

    public JsonJackson3Utils()
    {
        this(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    public JsonJackson3Utils(PropertyNamingStrategy propertyNamingStrategy)
    {
        jsonMapper = JsonMapper.builder()
                // For backward-compatibility, to remove in 0.7.0
                .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                // For backward-compatibility, to remove in 0.7.0
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .propertyNamingStrategy(propertyNamingStrategy).build();
    }

    public JsonNode readTree(String jsonString)
    {
        return jsonMapper.readTree(jsonString);
    }

    public String toJson(Object object)
    {
        return jsonMapper.writeValueAsString(object);
    }
}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.type.CollectionType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableSupplier;

public class JsonUtils
{
    private final ObjectMapper mapper;

    public JsonUtils()
    {
        this(PropertyNamingStrategies.LOWER_CAMEL_CASE);
    }

    public JsonUtils(ObjectMapper mapper)
    {
        this.mapper = mapper;
    }

    public JsonUtils(PropertyNamingStrategy namingStrategy)
    {
        this(new ObjectMapper().findAndRegisterModules());
        mapper.setPropertyNamingStrategy(namingStrategy);
    }

    public String toJson(Object object)
    {
        return performOperation(() -> mapper.writeValueAsString(object));
    }

    public byte[] toJsonAsBytes(Object object)
    {
        return performOperation(() -> mapper.writeValueAsBytes(object));
    }

    public String toPrettyJson(Object object)
    {
        return performOperation(() -> mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
    }

    public <T> T toObject(String json, Class<T> clazz)
    {
        return performOperation(() -> mapper.readValue(json, clazz));
    }

    public <T> List<T> toObjectList(String json, Class<T> clazz)
    {
        return performOperation(() -> mapper.readValue(json, constructListType(clazz)));
    }

    public <T> T toObject(InputStream json, Class<T> clazz)
    {
        return performOperation(() -> mapper.readValue(json, clazz));
    }

    public <T> List<T> toObjectList(InputStream json, Class<T> clazz)
    {
        return performOperation(() -> mapper.readValue(json, constructListType(clazz)));
    }

    public boolean isJson(String str)
    {
        try
        {
            mapper.readTree(str);
        }
        catch (IOException e)
        {
            return false;
        }
        // Single number ("1") is valid JSON as well, but we can't guarantee that it's JSON actually
        return StringUtils.startsWithAny(str.trim(), "[", "{");
    }

    public JsonNode readTree(String jsonString)
    {
        return performOperation(() -> mapper.readTree(jsonString));
    }

    private <T> T performOperation(FailableSupplier<T, IOException> operation)
    {
        try
        {
            return operation.get();
        }
        catch (IOException e)
        {
            throw new JsonProcessingException(e.getMessage(), e);
        }
    }

    private <T> CollectionType constructListType(Class<T> clazz)
    {
        return mapper.getTypeFactory().constructCollectionType(List.class, clazz);
    }
}

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

package org.vividus.util.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;

public class JsonUtils implements IJsonUtils
{
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonUtils()
    {
        // Required, since non-default constructors are present as well
    }

    public JsonUtils(PropertyNamingStrategy namingStrategy)
    {
        mapper.setPropertyNamingStrategy(namingStrategy);
    }

    public JsonUtils(PropertyNamingStrategy namingStrategy, Map<SerializationFeature, Boolean> serializationFeatures)
    {
        this(namingStrategy);
        serializationFeatures.forEach(mapper::configure);
    }

    @Override
    public String toJson(Object object) throws JsonProcessingException
    {
        return performOperation(() -> mapper.writeValueAsString(object));
    }

    @Override
    public <T> T toObject(String json, Class<T> clazz) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readValue(json, clazz));
    }

    @Override
    public <T> List<T> toObjectList(String json, Class<T> clazz) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readValue(json, constructListType(clazz)));
    }

    @Override
    public <T> T toObject(InputStream json, Class<T> clazz) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readValue(json, clazz));
    }

    @Override
    public <T> List<T> toObjectList(InputStream json, Class<T> clazz) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readValue(json, constructListType(clazz)));
    }

    @Override
    public JsonNode toJson(String jsonString) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readTree(jsonString));
    }

    @Override
    public JsonNode toJson(byte[] jsonBytes) throws JsonProcessingException
    {
        return performOperation(() -> mapper.readTree(jsonBytes));
    }

    private <T> T performOperation(JsonOperation<T> operation) throws JsonProcessingException
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

    public void setNamingStrategy(PropertyNamingStrategy namingStrategy)
    {
        mapper.setPropertyNamingStrategy(namingStrategy);
    }

    public void setSerializationFeature(SerializationFeature serializationFeature, boolean enable)
    {
        mapper.configure(serializationFeature, enable);
    }

    private interface JsonOperation<T>
    {
        T get() throws IOException;
    }
}

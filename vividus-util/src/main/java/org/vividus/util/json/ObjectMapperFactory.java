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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class ObjectMapperFactory
{
    private ObjectMapperFactory()
    {
    }

    public static ObjectMapper createWithCaseInsensitiveEnumDeserializer()
    {
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier()
        {
            @Override
            public JsonDeserializer<Enum<?>> modifyEnumDeserializer(DeserializationConfig config, final JavaType type,
                    BeanDescription beanDescription, final JsonDeserializer<?> deserializer)
            {
                return new JsonDeserializer<>()
                {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    @Override
                    public Enum<?> deserialize(JsonParser parser, DeserializationContext context) throws IOException
                    {
                        return Enum.valueOf((Class<? extends Enum>) type.getRawClass(),
                                parser.getValueAsString().toUpperCase());
                    }
                };
            }
        });
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        return objectMapper;
    }
}

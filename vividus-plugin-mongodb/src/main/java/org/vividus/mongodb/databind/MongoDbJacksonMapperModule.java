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

package org.vividus.mongodb.databind;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.bson.types.ObjectId;

public class MongoDbJacksonMapperModule extends SimpleModule
{
    private static final long serialVersionUID = -8454324208644523711L;

    public MongoDbJacksonMapperModule()
    {
        addSerializer(ObjectId.class, new JsonSerializer<>()
        {
            @Override
            public void serialize(ObjectId value, JsonGenerator generator, SerializerProvider serializers)
                    throws IOException
            {
                generator.writeString(value.toString());
            }
        });
    }
}

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

package org.vividus.proxy.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.proxy.deserializer.HarNameValuePairDeserializer;
import org.vividus.proxy.deserializer.HarNameVersionDeserializer;

import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarNameVersion;

public class HarParser implements IHarParser
{
    private final ObjectMapper objectMapper;

    public HarParser(List<DeserializationProblemHandler> deserializationProblemHandlers)
    {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(HarNameValuePair.class, new HarNameValuePairDeserializer());
        module.addDeserializer(HarNameVersion.class, new HarNameVersionDeserializer());
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        deserializationProblemHandlers.forEach(objectMapper::addHandler);
    }

    @Override
    public Har parseHar(InputStream inputStream) throws IOException
    {
        return objectMapper.readValue(inputStream, Har.class);
    }
}

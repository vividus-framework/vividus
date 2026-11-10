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

package org.vividus.databind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.apache.hc.core5.http.message.BasicHeader;

public class BasicHeadersDeserializer extends JsonDeserializer<Collection<BasicHeader>>
{
    @Override
    public Collection<BasicHeader> deserialize(JsonParser parser, DeserializationContext context) throws IOException
    {
        JsonNode node = parser.getCodec().readTree(parser);
        Collection<BasicHeader> headers = new ArrayList<>();
        node.properties().forEach(f ->
        {
            String headerName = f.getKey();
            String headerValue = f.getValue().asText();
            headers.add(new BasicHeader(headerName, headerValue));
        });
        return headers;
    }
}

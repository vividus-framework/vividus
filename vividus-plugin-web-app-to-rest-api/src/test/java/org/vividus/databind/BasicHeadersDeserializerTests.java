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

package org.vividus.databind;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.hc.core5.http.message.BasicHeader;
import org.junit.jupiter.api.Test;

class BasicHeadersDeserializerTests
{
    @Test
    void shouldDeserializerHeaders() throws IOException
    {
        String json = """
            {
                "header1": "value1",
                "header2": "value2"
            }
            """;

        JsonParser parser  = new JsonFactory().createParser(json);
        parser.setCodec(new ObjectMapper());

        Collection<BasicHeader> headers = new BasicHeadersDeserializer().deserialize(parser, null);
        assertThat(headers, hasSize(2));
        BasicHeader header1 = (BasicHeader) headers.toArray()[0];
        assertEquals("header1", header1.getName());
        assertEquals("value1", header1.getValue());
        BasicHeader header2 = (BasicHeader) headers.toArray()[1];
        assertEquals("header2", header2.getName());
        assertEquals("value2", header2.getValue());
    }
}

/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.azure.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Map;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.junit.jupiter.api.Test;

class InnersJacksonAdapterTests
{
    @Test
    void shouldSerializeToJson() throws IOException
    {
        var inputJson =
                "{\"name\": \"azure-resource\", \"properties\": {\"primaryLocation\": \"eastus\"}, \"tags\": {}}";
        var jacksonAdapter = new InnersJacksonAdapter();
        var inner = (TestInner) jacksonAdapter.deserialize(inputJson, TestInner.class, SerializerEncoding.JSON);
        inner.withPrimaryLocation("westus");
        String outputJson = jacksonAdapter.serializeToJson(inner);
        assertEquals("{\"name\":\"azure-resource\",\"tags\":{},\"properties\":{\"primaryLocation\":\"westus\"}}",
                outputJson);
    }

    @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.AvoidFieldNameMatchingMethodName" })
    @JsonFlatten
    @Fluent
    public static class TestInner
    {
        @JsonIgnore
        private final ClientLogger logger = new ClientLogger(TestInner.class);

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String name;

        @JsonProperty(value = "properties.primaryLocation", access = JsonProperty.Access.WRITE_ONLY)
        private String primaryLocation;

        private Map<String, String> tags;

        public String name()
        {
            return this.name;
        }

        public String primaryLocation()
        {
            return this.primaryLocation;
        }

        public TestInner withPrimaryLocation(String primaryLocation)
        {
            this.primaryLocation = primaryLocation;
            return this;
        }

        public Map<String, String> tags()
        {
            return this.tags;
        }

        public TestInner withTags(Map<String, String> tags)
        {
            this.tags = tags;
            return this;
        }
    }
}

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

package org.vividus.jira.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.vividus.jira.model.IssueLink;
import org.vividus.util.ResourceUtils;

class IssueLinkSerializerTests
{
    @Test
    void shouldSerializeIssueLink() throws IOException
    {
        IssueLinkSerializer serializer = new IssueLinkSerializer();
        IssueLink issueLink = new IssueLink("link-type", "inward-key", "outward-key");

        try (StringWriter writer = new StringWriter())
        {
            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(writer);

            serializer.serialize(issueLink, generator, null);

            generator.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode actual = mapper.readTree(writer.toString());
            JsonNode expected = mapper.readTree(ResourceUtils.loadResource(getClass(), "data.json"));
            assertEquals(expected, actual);
        }
    }
}

/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.jira.mapper;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.vividus.jira.databind.IssueLinkDeserializer;
import org.vividus.jira.databind.IssueLinkSerializer;
import org.vividus.jira.databind.JiraEntityDeserializer;
import org.vividus.jira.model.IssueLink;
import org.vividus.jira.model.JiraEntity;

public final class JiraEntityMapper
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new SimpleModule()
                    .addSerializer(IssueLink.class, new IssueLinkSerializer())
                    .addDeserializer(IssueLink.class, new IssueLinkDeserializer())
                    .addDeserializer(JiraEntity.class, new JiraEntityDeserializer()))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JiraEntityMapper()
    {
    }

    public static <T> T readValue(String json, Class<T> type) throws IOException
    {
        return OBJECT_MAPPER.readValue(json, type);
    }

    public static <T> String writeValueAsString(T value) throws IOException
    {
        return OBJECT_MAPPER.writeValueAsString(value);
    }
}

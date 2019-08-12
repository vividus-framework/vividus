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

package org.vividus.util.freemarker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import freemarker.template.TemplateException;

class FreemarkerProcessorTests
{
    private static final int ID = 12;
    private static final String RELATIVE_PATH = "test.ftl";
    private static final String EXPECTED_JSON = "{\"id\" : 12,\"name\" : someName,\"field\" : \"test field\"}";

    private final FreemarkerProcessor freemarkerProcessor = new FreemarkerProcessor(FreemarkerProcessor.class);

    @Test
    void testProcessAsStringTemplatePath() throws IOException, TemplateException
    {
        Map<String, Object> map = Map.of("id", ID, "field", "test field");
        String actualJson = freemarkerProcessor.process(RELATIVE_PATH, map, StandardCharsets.UTF_8);
        assertEquals(EXPECTED_JSON, actualJson);
    }

    @Test
    void testProcessIOException()
    {
        assertThrows(IOException.class, () ->
            freemarkerProcessor.process("test2.ftl", Collections.singletonMap("key", "value"), StandardCharsets.UTF_8));
    }

    @Test
    void testProcessTemplateException()
    {
        assertThrows(TemplateException.class, () ->
            freemarkerProcessor.process(RELATIVE_PATH, Collections.emptyMap(), StandardCharsets.UTF_8));
    }
}

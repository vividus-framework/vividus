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

package org.vividus.accessibility.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.vividus.accessibility.model.axe.Target;

class TargetDeserializerTests
{
    private static final List<String> SELECTORS = List.of("selector1", "selector2");

    private final TargetDeserializer deserializer = new TargetDeserializer();

    @Test
    void shouldDeserializeTarget() throws IOException
    {
        String json = """
                [
                    "selector1",
                    "selector2"
                ]
                """;
        Target target = deserializer.deserialize(createParser(json), null);
        assertNotNull(target);
        assertFalse(target.isInsideShadowDom());
        assertEquals(SELECTORS, target.getSelectorsChain());
    }

    @Test
    void shouldDeserializeTargetInsideShadowDom() throws IOException
    {
        String json = """
                [
                    [
                        "selector1",
                        "selector2"
                    ]
                ]
                """;
        Target target = deserializer.deserialize(createParser(json), null);
        assertNotNull(target);
        assertTrue(target.isInsideShadowDom());
        assertEquals(SELECTORS, target.getSelectorsChain());
    }

    @Test
    void shouldDeserializeTargetEmpty() throws IOException
    {
        Target target = deserializer.deserialize(createParser("[]"), null);
        assertNull(target);
    }

    private JsonParser createParser(String json) throws IOException
    {
        JsonParser parser = new JsonFactory().createParser(json);
        parser.setCodec(new ObjectMapper());
        return parser;
    }
}

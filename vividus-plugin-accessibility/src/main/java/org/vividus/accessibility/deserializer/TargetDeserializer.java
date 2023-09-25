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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.vividus.accessibility.model.axe.Target;

public class TargetDeserializer extends JsonDeserializer<Target>
{
    @Override
    public Target deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        JsonNode root = p.getCodec().readTree(p);

        if (root.isEmpty())
        {
            return null;
        }

        JsonNode firstChild = root.get(0);
        boolean insideShadowDom = firstChild.isArray();
        JsonNode elements = insideShadowDom ? firstChild : root;

        List<String> selectors = new ArrayList<>();
        elements.forEach(node -> selectors.add(node.asText()));

        return new Target(insideShadowDom, selectors);
    }
}

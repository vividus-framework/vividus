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

package org.vividus.json.steps;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang3.Validate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.vividus.util.json.JsonUtils;

import net.javacrumbs.jsonunit.core.ParametrizedMatcher;

public class JsonArrayContainsAnyOf extends BaseMatcher<Object> implements ParametrizedMatcher
{
    private List<JsonNode> expectedNodes;
    private final JsonUtils jsonUtils;

    public JsonArrayContainsAnyOf(JsonUtils jsonUtils)
    {
        this.jsonUtils = jsonUtils;
    }

    @Override
    public void setParameter(String expected)
    {
        expectedNodes = convert(expected);
    }

    @Override
    public boolean matches(Object actual)
    {
        List<JsonNode> actualNodes = convert(jsonUtils.toJson(actual));
        return actualNodes.stream().anyMatch(expectedNodes::contains);
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("JSON does not contains any of elements from ").appendValue(expectedNodes);
    }

    private List<JsonNode> convert(String jsonArray)
    {
        JsonNode node = jsonUtils.readTree(jsonArray);
        Validate.isTrue(node.isArray(), "Expected type `ARRAY`, but found `%s`: %s", node.getNodeType(),
            node.toPrettyString());
        List<JsonNode> result = new ArrayList<>();
        node.elements().forEachRemaining(result::add);
        return result;
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.util.json.JsonUtils;

class JsonArrayContainsAnyOfTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String ANOTHER_VALUE = "another value";
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
    private static final ObjectNode JSON_OBJECT_NODE = NODE_FACTORY.objectNode().put(KEY, VALUE);

    private final JsonArrayContainsAnyOf matcher = new JsonArrayContainsAnyOf(new JsonUtils());

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    static Stream<Arguments> positiveParametersProvider()
    {
        String parameter = createArrayNode(KEY, VALUE).toString();
        return Stream.of(
            arguments(parameter, createArrayNode(ANOTHER_VALUE, VALUE), true),
            arguments(parameter, createArrayNode(ANOTHER_VALUE),        false)
        );
    }

    @ParameterizedTest
    @MethodSource("positiveParametersProvider")
    void shouldMatches(String parameter, ArrayNode actual, boolean expectedResult)
    {
        matcher.setParameter(parameter);
        assertEquals(expectedResult, matcher.matches(actual));
    }

    @Test
    void shouldFillDescription()
    {
        matcher.setParameter("[1, 2]");
        Description actual = new StringDescription();
        matcher.describeTo(actual);

        Description expected = new StringDescription().appendText("JSON does not contains any of elements from ")
            .appendValue(List.of(1, 2));

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void shouldThrowExceptionParameterNotArray()
    {
        performValidationTest(() -> matcher.setParameter(JSON_OBJECT_NODE.toString()));
    }

    @Test
    void shouldThrowExceptionActualNotArray()
    {
        performValidationTest(() -> matcher.matches(JSON_OBJECT_NODE));
    }

    private void performValidationTest(Executable executable)
    {
        Exception exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals(String.format("Expected type `ARRAY`, but found `%s`: %s", JSON_OBJECT_NODE.getNodeType(),
            JSON_OBJECT_NODE.toPrettyString()), exception.getMessage());
    }

    private static ArrayNode createArrayNode(String... values)
    {
        ArrayNode arrayNode = NODE_FACTORY.arrayNode();
        Stream.of(values).forEach(arrayNode::add);
        return arrayNode;
    }
}

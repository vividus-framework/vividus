/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.zephyr.databind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.zephyr.model.TestCaseExecution;
import org.vividus.zephyr.model.TestCaseStatus;

@ExtendWith(MockitoExtension.class)
public class TestCaseExecutionDeserializerTests
{
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private JsonParser parser;

    @Mock
    private ObjectCodec objectCodec;

    private final TestCaseExecutionDeserializer deserializer = new TestCaseExecutionDeserializer();

    @BeforeEach
    void init()
    {
        when(parser.getCodec()).thenReturn(objectCodec);
    }

    @Test
    void testExecutionDeserialize() throws IOException
    {
        JsonNode root = MAPPER.readTree("{\"status\" : \"failed\", \"labels\" : [{\"name\" : \"testCaseId\","
                + "\"value\" : \"TEST-001\"}, {\"name\" : \"framework\", \"value\" : \"Vividus\"}]}");
        when(objectCodec.readTree(parser)).thenReturn(root);
        TestCaseExecution testCaseExecution = deserializer.deserialize(parser, null);

        assertEquals(List.of("TEST-001"), testCaseExecution.getKeys());
        assertEquals(TestCaseStatus.FAILED, testCaseExecution.getStatus());
    }

    @Test
    void testExecutionDeserializeWithoutTestCaseId() throws IOException
    {
        JsonNode root = MAPPER.readTree("{\"status\" : \"passed\","
                + "\"labels\" : [{\"name\" : \"framework\", \"value\" : \"Vividus\"}]}");
        when(objectCodec.readTree(parser)).thenReturn(root);
        TestCaseExecution testCaseExecution = deserializer.deserialize(parser, null);

        assertEquals(List.of(), testCaseExecution.getKeys());
        assertEquals(TestCaseStatus.PASSED, testCaseExecution.getStatus());
    }

    @Test
    void testExecutionDeserializeWithTwoTestCaseIds() throws IOException
    {
        JsonNode root = MAPPER.readTree("{\"status\" : \"broken\", \"labels\" : [{\"name\" : \"testCaseId\","
                + "\"value\" : \"TEST-002\"}, {\"name\" : \"testCaseId\",\"value\" : \"TEST-003\"},"
                + "{\"name\" : \"framework\", \"value\" : \"Vividus\"}]}");
        when(objectCodec.readTree(parser)).thenReturn(root);
        TestCaseExecution testCaseExecution = deserializer.deserialize(parser, null);

        assertEquals(List.of("TEST-002", "TEST-003"), testCaseExecution.getKeys());
        assertEquals(TestCaseStatus.BROKEN, testCaseExecution.getStatus());
    }
}

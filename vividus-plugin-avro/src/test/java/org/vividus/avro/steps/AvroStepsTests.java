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

package org.vividus.avro.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AvroStepsTests
{
    @Mock private VariableContext variableContext;
    @InjectMocks private AvroSteps avroSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AvroSteps.class);

    @Test
    void shouldConvertAvroDataToJson() throws IOException
    {
        var scopes = Set.of(VariableScope.STORY);
        var variableName = "varName";
        avroSteps.convertAvroDataToJson("/event-message.avro", scopes, variableName);
        verify(variableContext).putVariable(scopes, variableName, "[{\"SequenceNumber\": 0, \"Offset\": \"0\", "
                + "\"EnqueuedTimeUtc\": \"11/5/2021 1:25:22 PM\", \"SystemProperties\": {\"x-opt-enqueued-time\": "
                + "1636118722484}, \"Properties\": {}, \"Body\": \"my-data\"}]");
        assertThat(logger.getLoggingEvents(), is(List.of(info("Avro schema: {}", "{\"type\":\"record\","
                + "\"name\":\"EventData\",\"namespace\":\"Microsoft.ServiceBus.Messaging\","
                + "\"fields\":[{\"name\":\"SequenceNumber\",\"type\":\"long\"},{\"name\":\"Offset\","
                + "\"type\":\"string\"},{\"name\":\"EnqueuedTimeUtc\",\"type\":\"string\"},"
                + "{\"name\":\"SystemProperties\",\"type\":{\"type\":\"map\",\"values\":[\"long\",\"double\","
                + "\"string\",\"bytes\"]}},{\"name\":\"Properties\",\"type\":{\"type\":\"map\",\"values\":[\"long\","
                + "\"double\",\"string\",\"bytes\",\"null\"]}},{\"name\":\"Body\",\"type\":[\"null\",\"bytes\"]}]}"))));
    }
}

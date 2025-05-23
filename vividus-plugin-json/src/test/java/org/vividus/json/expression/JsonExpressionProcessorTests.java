/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.json.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonExpressionProcessorTests
{
    @InjectMocks private JsonExpressionProcessor processor;

    @Test
    void shouldFormatJsonToOneLine()
    {
        String json = """
            {
                "title": "Berserk",
                "year": 1997,
                "episodes": 25,
                "genre": ["Action", "Adventure", "Dark Fantasy"],
                "director": "Naohito Takahashi",
                "studio": "OLM, Inc.",
                "synopsis": "A mercenary named Guts joins the Band of the Hawk, leading to a tragic fate."
            }
            """;

        Optional<String> output = processor.execute("formatToOneLineJson(%s)".formatted(json));
        assertEquals(Optional.of("{\"title\":\"Berserk\",\"year\":1997,\"episodes\":25,\"genre\":[\"Action\","
                + "\"Adventure\",\"Dark Fantasy\"],\"director\":\"Naohito Takahashi\",\"studio\":\"OLM, Inc.\","
                + "\"synopsis\":\"A mercenary named Guts joins the Band of the Hawk, leading to a tragic fate.\"}"),
                output);
    }
}

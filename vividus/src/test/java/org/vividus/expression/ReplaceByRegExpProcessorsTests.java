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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.Optional;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ReplaceByRegExpProcessorsTests
{
    private final ReplaceByRegExpProcessors processors = new ReplaceByRegExpProcessors();

    static Stream<Arguments> expressionInput()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
            of("replaceFirstByRegExp(.+=(\\d), $1.0, onePlusTwo=3)",                                Optional.of("3.0")),
            of("formatDate()",                                                                      Optional.empty()),
            of("replaceFirstByRegExp(\"\"\".*three, (\\d)\"\"\", $1.0, \"\"\"two, three, 4\"\"\")", Optional.of("4.0")),
            of("replaceAllByRegExp(\\s, _, string with spaces)",                                    Optional.of("string_with_spaces")),
            of("replaceAllByRegExp(\\s, \"\"\",\"\"\", string with spaces)",                        Optional.of("string,with,spaces")),
            of("replaceAllByRegExp(\\s, \\,, string with spaces)",                                  Optional.of("string,with,spaces")),
            of("replaceAllByRegExp(\\,, _, string,with,commas)",                                    Optional.of("string_with_commas")),
            of("replaceAllByRegExp(\"\"\",\"\"\", _, string,with,commas)",                          Optional.of("string_with_commas")),
            of("replaceAllByRegExp(\\,, _, \"\"\"string,with,commas\"\"\")",                        Optional.of("string_with_commas")),
            of("replaceAllByRegExp(\\,, _, string\\,with\\,commas)",                                Optional.of("string_with_commas")),
            of("replaceFirstByRegExp(\\s, _, string with spaces)",                                  Optional.of("string_with spaces")),
            of("replaceFirstByRegExp(\\s, \"\"\",\"\"\", string with spaces)",                      Optional.of("string,with spaces")),
            of("replaceFirstByRegExp(\\s, \\,, string with spaces)",                                Optional.of("string,with spaces")),
            of("replaceFirstByRegExp(\\,, _, string,with,commas)",                                  Optional.of("string_with,commas")),
            of("replaceFirstByRegExp(\"\"\",\"\"\", _, string,with,commas)",                        Optional.of("string_with,commas")),
            of("replaceFirstByRegExp(\\,, _, \"\"\"string,with,commas\"\"\")",                      Optional.of("string_with,commas")),
            of("replaceFirstByRegExp(\\,, _, string\\,with\\,commas)",                              Optional.of("string_with,commas")),
            of("replaceFirstByRegExp(\\,, , string\\,with\\,commas)",                               Optional.of("stringwith,commas")),
            of("replaceAllByRegExp(\\,, , string\\,with\\,commas)",                                 Optional.of("stringwithcommas")),
            of("replaceAllByRegExp(test, TEST, this\nis\ntest\nvalue)",                             Optional.of("this\nis\nTEST\nvalue")),
            of("replaceAllByRegExp(.test, TEST, this\nis\ntest\nvalue)",                            Optional.of("this\nisTEST\nvalue")),
            of("replaceFirstByRegExp(.*(te[a-z]+).*, $1, this\nis\ntest\nvalue)",                   Optional.of("test"))
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("expressionInput")
    void testExecute(String input, Optional<String> expectedResult)
    {
        assertEquals(expectedResult, processors.execute(input));
    }

    @Test
    void testExecuteWithPatterException()
    {
        assertThrows(PatternSyntaxException.class,
            () -> processors.execute("replaceFirstByRegExp(}{, $1, justAString)"));
    }
}

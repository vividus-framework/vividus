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
import static org.mockito.Mockito.when;
import static org.vividus.expression.NormalizingArgumentsUtils.normalize;

import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpressionArgumentMatcherTests
{
    @Mock private Matcher matcher;

    static Stream<Arguments> dataProvider()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(1, " Abc "),
                Arguments.of(2, " d\\, e "),
                Arguments.of(3, " \"\"\" f, g \"\"\" ")
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void group(int group, String matcherOutput)
    {
        when(matcher.group(group)).thenReturn(matcherOutput);
        ExpressionArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(matcher);
        assertEquals(matcherOutput, argumentMatcher.group(group));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void getArgument(int group, String matcherOutput)
    {
        when(matcher.group(group)).thenReturn(matcherOutput);
        ExpressionArgumentMatcher argumentMatcher = new ExpressionArgumentMatcher(matcher);
        assertEquals(normalize(matcherOutput), argumentMatcher.getArgument(group));
    }
}

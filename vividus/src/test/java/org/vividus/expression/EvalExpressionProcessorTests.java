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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.lenient;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.jexl3.JexlException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;

@ExtendWith(MockitoExtension.class)
class EvalExpressionProcessorTests
{
    private static final String TRUE = "true";
    private static final String VALUE = "Value";
    private static final String FALSE = "false";

    @Mock private VariableContext bddVariableContext;
    @InjectMocks private EvalExpressionProcessor processor;

    static Stream<Arguments> evalArguments()
    {
        return Stream.of(
                arguments("eval(null)", "null"),
                arguments("eval(16 + 2 * 6)", "28"),
                arguments("eval(math:abs(-10))", "10"),
                arguments("eval(stringUtils:substringAfterLast('namescpaces are %here', '%'))", "here"),
                arguments("eval((16 + 2) * 6)", "108"),
                arguments("eval(100 / 5 - 16 * 2 + 6)", "-6"),
                arguments("eval(`string\n1` == `string\n1`)", TRUE),
                arguments("eval(`string\n1` == `string1`)", FALSE),
                arguments("eval(`string\n1` == 'string1')", FALSE),
                arguments("eval(var t = 20; var s = function(x, y) {x + y + t}; t = 54; s(15, 7))", "42"),
                arguments("eval(wordUtils:capitalize('i am FINE'))", "I Am FINE"),
                arguments("eval(wordUtils:uncapitalize('I Am FINE'))", "i am fINE"),
                arguments("eval(wordUtils:swapCase('The dog has a BONE'))", "tHE DOG HAS A bone"),
                arguments("eval(wordUtils:initials('Fus Ro Dah'))", "FRD")
        );
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\"")
    @MethodSource("evalArguments")
    void testExecuteMatchingExpression(String expression, String expected)
    {
        assertEquals(Optional.of(expected), processor.execute(expression));
    }

    @Test
    void testExecuteNonMatchingExpression()
    {
        assertEquals(Optional.empty(), processor.execute("evaluate(1+1)"));
    }

    static Stream<Arguments> evalWithVars()
    {
        return Stream.of(
                arguments("eval(someKey + '=10')", "Value=10", VALUE),
                arguments("eval(someKey.length() + '')", "5", VALUE),
                arguments("eval((someKey.length() == 5) + '' )", TRUE, VALUE),
                arguments("eval(someKey = '1'; someKey + someKey)", "11", VALUE)
        );
    }

    @ParameterizedTest
    @MethodSource("evalWithVars")
    void shouldResolveBddVariable(String expression, Object expectedValue, Object variable)
    {
        lenient().when(bddVariableContext.getVariable("someKey")).thenReturn(variable);
        assertEquals(Optional.of(expectedValue), processor.execute(expression));
    }

    @Test
    void shouldThrowAnExceptionInCaseOfMissingVariable()
    {
        var exception = assertThrows(JexlException.Variable.class, () -> processor.execute("eval(missingVar + 'val')"));
        assertEquals(
                "org.vividus.expression.EvalExpressionProcessor.lambda$new$0:49@1:1 variable 'missingVar' is undefined",
                exception.getMessage());
    }

    @Test
    void shouldThrowAnExceptionInCaseOfSyntaxError()
    {
        var exception = assertThrows(JexlException.Parsing.class, () -> processor.execute("eval(var + 'val')"));
        assertEquals("org.vividus.expression.EvalExpressionProcessor.lambda$new$0:49@1:1 parsing error in 'var'",
                exception.getMessage());
    }
}

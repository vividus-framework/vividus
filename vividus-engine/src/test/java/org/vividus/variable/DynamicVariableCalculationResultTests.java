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

package org.vividus.variable;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class DynamicVariableCalculationResultTests
{
    private static final String VALUE = "value";
    private static final String ERROR = "error";

    @Test
    void shouldCreateResultWithValueAndWithoutError()
    {
        var result = DynamicVariableCalculationResult.withValue(VALUE);
        assertResultWithValue(result, VALUE);
    }

    @Test
    void shouldCreateResultWithoutValueAndWithError()
    {
        var result = DynamicVariableCalculationResult.withError(ERROR);
        assertResultWithError(result, ERROR);
    }

    @Test
    void shouldCreateResultWithOptionalValueAndWithoutError()
    {
        var result = DynamicVariableCalculationResult.withValueOrError(Optional.of(VALUE), () -> ERROR);
        assertResultWithValue(result, VALUE);
    }

    @Test
    void shouldCreateResultWithMissingValueAndWithError()
    {
        var result = DynamicVariableCalculationResult.withValueOrError(Optional.empty(), () -> ERROR);
        assertResultWithError(result, ERROR);
    }

    @Test
    void validateHashCodeAndEquals()
    {
        EqualsVerifier.forClass(DynamicVariableCalculationResult.class)
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @SuppressWarnings("unchecked")
    private void assertResultWithValue(DynamicVariableCalculationResult result, String expectedValue)
    {
        assertAll(
                () -> {
                    var errorHandler = mock(Consumer.class);
                    assertEquals(Optional.of(expectedValue), result.getValueOrHandleError(errorHandler));
                    verifyNoInteractions(errorHandler);
                },
                () -> assertEquals(result, DynamicVariableCalculationResult.withValue(expectedValue))
        );
    }

    @SuppressWarnings("unchecked")
    private void assertResultWithError(DynamicVariableCalculationResult result, String expectedError)
    {
        assertAll(
                () -> {
                    var errorHandler = mock(Consumer.class);
                    assertEquals(Optional.empty(), result.getValueOrHandleError(errorHandler));
                    verify(errorHandler).accept(expectedError);
                },
                () -> assertEquals(result, DynamicVariableCalculationResult.withError(expectedError)));
    }
}

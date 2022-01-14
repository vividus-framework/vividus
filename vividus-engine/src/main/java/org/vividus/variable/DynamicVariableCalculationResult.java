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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This class represents the result of the dynamic variable calculation. It contains either calculated
 * {@link String} value or error message describing the failure happened at the calculation. Both value and error
 * message can't be present at the same time.
 */
public final class DynamicVariableCalculationResult
{
    private final Optional<String> value;
    private final Optional<String> error;

    private DynamicVariableCalculationResult(Optional<String> value, Optional<String> error)
    {
        this.value = value;
        this.error = error;
    }

    /**
     * Creates a successful result with variable value.
     *
     * @param value The result of the calculation - dynamic variable value.
     * @return Created result object.
     */
    public static DynamicVariableCalculationResult withValue(String value)
    {
        return new DynamicVariableCalculationResult(Optional.of(value), Optional.empty());
    }

    /**
     * Creates a failure result with error message describing the root cause of the unsuccessful calculation.
     *
     * @param error The error message describing the failure happened at the calculation.
     * @return Created result object.
     */
    public static DynamicVariableCalculationResult withError(String error)
    {
        return new DynamicVariableCalculationResult(Optional.empty(), Optional.of(error));
    }

    /**
     * Creates a successful result if {@code value} is present, otherwise creates a failure result with an error
     * provided by {@code errorSupplier}
     *
     * @param value The result of the calculation, it can be empty in case of error.
     * @param errorSupplier The error supplier used to generate error message in case of missing value.
     * @return Created result object.
     */
    public static DynamicVariableCalculationResult withValueOrError(Optional<String> value,
            Supplier<String> errorSupplier)
    {
        return new DynamicVariableCalculationResult(value,
                value.isEmpty() ? Optional.of(errorSupplier.get()) : Optional.empty());
    }

    public Optional<String> getValueOrHandleError(Consumer<String> errorHandler)
    {
        if (value.isEmpty())
        {
            errorHandler.accept(error.get());
        }
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        DynamicVariableCalculationResult that = (DynamicVariableCalculationResult) o;
        return value.equals(that.value) && error.equals(that.error);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value, error);
    }
}

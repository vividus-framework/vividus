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

package org.vividus.http.variable;

import java.util.Optional;
import java.util.function.Function;

import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public abstract class AbstractHttpResponseDynamicVariable implements DynamicVariable
{
    private final HttpTestContext httpTestContext;
    private final Function<HttpResponse, String> valueMapper;

    protected AbstractHttpResponseDynamicVariable(HttpTestContext httpTestContext,
            Function<HttpResponse, String> valueMapper)
    {
        this.httpTestContext = httpTestContext;
        this.valueMapper = valueMapper;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        return DynamicVariableCalculationResult.withValueOrError(
                Optional.ofNullable(httpTestContext.getResponse()).map(valueMapper),
                () -> "no HTTP requests were executed"
        );
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith(MockitoExtension.class)
class ResponseDynamicVariableTests
{
    @Mock private HttpTestContext httpTestContext;
    @InjectMocks private ResponseDynamicVariable responseDynamicVariable;

    @Test
    void shouldReturnResponseBodyAsString()
    {
        String responseBody = "response";
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseBody(responseBody.getBytes(StandardCharsets.UTF_8));
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        assertEquals(DynamicVariableCalculationResult.withValue(responseBody),
                responseDynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnErrorWhenNoResponsePresent()
    {
        when(httpTestContext.getResponse()).thenReturn(null);
        assertEquals(DynamicVariableCalculationResult.withError("no HTTP requests were executed"),
                responseDynamicVariable.calculateValue());
    }
}

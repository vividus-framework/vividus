/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.aws.lambda.steps;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LogType;

@ExtendWith(MockitoExtension.class)
class LambdaStepsTests
{
    private static final String PAYLOAD_RESULT = "result";
    private static final String LOG_RESULT = "log-log-log";
    private static final String EXECUTED_VERSION = "0.2.11";

    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private VariableContext variableContext;

    @Test
    void shouldInvokeAwsLambda()
    {
        testAwsLambdaInvocation(ignored -> { }, Map.of());
    }

    @Test
    void shouldInvokeAwsLambdaWithError()
    {
        String error = "error";
        testAwsLambdaInvocation(result -> result.functionError(error), Map.of("function-error", error));
    }

    @SuppressWarnings("PMD.CloseResource")
    private void testAwsLambdaInvocation(Consumer<InvokeResponse.Builder> resultDecorator,
            Map<String, String> extraExpectedEntries)
    {
        LambdaClient awsLambda = mock();
        String functionName = "function";
        String payload = "request";
        InvokeResponse invokeResult = buildInvokeResponse(resultDecorator);
        when(awsLambda.invoke(argThat((InvokeRequest request) -> functionName.equals(request.functionName())
                && payload.equals(request.payload().asUtf8String())
                && LogType.TAIL == request.logType()))).thenReturn(invokeResult);
        LambdaSteps steps = new LambdaSteps(clientsContext, variableContext);
        when(clientsContext.getServiceClient(any(), any())).thenReturn(awsLambda);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "var";
        steps.invokeLambda(functionName, payload, scopes, variableName);
        Map<String, String> variableValue = new HashMap<>();
        variableValue.put("payload", PAYLOAD_RESULT);
        variableValue.put("status-code", "500");
        variableValue.put("log-result", LOG_RESULT);
        variableValue.put("executed-version", EXECUTED_VERSION);
        variableValue.putAll(extraExpectedEntries);
        verify(variableContext).putVariable(scopes, variableName, variableValue);
    }

    private static InvokeResponse buildInvokeResponse(Consumer<InvokeResponse.Builder> resultDecorator)
    {
        InvokeResponse.Builder builder = InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String(PAYLOAD_RESULT))
                .statusCode(500)
                .logResult(Base64.getEncoder().encodeToString(LOG_RESULT.getBytes(StandardCharsets.UTF_8)))
                .executedVersion(EXECUTED_VERSION);
        resultDecorator.accept(builder);
        return builder.build();
    }
}

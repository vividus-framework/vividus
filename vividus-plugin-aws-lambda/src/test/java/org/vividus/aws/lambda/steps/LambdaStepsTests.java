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

package org.vividus.aws.lambda.steps;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class LambdaStepsTests
{
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
        testAwsLambdaInvocation(result -> result.setFunctionError(error), Map.of("function-error", error));
    }

    private void testAwsLambdaInvocation(Consumer<InvokeResult> resultDecorator,
            Map<String, String> extraExpectedEntries)
    {
        try (var builder = mockStatic(AWSLambdaClientBuilder.class))
        {
            AWSLambda awsLambda = mock();
            builder.when(AWSLambdaClientBuilder::defaultClient).thenReturn(awsLambda);

            AWSLambdaClientBuilder customClientBuilder = mock();
            builder.when(AWSLambdaClientBuilder::standard).thenReturn(customClientBuilder);

            String result = "result";
            int statusCode = 500;
            String logResult = "log-log-log";
            String executedVersion = "0.2.11";
            InvokeResult invokeResult = new InvokeResult();
            invokeResult.setPayload(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
            invokeResult.setStatusCode(statusCode);
            invokeResult.setLogResult(Base64.getEncoder().encodeToString(logResult.getBytes(StandardCharsets.UTF_8)));
            invokeResult.setExecutedVersion(executedVersion);
            resultDecorator.accept(invokeResult);

            String functionName = "function";
            String payload = "request";
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(functionName)
                    .withPayload(payload)
                    .withLogType(LogType.Tail);
            when(awsLambda.invoke(invokeRequest)).thenReturn(invokeResult);

            LambdaSteps steps = new LambdaSteps(clientsContext, variableContext);

            when(clientsContext.getServiceClient(
                    argThat((ArgumentMatcher<Supplier<AwsClientBuilder<AWSLambdaClientBuilder, AWSLambda>>>)
                            supplier -> supplier.get().equals(customClientBuilder)), eq(awsLambda)))
                    .thenReturn(awsLambda);

            Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
            String variableName = "var";
            steps.invokeLambda(functionName, payload, scopes, variableName);
            Map<String, String> variableValue = new HashMap<>();
            variableValue.put("payload", result);
            variableValue.put("status-code", Integer.toString(statusCode));
            variableValue.put("log-result", logResult);
            variableValue.put("executed-version", executedVersion);
            variableValue.putAll(extraExpectedEntries);
            verify(variableContext).putVariable(scopes, variableName, variableValue);
        }
    }
}

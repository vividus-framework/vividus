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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LogType;

public class LambdaSteps
{
    private final VariableContext variableContext;
    private final AwsServiceClientsContext clientsContext;

    public LambdaSteps(AwsServiceClientsContext clientsContext, VariableContext variableContext)
    {
        this.clientsContext = clientsContext;
        this.variableContext = variableContext;
    }

    private LambdaClient getLambdaClient()
    {
        return clientsContext.getServiceClient(LambdaClient::builder, () -> LambdaClient.builder().build());
    }

    /**
     * Invoke AWS Lambda function by providing the function name and the payload to pass to the Lambda function.
     *
     * @param functionName the name of AWS Lambda function, version, or alias. The value can be retrieved by looking
     *                     at the function in the AWS Console. Supported name formats:
     *                     <ul>
     *                     <li>Function name: my-function (name-only), my-function:v1 (with alias).</li>
     *                     <li>Function ARN: arn:aws:lambda:us-west-2:123456789012:function:my-function.</li>
     *                     <li>Partial ARN: 123456789012:function:my-function.</li>
     *                     </ul>
     *                     It’s allowed to append a version number or alias to any of the formats. The length
     *                     constraint applies only to the full ARN. If only the function name is specified, it is
     *                     limited to 64 characters in length.
     * @param payload      the JSON that to provide to AWS Lambda function as input. Vividus performs a Base64 encoding
     *                     on this field before sending this request to the AWS service. Users should not perform Base64
     *                     encoding on this field.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>scopes
     * @param variableName The variable name to store results. If the variable name is my-var, the following
     *                     variables will be created:
     *                     <ul>
     *                     <li>${my-var.payload} - the response from the function, or an error object</li>
     *                     <li>${my-var.status-code} - the HTTP status code is in the 200 range for a successful
     *                     request</li>
     *                     <li>${my-var.log-result} - the last 4 KB of the execution log</li>
     *                     <li>${my-var.executed-version} - the version of the function that executed, when it’s invoked
     *                     a function with an
     *                     alias, this indicates which version the alias resolved to</li>
     *                     <li>${my-var.function-error} - if present, indicates that an error occurred during function
     *                     execution, details about the error are included in the response payload</li>
     *                     </ul>
     */
    @When("I invoke AWS Lambda function `$functionName` with payload `$payload` and save result to $scopes variable "
            + "`$variableName`")
    public void invokeLambda(String functionName, String payload, Set<VariableScope> scopes, String variableName)
    {
        InvokeRequest invokeRequest = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromUtf8String(payload))
                .logType(LogType.TAIL)
                .build();
        InvokeResponse invokeResult = getLambdaClient().invoke(invokeRequest);

        Map<String, String> result = new HashMap<>();
        result.put("payload", new String(invokeResult.payload().asByteArray(), StandardCharsets.UTF_8));
        result.put("status-code", invokeResult.statusCode().toString());
        result.put("log-result",
                new String(Base64.getDecoder().decode(invokeResult.logResult()), StandardCharsets.UTF_8));
        result.put("executed-version", invokeResult.executedVersion());
        String functionError = invokeResult.functionError();
        if (functionError != null)
        {
            result.put("function-error", functionError);
        }
        variableContext.putVariable(scopes, variableName, result);
    }
}

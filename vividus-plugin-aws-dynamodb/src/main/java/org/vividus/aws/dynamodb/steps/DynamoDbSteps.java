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

package org.vividus.aws.dynamodb.steps;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

public class DynamoDbSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbSteps.class);

    private final AwsServiceClientsContext clientsContext;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils = new JsonUtils();

    private final String roleArn;

    public DynamoDbSteps(String roleArn, AwsServiceClientsContext clientsContext, VariableContext variableContext)
    {
        this.roleArn = roleArn;
        this.clientsContext = clientsContext;
        this.variableContext = variableContext;
    }

    private DynamoDbClient getDynamoDbClient()
    {
        return clientsContext.getServiceClient(DynamoDbClient::builder, this::createDefaultDynamoDbClient);
    }

    /**
     * The step is intended to execute PartiQL INSERT, UPDATE, DELETE statements against DynamoDB. The PartiQL SELECT
     * statement can be executed successfully, but results won't be saved. For the  PartiQL SELECT statements use step:
     * <br>
     * <code>
     * When I execute query `$partiqlQuery` against DynamoDB and save result as JSON to $scopes variable `$variableName`
     * </code>
     *
     * @param partiqlQuery The PartiQL (A SQL-Compatible Query Language for Amazon DynamoDB) statement representing
     *                     the operation to run.
     * @return Result of the ExecuteStatement operation returned by the service.
     * @see <a href="https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ql-reference.html">PartiQL</a>
     */
    @When("I execute query `$partiqlQuery` against DynamoDB")
    public ExecuteStatementResponse executeQuery(String partiqlQuery)
    {
        LOGGER.info("Executing query: {}", partiqlQuery);
        ExecuteStatementRequest request = ExecuteStatementRequest.builder().statement(partiqlQuery).build();
        return getDynamoDbClient().executeStatement(request);
    }

    /**
     * The step is intended to execute PartiQL SELECT statement against DynamoDB. The INSERT, UPDATE, DELETE
     * statements can be executed successfully, but the result will be empty. For the  PartiQL INSERT, UPDATE, DELETE
     * statements use step:
     * <br>
     * <code>
     * When I execute query `$partiqlQuery` against DynamoDB
     * </code>
     * @param partiqlQuery The PartiQL (A SQL-Compatible Query Language for Amazon DynamoDB) statement representing
     *                     the operation to run.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save PartiQL query execution result in JSON format
     */
    @When("I execute query `$partiqlQuery` against DynamoDB and save result as JSON to $scopes variable "
            + "`$variableName`")
    public void executeQuery(String partiqlQuery, Set<VariableScope> scopes, String variableName)
    {
        String jsonResult = jsonUtils.toJson(executeQuery(partiqlQuery).items().stream()
                .map(DynamoDbSteps::toMap)
                .toList());
        variableContext.putVariable(scopes, variableName, jsonResult);
    }

    private static Map<String, Object> toMap(Map<String, AttributeValue> values)
    {
        return values.entrySet()
                .stream()
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), toObject(entry.getValue())),
                        LinkedHashMap::putAll);
    }

    private static Object toObject(AttributeValue attributeValue)
    {
        return switch (attributeValue.type())
        {
            case S -> attributeValue.s();
            case N -> new BigDecimal(attributeValue.n());
            case BOOL -> attributeValue.bool();
            case NUL -> null;
            case B -> Base64.getEncoder().encodeToString(attributeValue.b().asByteArray());
            case L -> attributeValue.l().stream().map(DynamoDbSteps::toObject).toList();
            case M -> toMap(attributeValue.m());
            case SS -> attributeValue.ss();
            case NS -> attributeValue.ns().stream().map(BigDecimal::new).toList();
            case BS -> attributeValue.bs().stream()
                    .map(bytes -> Base64.getEncoder().encodeToString(bytes.asByteArray()))
                    .toList();
            default -> attributeValue.toString();
        };
    }

    private DynamoDbClient createDefaultDynamoDbClient()
    {
        if (roleArn == null)
        {
            return DynamoDbClient.builder().build();
        }
        AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                .refreshRequest(AssumeRoleRequest.builder()
                        .roleArn(roleArn)
                        .roleSessionName("Vividus")
                        .build())
                .build();
        return DynamoDbClient.builder().credentialsProvider(credentialsProvider).build();
    }
}

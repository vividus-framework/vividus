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
import java.util.List;
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

public class DynamoDbSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbSteps.class);

    private final AwsServiceClientsContext clientsContext;
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils = new JsonUtils();

    private final DynamoDbClient amazonDynamoDB;

    public DynamoDbSteps(String roleArn, AwsServiceClientsContext clientsContext, VariableContext variableContext)
    {
        this.clientsContext = clientsContext;
        this.variableContext = variableContext;

        var amazonDynamoDBClientBuilder = DynamoDbClient.builder();
        if (roleArn != null)
        {
            AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .refreshRequest(request -> request.roleArn(roleArn).roleSessionName("Vividus"))
                    .build();
            amazonDynamoDBClientBuilder.credentialsProvider(credentialsProvider);
        }
        this.amazonDynamoDB = amazonDynamoDBClientBuilder.build();
    }

    private DynamoDbClient getDynamoDbClient()
    {
        return clientsContext.getServiceClient(DynamoDbClient::builder, amazonDynamoDB);
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
                .stream()
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
        if (attributeValue.s() != null)
        {
            return attributeValue.s();
        }
        if (attributeValue.n() != null)
        {
            return new BigDecimal(attributeValue.n());
        }
        if (attributeValue.bool() != null)
        {
            return attributeValue.bool();
        }
        if (Boolean.TRUE.equals(attributeValue.nul()))
        {
            return null;
        }
        if (attributeValue.hasL())
        {
            return attributeValue.l().stream().map(DynamoDbSteps::toObject).toList();
        }
        if (attributeValue.hasM())
        {
            return toMap(attributeValue.m());
        }
        if (attributeValue.hasSs())
        {
            return attributeValue.ss();
        }
        if (attributeValue.hasNs())
        {
            return attributeValue.ns().stream().map(BigDecimal::new).toList();
        }
        if (attributeValue.hasBs())
        {
            return attributeValue.bs().stream()
                    .map(bytes -> Base64.getEncoder().encodeToString(bytes.asByteArray()))
                    .toList();
        }
        if (attributeValue.b() != null)
        {
            return Base64.getEncoder().encodeToString(attributeValue.b().asByteArray());
        }
        return attributeValue.toString();
    }
}

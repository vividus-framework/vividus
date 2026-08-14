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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DynamoDbStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DynamoDbSteps.class);

    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private VariableContext variableContext;

    @Test
    void shouldExecuteDeleteQueryWithAssumedRole()
    {
        String partiqlQuery = "DELETE FROM Table WHERE KeyName='Value'";
        ExecuteStatementResponse result = ExecuteStatementResponse.builder().build();
        executeQuery("role:arn", partiqlQuery, result, steps ->
        {
            ExecuteStatementResponse actual = steps.executeQuery(partiqlQuery);
            assertEquals(result, actual);
        });
    }

    @Test
    void shouldExecuteSelectQuery()
    {
        String partiqlQuery = "SELECT * FROM Table";
        ExecuteStatementResponse result = ExecuteStatementResponse.builder()
                .items(
                        Map.of("key1", AttributeValue.builder().s("value1").build()),
                        Map.of("key2", AttributeValue.builder().s("value2").build()))
                .build();
        executeQuery(null, partiqlQuery, result, steps -> {
            Set<VariableScope> scopes = Set.of(VariableScope.STORY);
            String variableName = "var";
            steps.executeQuery(partiqlQuery, scopes, variableName);
            String expectedValue = "[{\"key1\":\"value1\"},{\"key2\":\"value2\"}]";
            verify(variableContext).putVariable(scopes, variableName, expectedValue);
        });
    }

    @SuppressWarnings("PMD.CloseResource")
    private void executeQuery(String roleArn, String partiqlQuery, ExecuteStatementResponse result,
            Consumer<DynamoDbSteps> test)
    {
        DynamoDbClient amazonDynamoDB = mock();
        when(amazonDynamoDB.executeStatement(
                argThat((ExecuteStatementRequest request) -> partiqlQuery.equals(request.statement()))))
                .thenReturn(result);

        DynamoDbSteps steps = new DynamoDbSteps(roleArn, clientsContext, variableContext);
        when(clientsContext.getServiceClient(any(), any(), any())).thenReturn(amazonDynamoDB);

        test.accept(steps);

        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info("Executing query: {}", partiqlQuery))));
    }
}

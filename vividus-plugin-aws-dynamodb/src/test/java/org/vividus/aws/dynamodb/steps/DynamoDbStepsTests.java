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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider.Builder;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExecuteStatementRequest;
import com.amazonaws.services.dynamodbv2.model.ExecuteStatementResult;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DynamoDbStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DynamoDbSteps.class);

    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private VariableContext variableContext;

    @SuppressWarnings({ "try", "PMD.CloseResource" })
    @Test
    void shouldExecuteDeleteQueryWithAssumedRole()
    {
        String partiqlQuery = "DELETE FROM Table WHERE KeyName='Value'";
        ExecuteStatementResult result = mock(ExecuteStatementResult.class);
        STSAssumeRoleSessionCredentialsProvider provider = mock(STSAssumeRoleSessionCredentialsProvider.class);
        String roleArn = "role:arn";
        try (MockedConstruction<Builder> ignored = mockConstruction(Builder.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(roleArn, "Vividus"), context.arguments());
                    when(mock.build()).thenReturn(provider);
                }))
        {
            AmazonDynamoDBClientBuilder builder = executeQuery(roleArn, partiqlQuery,
                    result, steps -> {
                        ExecuteStatementResult actual = steps.executeQuery(partiqlQuery);
                        assertEquals(result, actual);
                    });
            verify(builder).withCredentials(provider);
        }
    }

    @Test
    void shouldExecuteSelectQuery()
    {
        String partiqlQuery = "SELECT * FROM Table";
        ExecuteStatementResult result = new ExecuteStatementResult();
        result.setItems(List.of(
                Map.of("key1", new AttributeValue("value1")),
                Map.of("key2", new AttributeValue("value2"))
        ));
        executeQuery(null, partiqlQuery, result, steps -> {
            Set<VariableScope> scopes = Set.of(VariableScope.STORY);
            String variableName = "var";
            steps.executeQuery(partiqlQuery, scopes, variableName);
            String expectedValue = "[{\"key1\":\"value1\"},{\"key2\":\"value2\"}]";
            verify(variableContext).putVariable(scopes, variableName, expectedValue);
        });
    }

    private AmazonDynamoDBClientBuilder executeQuery(String roleArn, String partiqlQuery, ExecuteStatementResult result,
            Consumer<DynamoDbSteps> test)
    {
        try (var builder = mockStatic(AmazonDynamoDBClientBuilder.class))
        {
            AmazonDynamoDBClientBuilder amazonDynamoDBClientBuilder = mock();
            builder.when(AmazonDynamoDBClientBuilder::standard).thenReturn(amazonDynamoDBClientBuilder);

            AmazonDynamoDB amazonDynamoDB = mock();
            when(amazonDynamoDBClientBuilder.build()).thenReturn(amazonDynamoDB);

            ArgumentCaptor<ExecuteStatementRequest> captor = ArgumentCaptor.forClass(ExecuteStatementRequest.class);
            when(amazonDynamoDB.executeStatement(captor.capture())).thenReturn(result);

            DynamoDbSteps steps = new DynamoDbSteps(roleArn, clientsContext, variableContext);

            when(clientsContext.getServiceClient(
                    argThat((ArgumentMatcher<Supplier<AwsClientBuilder<AmazonDynamoDBClientBuilder, AmazonDynamoDB>>>)
                            supplier -> supplier.get().equals(amazonDynamoDBClientBuilder)), eq(amazonDynamoDB)))
                    .thenReturn(amazonDynamoDB);

            test.accept(steps);

            ExecuteStatementRequest request = captor.getValue();
            assertEquals(partiqlQuery, request.getStatement());
            assertNull(request.getParameters());

            assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info("Executing query: {}", partiqlQuery))));

            return amazonDynamoDBClientBuilder;
        }
    }
}

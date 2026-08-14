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

package org.vividus.aws.dynamodb.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementRequest;
import software.amazon.awssdk.services.dynamodb.model.ExecuteStatementResponse;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DynamoDbStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(DynamoDbSteps.class);

    private static final String PARTIQL_QUERY = "SELECT * FROM Table";
    private static final String ROLE_ARN = "role:arn";
    private static final String VARIABLE_NAME = "var";
    private static final String BINARY_BASE64 = "dGVzdA==";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);

    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private VariableContext variableContext;

    @Test
    void shouldExecuteDeleteQuery()
    {
        String partiqlQuery = "DELETE FROM Table WHERE KeyName='Value'";
        ExecuteStatementResponse result = ExecuteStatementResponse.builder().build();
        executeQuery(null, partiqlQuery, result, steps ->
        {
            ExecuteStatementResponse actual = steps.executeQuery(partiqlQuery);
            assertEquals(result, actual);
        });
    }

    @Test
    void shouldExecuteSelectQueryAndSaveStringAttributesAsJson()
    {
        ExecuteStatementResponse result = ExecuteStatementResponse.builder()
                .items(
                        Map.of("key1", AttributeValue.builder().s("value1").build()),
                        Map.of("key2", AttributeValue.builder().s("value2").build()))
                .build();
        executeQuery(null, PARTIQL_QUERY, result, steps ->
        {
            steps.executeQuery(PARTIQL_QUERY, SCOPES, VARIABLE_NAME);
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME,
                    "[{\"key1\":\"value1\"},{\"key2\":\"value2\"}]");
        });
    }

    @Test
    void shouldConvertAllAttributeValueTypesToJson()
    {
        byte[] binary = "test".getBytes(StandardCharsets.UTF_8);

        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put("str", AttributeValue.builder().s("hello").build());
        item.put("num", AttributeValue.builder().n("42.5").build());
        item.put("bool", AttributeValue.builder().bool(true).build());
        item.put("nul", AttributeValue.builder().nul(true).build());
        item.put("bin", AttributeValue.builder().b(SdkBytes.fromByteArray(binary)).build());
        item.put("list", AttributeValue.builder().l(
                AttributeValue.builder().s("a").build(),
                AttributeValue.builder().n("7").build()).build());
        item.put("map", AttributeValue.builder().m(Map.of(
                "nested", AttributeValue.builder().s("value").build())).build());
        item.put("ss", AttributeValue.builder().ss("x", "y").build());
        item.put("ns", AttributeValue.builder().ns("1", "2").build());
        item.put("bs", AttributeValue.builder().bs(SdkBytes.fromByteArray(binary)).build());

        AttributeValue unknown = mock(AttributeValue.class);
        when(unknown.type()).thenReturn(AttributeValue.Type.UNKNOWN_TO_SDK_VERSION);
        when(unknown.toString()).thenReturn("unknown-attr");
        item.put("unknown", unknown);

        ExecuteStatementResponse result = ExecuteStatementResponse.builder().items(item).build();
        executeQuery(null, PARTIQL_QUERY, result, steps ->
        {
            steps.executeQuery(PARTIQL_QUERY, SCOPES, VARIABLE_NAME);
            String expected = "[{\"str\":\"hello\",\"num\":42.5,\"bool\":true,\"nul\":null,"
                    + "\"bin\":\"" + BINARY_BASE64 + "\",\"list\":[\"a\",7],\"map\":{\"nested\":\"value\"},"
                    + "\"ss\":[\"x\",\"y\"],\"ns\":[1,2],\"bs\":[\"" + BINARY_BASE64 + "\"],"
                    + "\"unknown\":\"unknown-attr\"}]";
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, expected);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateDefaultClientWithoutAssumedRole()
    {
        DynamoDbClient amazonDynamoDB = mock();
        when(amazonDynamoDB.executeStatement(any(ExecuteStatementRequest.class)))
                .thenReturn(ExecuteStatementResponse.builder().build());

        try (MockedStatic<DynamoDbClient> dynamoDbClient = mockStatic(DynamoDbClient.class))
        {
            DynamoDbClientBuilder builder = mock();
            dynamoDbClient.when(DynamoDbClient::builder).thenReturn(builder);
            when(builder.build()).thenReturn(amazonDynamoDB);

            when(clientsContext.getServiceClient(any(), any())).thenAnswer(invocation ->
            {
                Supplier<DynamoDbClient> defaultClientSupplier = invocation.getArgument(1);
                return defaultClientSupplier.get();
            });

            DynamoDbSteps steps = new DynamoDbSteps(null, clientsContext, variableContext);
            steps.executeQuery(PARTIQL_QUERY);

            verify(builder).build();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateDefaultClientWithAssumedRole()
    {
        DynamoDbClient amazonDynamoDB = mock();
        when(amazonDynamoDB.executeStatement(any(ExecuteStatementRequest.class)))
                .thenReturn(ExecuteStatementResponse.builder().build());

        try (MockedStatic<DynamoDbClient> dynamoDbClient = mockStatic(DynamoDbClient.class);
                MockedStatic<StsAssumeRoleCredentialsProvider> stsProvider = mockStatic(
                        StsAssumeRoleCredentialsProvider.class))
        {
            DynamoDbClientBuilder builder = mock();
            dynamoDbClient.when(DynamoDbClient::builder).thenReturn(builder);
            when(builder.credentialsProvider(any())).thenReturn(builder);
            when(builder.build()).thenReturn(amazonDynamoDB);

            StsAssumeRoleCredentialsProvider.Builder stsBuilder = mock();
            StsAssumeRoleCredentialsProvider credentialsProvider = mock();
            stsProvider.when(StsAssumeRoleCredentialsProvider::builder).thenReturn(stsBuilder);
            ArgumentCaptor<AssumeRoleRequest> requestCaptor = ArgumentCaptor.forClass(AssumeRoleRequest.class);
            when(stsBuilder.refreshRequest(requestCaptor.capture())).thenReturn(stsBuilder);
            when(stsBuilder.build()).thenReturn(credentialsProvider);

            when(clientsContext.getServiceClient(any(), any())).thenAnswer(invocation ->
            {
                Supplier<DynamoDbClient> defaultClientSupplier = invocation.getArgument(1);
                return defaultClientSupplier.get();
            });

            DynamoDbSteps steps = new DynamoDbSteps(ROLE_ARN, clientsContext, variableContext);
            steps.executeQuery(PARTIQL_QUERY);

            AssumeRoleRequest assumeRoleRequest = requestCaptor.getValue();
            assertEquals(ROLE_ARN, assumeRoleRequest.roleArn());
            assertEquals("Vividus", assumeRoleRequest.roleSessionName());
            verify(builder).credentialsProvider(credentialsProvider);
            verify(builder).build();
        }
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
        when(clientsContext.getServiceClient(any(), any())).thenReturn(amazonDynamoDB);

        test.accept(steps);

        assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(info("Executing query: {}", partiqlQuery))));
    }
}

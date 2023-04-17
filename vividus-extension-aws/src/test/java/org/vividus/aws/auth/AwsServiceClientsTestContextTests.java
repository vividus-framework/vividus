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

package org.vividus.aws.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.vividus.testcontext.SimpleTestContext;

class AwsServiceClientsTestContextTests
{
    private final AwsServiceClientsTestContext awsServiceClientsTestContext = new AwsServiceClientsTestContext(
            new SimpleTestContext());

    @ParameterizedTest
    @EnumSource(AwsServiceClientScope.class)
    void shouldCreateServiceClientWhenCredentialsAreProvided(AwsServiceClientScope scope)
    {
        var expectedClient = new Object();

        Object actualClient = createServiceClient(scope, expectedClient);

        assertEquals(expectedClient, actualClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnDefaultClientWhenNoCredentialsAreProvided()
    {
        var clientBuilderSupplier = mock(Supplier.class);
        var defaultClient = new Object();

        var actualClient = awsServiceClientsTestContext.getServiceClient(clientBuilderSupplier, defaultClient);

        assertEquals(defaultClient, actualClient);
        verifyNoInteractions(clientBuilderSupplier);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnCachedServiceClientWhenAvailable()
    {
        Object storyScopedClient = new Object();
        Object actualClient = createServiceClient(AwsServiceClientScope.STORY, storyScopedClient);
        assertEquals(storyScopedClient, actualClient);

        Object scenarioScopedClient = new Object();
        actualClient = createServiceClient(AwsServiceClientScope.SCENARIO, scenarioScopedClient);
        assertEquals(scenarioScopedClient, actualClient);

        var clientBuilderSupplier = mock(Supplier.class);
        Object defaultClient = new Object();

        Object cachedClient = awsServiceClientsTestContext.getServiceClient(clientBuilderSupplier, defaultClient);

        assertEquals(scenarioScopedClient, cachedClient);
        verifyNoInteractions(clientBuilderSupplier);

        awsServiceClientsTestContext.clearScenarioScopedClients();
        cachedClient = awsServiceClientsTestContext.getServiceClient(clientBuilderSupplier, defaultClient);

        assertEquals(storyScopedClient, cachedClient);
        verifyNoInteractions(clientBuilderSupplier);
    }

    @Test
    void shouldNotCreateStoryScopedClientWhenScenarioScopedOneIsAvailable()
    {
        Object scenarioScopedClient = new Object();
        Object actualClient = createServiceClient(AwsServiceClientScope.SCENARIO, scenarioScopedClient);
        assertEquals(scenarioScopedClient, actualClient);

        Object storyScopedClient = new Object();
        actualClient = createServiceClient(AwsServiceClientScope.STORY, storyScopedClient);
        assertEquals(scenarioScopedClient, actualClient);
    }

    @SuppressWarnings("unchecked")
    private Object createServiceClient(AwsServiceClientScope scope, Object expectedClient)
    {
        var builder = mock(AwsClientBuilder.class);
        AWSCredentialsProvider credentialsProvider = mock();
        when(builder.withCredentials(credentialsProvider)).thenReturn(builder);
        when(builder.build()).thenReturn(expectedClient);

        awsServiceClientsTestContext.putCredentialsProvider(scope, credentialsProvider);
        return awsServiceClientsTestContext.getServiceClient(() -> builder, new Object());
    }
}

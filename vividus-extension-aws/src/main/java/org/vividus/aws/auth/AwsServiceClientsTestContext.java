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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;

import org.jbehave.core.annotations.AfterScenario;
import org.vividus.testcontext.TestContext;

public class AwsServiceClientsTestContext implements AwsServiceClientsContext
{
    private static final Class<ScopedAwsServiceClients> AWS_SERVICE_CLIENTS_KEY = ScopedAwsServiceClients.class;

    private final TestContext testContext;

    public AwsServiceClientsTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    @Override
    public <B extends AwsClientBuilder<B, T>, T> T getServiceClient(
            Supplier<AwsClientBuilder<B, T>> clientBuilderSupplier, T defaultClient)
    {
        ScopedAwsServiceClients clients = getAwsServiceClients();

        @SuppressWarnings("unchecked")
        Class<T> clientClass = (Class<T>) defaultClient.getClass();

        return getClient(clients, AwsServiceClientScope.SCENARIO, clientClass, clientBuilderSupplier)
                .or(() -> getClient(clients, AwsServiceClientScope.STORY, clientClass, clientBuilderSupplier))
                .orElse(defaultClient);
    }

    @SuppressWarnings("unchecked")
    private static <B extends AwsClientBuilder<B, T>, T> Optional<T> getClient(ScopedAwsServiceClients scopedClients,
            AwsServiceClientScope scope, Class<T> clientClass, Supplier<AwsClientBuilder<B, T>> clientBuilderSupplier)
    {
        return Optional.ofNullable(scopedClients.clients.get(scope)).map(clients ->
                (T) clients.clients.computeIfAbsent(clientClass,
                        k -> Optional.ofNullable(clients.credentialsProvider)
                                .map(credentials -> clientBuilderSupplier.get().withCredentials(credentials).build())
                                .orElse(null)
                )
        );
    }

    @Override
    public void putCredentialsProvider(AwsServiceClientScope scope, AWSCredentialsProvider credentialsProvider)
    {
        getAwsServiceClients().clients.computeIfAbsent(scope,
                k -> new AwsServiceClients()).credentialsProvider = credentialsProvider;
    }

    @AfterScenario
    public void clearScenarioScopedClients()
    {
        getAwsServiceClients().clients.remove(AwsServiceClientScope.SCENARIO);
    }

    private ScopedAwsServiceClients getAwsServiceClients()
    {
        return testContext.get(AWS_SERVICE_CLIENTS_KEY, ScopedAwsServiceClients::new);
    }

    private static final class ScopedAwsServiceClients
    {
        private final Map<AwsServiceClientScope, AwsServiceClients> clients = new EnumMap<>(
                AwsServiceClientScope.class);
    }

    private static final class AwsServiceClients
    {
        private AWSCredentialsProvider credentialsProvider;
        private final Map<Class<?>, Object> clients = new HashMap<>();
    }
}

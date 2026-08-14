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

import org.jbehave.core.annotations.AfterScenario;
import org.vividus.testcontext.TestContext;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;

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
            Supplier<AwsClientBuilder<B, T>> clientBuilderSupplier, Supplier<T> defaultClientSupplier)
    {
        ScopedAwsServiceClients clients = getAwsServiceClients();

        return getClient(clients, AwsServiceClientScope.SCENARIO, clientBuilderSupplier)
                .or(() -> getClient(clients, AwsServiceClientScope.STORY, clientBuilderSupplier))
                .orElseGet(defaultClientSupplier);
    }

    @SuppressWarnings("unchecked")
    private static <B extends AwsClientBuilder<B, T>, T> Optional<T> getClient(ScopedAwsServiceClients scopedClients,
            AwsServiceClientScope scope, Supplier<AwsClientBuilder<B, T>> clientBuilderSupplier)
    {
        return Optional.ofNullable(scopedClients.clients.get(scope)).map(clients ->
        {
            AwsClientBuilder<B, T> builder = clientBuilderSupplier.get();
            return (T) clients.clients.computeIfAbsent(builder.getClass(),
                    k -> Optional.ofNullable(clients.credentialsProvider)
                            .map(cp -> builder.credentialsProvider(cp).build())
                            .orElse(null)
            );
        });
    }

    @Override
    public void putCredentialsProvider(AwsServiceClientScope scope, AwsCredentialsProvider credentialsProvider)
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
        private AwsCredentialsProvider credentialsProvider;
        private final Map<Class<?>, Object> clients = new HashMap<>();
    }
}

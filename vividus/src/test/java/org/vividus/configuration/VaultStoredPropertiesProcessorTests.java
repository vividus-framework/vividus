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

package org.vividus.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatcher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.vault.client.RestTemplateCustomizer;
import org.springframework.vault.config.EnvironmentVaultConfiguration;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.RestTemplate;

class VaultStoredPropertiesProcessorTests
{
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "/",
            "engine/",
            "engine/path",
            "engine/path/",
            "/engine/path/",
            "engine//path",
            "/engine/path/key"
    })
    void shouldRejectInvalidSecretPaths(String fullSecretPath) throws IOException
    {
        try (var processor = new VaultStoredPropertiesProcessor(new Properties()))
        {
            var propertyName = "invalid-property";
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> processor.processValue(propertyName, fullSecretPath));
            assertEquals(
                    "Full secret path must follow pattern 'engine/path_with_separators/key', but '" + fullSecretPath
                            + "' was found in property '" + propertyName + "'", exception.getMessage());
        }
    }

    @Test
    void shouldUseNamespaceForEnterpriseVaultWhenConfigured() throws IOException
    {
        var vaultNamespaceProperty = "vault.namespace";
        var vaultNamespaceValue = "ns1/ns2";
        var properties = new Properties();
        properties.put(vaultNamespaceProperty, vaultNamespaceValue);

        MutablePropertySources propertySources = mock();
        var result = "pa$$w0rd";

        try (var processor = new VaultStoredPropertiesProcessor(properties);
                var annotationConfigApplicationContextConstruction = mockConstruction(
                        AnnotationConfigApplicationContext.class,
                        (mock, context) -> {
                            ConfigurableEnvironment configurableEnvironment = mock();
                            when(mock.getEnvironment()).thenReturn(configurableEnvironment);

                            when(configurableEnvironment.getPropertySources()).thenReturn(propertySources);

                            VaultTemplate vaultTemplate = mock();
                            when(mock.getBean(VaultTemplate.class)).thenReturn(vaultTemplate);

                            VaultKeyValueOperations operations = mock();
                            when(vaultTemplate.opsForKeyValue("secret", KeyValueBackend.KV_2)).thenReturn(operations);

                            var vaultResponse = new VaultResponse();
                            vaultResponse.setData(Map.of("test", result));
                            when(operations.get("vividus")).thenReturn(vaultResponse);
                        }))
        {
            var actual = processor.processValue("my-super-password", "secret/vividus/test");
            assertEquals(result, actual);

            assertThat(annotationConfigApplicationContextConstruction.constructed(), hasSize(1));
            var context = annotationConfigApplicationContextConstruction.constructed().get(0);

            var ordered = inOrder(context, propertySources);
            ordered.verify(propertySources).addFirst(
                    argThat(source -> vaultNamespaceValue.equals(source.getProperty(vaultNamespaceProperty)))
            );

            ordered.verify(context).registerBean(eq(RestTemplateCustomizer.class), argThat(
                    (ArgumentMatcher<Supplier<RestTemplateCustomizer>>) supplier -> {
                        RestTemplate restTemplate = mock();
                        var interceptors = new ArrayList<ClientHttpRequestInterceptor>();
                        when(restTemplate.getInterceptors()).thenReturn(interceptors);

                        var restTemplateCustomizer = supplier.get();
                        restTemplateCustomizer.customize(restTemplate);

                        assertThat(interceptors, hasSize(1));
                        var interceptor = interceptors.get(0);

                        HttpRequest httpRequest = mock();
                        var httpHeaders = new HttpHeaders();
                        when(httpRequest.getHeaders()).thenReturn(httpHeaders);

                        try
                        {
                            interceptor.intercept(httpRequest, new byte[0], mock(ClientHttpRequestExecution.class));
                            assertEquals(Set.of(Map.entry("X-Vault-Namespace", List.of(vaultNamespaceValue))),
                                    httpHeaders.entrySet());
                            return true;
                        }
                        catch (IOException e)
                        {
                            throw new UncheckedIOException(e);
                        }
                    }));
            ordered.verify(context).register(EnvironmentVaultConfiguration.class);
            ordered.verify(context).refresh();
            ordered.verify(context).getBean(VaultTemplate.class);
        }
    }
}

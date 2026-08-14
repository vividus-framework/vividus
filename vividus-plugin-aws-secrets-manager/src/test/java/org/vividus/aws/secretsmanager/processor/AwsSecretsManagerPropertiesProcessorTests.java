/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.aws.secretsmanager.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Properties;

import com.google.common.util.concurrent.UncheckedExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AwsSecretsManagerPropertiesProcessorTests
{
    private static final String PROP_NAME = "property_name";

    private AwsSecretsManagerPropertiesProcessor processor;

    @BeforeEach
    void setUp()
    {
        processor = new AwsSecretsManagerPropertiesProcessor();
    }

    @CsvSource(value = {
        "default;sec/ret;sec/ret/key",
        "custom;secret;custom, secret/key"
    }, delimiter = ';')
    @ParameterizedTest
    void shouldProcessValue(String profile, String secretId, String valueToProcess)
    {
        String secretString = "{\"key\":\"value\"}";
        GetSecretValueResponse result = GetSecretValueResponse.builder().secretString(secretString).build();

        try (MockedStatic<SecretsManagerClient> clientStatic = mockStatic(SecretsManagerClient.class))
        {
            SecretsManagerClientBuilder builder = mock(SecretsManagerClientBuilder.class);
            SecretsManagerClient client = mock(SecretsManagerClient.class);

            clientStatic.when(SecretsManagerClient::builder).thenReturn(builder);
            when(builder.credentialsProvider(any())).thenReturn(builder);
            when(builder.build()).thenReturn(client);
            ArgumentCaptor<GetSecretValueRequest> requestCaptor = ArgumentCaptor.forClass(GetSecretValueRequest.class);
            when(client.getSecretValue(requestCaptor.capture())).thenReturn(result);

            String value = processor.processValue(PROP_NAME, valueToProcess);
            assertEquals("value", value);
            assertEquals(secretId, requestCaptor.getValue().secretId());
        }
    }

    @Test
    void shouldThrowExceptionForInvalidFormat()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.processValue("test", "invalid-format"));
        assertEquals("The expected property value format is AWS_SECRETS_MANAGER(profile, secret/secret_key) "
                + "or AWS_SECRETS_MANAGER(secret/secret_key)", exception.getMessage());
    }

    @Test
    void shouldNotProcessPropertiesWhenProcessorDisabled()
    {
        var properties = new Properties();
        properties.put("property", "AWS_SECRETS_MANAGER(profile, secret/test)");
        assertEquals(properties, processor.processProperties(properties));
    }

    @Test
    void shouldHandleResourceNotFoundExceptionFromCache()
    {
        String valueToProcess = "profileX, secretName/key";

        try (MockedStatic<SecretsManagerClient> clientStatic = mockStatic(SecretsManagerClient.class))
        {
            SecretsManagerClientBuilder builder = mock(SecretsManagerClientBuilder.class);
            SecretsManagerClient client = mock(SecretsManagerClient.class);

            clientStatic.when(SecretsManagerClient::builder).thenReturn(builder);
            when(builder.credentialsProvider(any())).thenReturn(builder);
            when(builder.build()).thenReturn(client);

            when(client.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenThrow(ResourceNotFoundException.builder().message("not found").build());

            UncheckedExecutionException exception = assertThrows(UncheckedExecutionException.class,
                    () -> processor.processValue(PROP_NAME, valueToProcess));
            assertEquals(
                    "The requested secret 'secretName' was not found in AWS Secrets Manager using profile 'profileX'",
                    exception.getCause().getMessage());
        }
    }
}

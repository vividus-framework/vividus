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
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwsSecretsManagerPropertiesProcessorTests
{
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
        GetSecretValueResult result = new GetSecretValueResult().withSecretString(secretString);

        try (MockedStatic<AWSSecretsManagerClient> clientStatic = mockStatic(AWSSecretsManagerClient.class);
                MockedConstruction<ProfileCredentialsProvider> credentialsProviderMock = mockConstruction(
                        ProfileCredentialsProvider.class,
                        (mock, context) -> assertEquals(profile, context.arguments().get(0))))
        {
            AWSSecretsManagerClientBuilder builder = mock(AWSSecretsManagerClientBuilder.class);
            AWSSecretsManager client = mock(AWSSecretsManager.class);

            clientStatic.when(AWSSecretsManagerClient::builder).thenReturn(builder);
            when(builder.withCredentials(any(ProfileCredentialsProvider.class))).thenReturn(builder);
            when(builder.build()).thenReturn(client);
            ArgumentCaptor<GetSecretValueRequest> requestCaptor = ArgumentCaptor.forClass(GetSecretValueRequest.class);
            when(client.getSecretValue(requestCaptor.capture())).thenReturn(result);

            String value = processor.processValue("property_name", valueToProcess);
            assertEquals("value", value);
            assertEquals(secretId, requestCaptor.getValue().getSecretId());
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
}

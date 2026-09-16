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

package org.vividus.aws.secretsmanager.processor;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Properties;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AwsSecretsManagerPropertiesProcessorTests
{
    private static final String PROP_NAME = "property_name";
    private static final String SECRET_STRING = "{\"key\":\"value\"}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AwsSecretsManagerPropertiesProcessor.class);

    private AwsSecretsManagerPropertiesProcessor processor;

    @BeforeEach
    void setUp()
    {
        processor = new AwsSecretsManagerPropertiesProcessor();
    }

    @CsvSource(value = {
        "default;sec/ret;sec/ret/key",
        "default;sec/ret;sec/ret/key:fallback",
        "custom;secret;custom, secret/key",
        "custom;secret;custom, secret/key:fallback"
    }, delimiter = ';')
    @ParameterizedTest
    void shouldProcessValue(String profile, String secretId, String valueToProcess)
    {
        GetSecretValueResult result = new GetSecretValueResult().withSecretString(SECRET_STRING);

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

            String value = processor.processValue(PROP_NAME, valueToProcess);
            assertEquals("value", value);
            assertEquals(secretId, requestCaptor.getValue().getSecretId());
        }
    }

    @Test
    void shouldThrowExceptionForInvalidFormat()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> processor.processValue("test", "invalid-format"));
        assertEquals(
                "The expected property value format is AWS_SECRETS_MANAGER(profile, "
                        + "secret/secret_key:default_value) "
                        + "or AWS_SECRETS_MANAGER(secret/secret_key:default_value)",
                exception.getMessage());
    }

    @Test
    void shouldNotProcessPropertiesWhenProcessorDisabled()
    {
        var properties = new Properties();
        properties.put("property", "AWS_SECRETS_MANAGER(profile, secret/test)");
        assertEquals(properties, processor.processProperties(properties));
    }

    @ValueSource(strings = {
        "profileX, secretName/key",
        "profileX, secretName/key:local-default"
    })
    @ParameterizedTest
    void shouldHandleResourceNotFoundExceptionFromCache(String valueToProcess)
    {
        try (MockedStatic<AWSSecretsManagerClient> clientStatic = mockStatic(AWSSecretsManagerClient.class);
                MockedConstruction<ProfileCredentialsProvider> credentialsProviderMock = mockConstruction(
                        ProfileCredentialsProvider.class,
                        (mock, context) -> assertEquals("profileX", context.arguments().get(0))))
        {
            AWSSecretsManagerClientBuilder builder = mock(AWSSecretsManagerClientBuilder.class);
            AWSSecretsManager client = mock(AWSSecretsManager.class);

            clientStatic.when(AWSSecretsManagerClient::builder).thenReturn(builder);
            when(builder.withCredentials(any(ProfileCredentialsProvider.class))).thenReturn(builder);
            when(builder.build()).thenReturn(client);

            when(client.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenThrow(new ResourceNotFoundException("not found"));

            UncheckedExecutionException exception = assertThrows(UncheckedExecutionException.class,
                    () -> processor.processValue(PROP_NAME, valueToProcess));
            assertEquals(
                    "The requested secret 'secretName' was not found in AWS Secrets Manager using profile 'profileX'",
                    exception.getCause().getMessage());
        }
    }

    @CsvSource(value = {
        "default;sec/ret;sec/ret/missing-key:local-default;local-default",
        "custom;secret;custom, secret/missing-key:local-default;local-default",
        "custom;secret;custom, secret/missing-key:;''"
    }, delimiter = ';')
    @ParameterizedTest
    void shouldUseDefaultValueWhenSecretKeyIsMissing(String profile, String secretId, String valueToProcess,
            String expectedValue)
    {
        GetSecretValueResult result = new GetSecretValueResult().withSecretString(SECRET_STRING);

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
            when(client.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(result);

            assertEquals(expectedValue, processor.processValue(PROP_NAME, valueToProcess));
            assertThat(logger.getLoggingEvents(), is(List.of(info(
                    "The secret key '{}' was not found in the secret '{}' retrieved from AWS Secrets Manager "
                            + "using profile '{}'. Using default value for property '{}'",
                    "missing-key", secretId, profile, PROP_NAME))));
        }
    }

    @Test
    void shouldThrowExceptionWhenSecretKeyIsMissingAndNoDefaultValue()
    {
        GetSecretValueResult result = new GetSecretValueResult().withSecretString(SECRET_STRING);

        try (MockedStatic<AWSSecretsManagerClient> clientStatic = mockStatic(AWSSecretsManagerClient.class);
                MockedConstruction<ProfileCredentialsProvider> credentialsProviderMock = mockConstruction(
                        ProfileCredentialsProvider.class))
        {
            AWSSecretsManagerClientBuilder builder = mock(AWSSecretsManagerClientBuilder.class);
            AWSSecretsManager client = mock(AWSSecretsManager.class);

            clientStatic.when(AWSSecretsManagerClient::builder).thenReturn(builder);
            when(builder.withCredentials(any(ProfileCredentialsProvider.class))).thenReturn(builder);
            when(builder.build()).thenReturn(client);
            when(client.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(result);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> processor.processValue(PROP_NAME, "secret/missing-key"));
            assertEquals(
                    "The secret key 'missing-key' was not found in the secret 'secret' retrieved from AWS Secrets "
                            + "Manager using profile 'default'",
                    exception.getMessage());
        }
    }
}

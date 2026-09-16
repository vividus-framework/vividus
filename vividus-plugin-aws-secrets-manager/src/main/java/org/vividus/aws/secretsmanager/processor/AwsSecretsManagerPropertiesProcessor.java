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

import java.time.Duration;
import java.util.Properties;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jayway.jsonpath.PathNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.configuration.AbstractPropertiesProcessor;
import org.vividus.util.json.JsonPathUtils;

public class AwsSecretsManagerPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSecretsManagerPropertiesProcessor.class);
    private static final String PATH_SEPARATOR = "/";
    private static final String DEFAULT_VALUE_SEPARATOR = ":";
    private static final String AWS_DEFAULT_PROFILE = "default";
    private static final String PROPERTY_REGEX = "([^\\s]+?\\s*,\\s*)?[^\\s]+/[^\\s:]+(?::.*)?";
    private static final String PROCESSOR_ENABLED_PROPERTY = "secrets-manager.aws-secrets-manager.enabled";

    private final LoadingCache<SecretId, String> secretsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1)).build(new CacheLoader<>()
            {
                @Override
                public String load(SecretId secretId) throws Exception
                {
                    AWSSecretsManager client = AWSSecretsManagerClient.builder()
                            .withCredentials(new ProfileCredentialsProvider(secretId.profile))
                            .build();

                    try
                    {
                        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                                .withSecretId(secretId.secret);

                        GetSecretValueResult response = client.getSecretValue(getSecretValueRequest);
                        return response.getSecretString();
                    }
                    catch (ResourceNotFoundException thrown)
                    {
                        throw new IllegalArgumentException(String.format(
                                "The requested secret '%s' was not found in AWS Secrets Manager using profile '%s'",
                                secretId.secret, secretId.profile), thrown);
                    }
                    finally
                    {
                        client.shutdown();
                    }
                }
            });

    public AwsSecretsManagerPropertiesProcessor()
    {
        super("AWS_SECRETS_MANAGER");
    }

    @Override
    public boolean isEnabled(Properties properties)
    {
        return Boolean.parseBoolean(properties.getProperty(PROCESSOR_ENABLED_PROPERTY));
    }

    @Override
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        Validate.isTrue(partOfPropertyValueToProcess.matches(PROPERTY_REGEX),
                "The expected property value format is AWS_SECRETS_MANAGER(profile, secret/secret_key:default_value) "
                + "or AWS_SECRETS_MANAGER(secret/secret_key:default_value)");
        String[] configVariables = partOfPropertyValueToProcess.split(",", 2);

        String profile = AWS_DEFAULT_PROFILE;
        String secretPath = configVariables[0];
        if (configVariables.length == 2)
        {
            profile = configVariables[0].strip();
            secretPath = configVariables[1];
        }

        String secret = StringUtils.substringBeforeLast(secretPath, PATH_SEPARATOR).strip();
        String keyWithOptionalDefault = StringUtils.substringAfterLast(secretPath, PATH_SEPARATOR);
        String[] keyAndDefaultValue = StringUtils.splitPreserveAllTokens(keyWithOptionalDefault,
                DEFAULT_VALUE_SEPARATOR, 2);
        String key = keyAndDefaultValue[0];
        String defaultValue = keyAndDefaultValue.length > 1 ? keyAndDefaultValue[1] : null;

        String secretString = secretsCache.getUnchecked(new SecretId(profile, secret));
        try
        {
            return JsonPathUtils.getData(secretString, "$." + key);
        }
        catch (PathNotFoundException thrown)
        {
            if (defaultValue != null)
            {
                LOGGER.info(
                        "The secret key '{}' was not found in the secret '{}' retrieved from AWS Secrets Manager "
                                + "using profile '{}'. Using default value for property '{}'",
                        key, secret, profile, propertyName);
                return defaultValue;
            }
            throw new IllegalArgumentException(String.format(
                    "The secret key '%s' was not found in the secret '%s' retrieved from AWS Secrets Manager using "
                            + "profile '%s'",
                    key, secret, profile), thrown);
        }
    }

    private record SecretId(String profile, String secret)
    {
    }
}

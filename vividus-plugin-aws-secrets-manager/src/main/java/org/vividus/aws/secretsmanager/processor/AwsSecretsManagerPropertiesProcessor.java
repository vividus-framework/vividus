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

import java.time.Duration;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.vividus.configuration.AbstractPropertiesProcessor;
import org.vividus.util.json.JsonPathUtils;

public class AwsSecretsManagerPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final String PATH_SEPARATOR = "/";
    private static final String AWS_DEFAULT_PROFILE = "default";
    private static final String PROPERTY_REGEX = "([^\\s]+?\\s*,\\s*)?[^\\s]+/[^\\s]+";

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
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        Validate.isTrue(partOfPropertyValueToProcess.matches(PROPERTY_REGEX),
                "The expected property value format is AWS_SECRETS_MANAGER(profile, secret/secret_key) "
                + "or AWS_SECRETS_MANAGER(secret/secret_key)");
        String[] configVariables = partOfPropertyValueToProcess.split(",", 2);

        String profile = AWS_DEFAULT_PROFILE;
        String secretPath = configVariables[0];
        if (configVariables.length == 2)
        {
            profile = configVariables[0].strip();
            secretPath = configVariables[1];
        }

        String secret = StringUtils.substringBeforeLast(secretPath, PATH_SEPARATOR).strip();
        String secretString = secretsCache.getUnchecked(new SecretId(profile, secret));

        String key = StringUtils.substringAfterLast(secretPath, PATH_SEPARATOR);
        return JsonPathUtils.getData(secretString, "$." + key);
    }

    private record SecretId(String profile, String secret)
    {
    }
}

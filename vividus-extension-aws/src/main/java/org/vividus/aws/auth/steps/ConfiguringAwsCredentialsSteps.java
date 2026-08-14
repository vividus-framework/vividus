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

package org.vividus.aws.auth.steps;

import org.jbehave.core.annotations.Given;
import org.vividus.aws.auth.AwsServiceClientScope;
import org.vividus.aws.auth.AwsServiceClientsContext;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class ConfiguringAwsCredentialsSteps
{
    private final AwsServiceClientsContext awsServiceClientsContext;

    public ConfiguringAwsCredentialsSteps(AwsServiceClientsContext awsServiceClientsContext)
    {
        this.awsServiceClientsContext = awsServiceClientsContext;
    }

    /**
     * Configures the AWS credentials scoped to either current scenario or story: all subsequent interactions with
     * any AWS service will use the provided credentials.
     *
     * @param scope     The AWS authentication scope: scenario or story.
     * @param accessKey The AWS access key.
     * @param secretKey The AWS secret access key.
     */
    @Given("I configure $awsCredentialsScope-scoped AWS credentials with access key `$accessKey` and secret key "
            + "`$secretKey`")
    public void configureAwsCredentials(AwsServiceClientScope scope, String accessKey, String secretKey)
    {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCredentials);
        awsServiceClientsContext.putCredentialsProvider(scope, credentialsProvider);
    }
}

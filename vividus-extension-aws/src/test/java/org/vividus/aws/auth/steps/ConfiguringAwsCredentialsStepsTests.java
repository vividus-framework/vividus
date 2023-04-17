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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.amazonaws.auth.AWSCredentialsProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientScope;
import org.vividus.aws.auth.AwsServiceClientsContext;

@ExtendWith(MockitoExtension.class)
class ConfiguringAwsCredentialsStepsTests
{
    @Mock private AwsServiceClientsContext mockAwsServiceClientsContext;
    @InjectMocks private ConfiguringAwsCredentialsSteps steps;

    @Test
    void shouldConfigureAwsCredentials()
    {
        var scope = AwsServiceClientScope.SCENARIO;
        var accessKeyId = "accessKeyId";
        var secretKey = "secretKey";

        steps.configureAwsCredentials(scope, accessKeyId, secretKey);

        var credentialsProviderCaptor = ArgumentCaptor.forClass(AWSCredentialsProvider.class);
        verify(mockAwsServiceClientsContext).putCredentialsProvider(eq(scope), credentialsProviderCaptor.capture());
        var credentials = credentialsProviderCaptor.getValue().getCredentials();
        assertEquals(accessKeyId, credentials.getAWSAccessKeyId());
        assertEquals(secretKey, credentials.getAWSSecretKey());
    }
}

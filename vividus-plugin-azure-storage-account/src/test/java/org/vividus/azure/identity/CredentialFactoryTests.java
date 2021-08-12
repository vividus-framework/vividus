/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.azure.identity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class CredentialFactoryTests
{
    @Test
    void shouldCreateTokenCredential()
    {
        DefaultAzureCredential defaultAzureCredential = mock(DefaultAzureCredential.class);
        try (MockedConstruction<DefaultAzureCredentialBuilder> credentialsBuilder = mockConstruction(
                DefaultAzureCredentialBuilder.class,
                (mock, context) -> when(mock.build()).thenReturn(defaultAzureCredential))
        )
        {
            assertEquals(defaultAzureCredential, CredentialFactory.createTokenCredential());
            assertThat(credentialsBuilder.constructed(), hasSize(1));
        }
    }
}

/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;

import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class ClientBuilderUtilsTests
{
    @Test
    void testCreateCredentialsProviderSplitWithScope()
    {
        final AuthScope authScope = new AuthScope("host1", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, "user13:pass13");
        verifyProvider(actualProvider, authScope, "user13", "pass13");
    }

    @Test
    void testCreateCredentialsProviderSimpleWithScope()
    {
        String user = "user23";
        String pass = "pass1234";
        AuthScope authScope = new AuthScope("host2", 1);
        CredentialsProvider actualProvider = ClientBuilderUtils.createCredentialsProvider(authScope, user, pass);
        verifyProvider(actualProvider, authScope, user, pass);
    }

    @Test
    void testToCookieStore()
    {
        try (MockedConstruction<CookieStoreCollector> cookieStoreCollector = mockConstruction(
                CookieStoreCollector.class))
        {
            assertEquals(cookieStoreCollector.constructed(), List.of(ClientBuilderUtils.toCookieStore()));
        }
    }

    private static void verifyProvider(CredentialsProvider actualProvider, AuthScope expectedAuthScope,
            String expectedUser, String expectedPass)
    {
        String actualName = actualProvider.getCredentials(expectedAuthScope).getUserPrincipal().getName();
        String actualPass = actualProvider.getCredentials(expectedAuthScope).getPassword();
        assertEquals(expectedUser, actualName);
        assertEquals(expectedPass, actualPass);
    }
}

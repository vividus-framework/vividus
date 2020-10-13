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

package org.vividus.http.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

class CachingCredentialsHttpContextFactoryTests
{
    @Test
    void shouldCreateHttpContext()
    {
        String username = "username";
        String password = "password";
        String endpoint = "https://example.com/";

        HttpContextFactory factory = new CachingCredentialsHttpContextFactory(endpoint, username,
                password);
        HttpContext context = factory.create();
        assertThat(context, instanceOf(HttpClientContext.class));
        HttpClientContext testContext = (HttpClientContext) context;
        Credentials credentials = testContext.getCredentialsProvider().getCredentials(AuthScope.ANY);

        assertEquals(username, credentials.getUserPrincipal().getName());
        assertEquals(password, credentials.getPassword());
        assertThat(testContext.getAuthCache(), instanceOf(BasicAuthCache.class));

        BasicAuthCache cache = (BasicAuthCache) testContext.getAuthCache();
        assertThat(cache.get(HttpHost.create(endpoint)), instanceOf(BasicScheme.class));
    }
}

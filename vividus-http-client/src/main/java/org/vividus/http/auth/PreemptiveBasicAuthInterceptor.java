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

package org.vividus.http.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.protocol.HttpContext;

public class PreemptiveBasicAuthInterceptor implements HttpRequestInterceptor
{
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException
    {
        CredentialsProvider credentialsProvider = (CredentialsProvider) context
                .getAttribute(HttpClientContext.CREDS_PROVIDER);
        if (credentialsProvider != null)
        {
            Credentials credentials = credentialsProvider.getCredentials(AuthScope.ANY);
            ContextAwareAuthScheme scheme = new BasicScheme(StandardCharsets.UTF_8);
            Header authHeader = scheme.authenticate(credentials, request, context);
            request.setHeader(authHeader);
        }
    }
}

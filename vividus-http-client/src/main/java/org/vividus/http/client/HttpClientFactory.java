/*
 * Copyright 2019 the original author or authors.
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

import java.util.Optional;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory implements IHttpClientFactory
{
    private ISslContextFactory sslContextFactory;

    @Override
    public IHttpClient buildHttpClient(HttpClientConfig config)
    {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultHeaders(config.createHeaders());
        if (config.hasCookieStore())
        {
            builder.setDefaultCookieStore(config.getCookieStore());
        }
        if (config.hasCredentials())
        {
            AuthScope authScope = config.hasAuthScope() ? config.getAuthScope()
                    : ClientBuilderUtils.DEFAULT_AUTH_SCOPE;
            CredentialsProvider credProvider = ClientBuilderUtils.createCredentialsProvider(authScope,
                    config.getCredentials());
            builder.setDefaultCredentialsProvider(credProvider);
        }

        sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, !config.isSslCertificateCheckEnabled())
                .ifPresent(builder::setSSLContext);

        if (!config.isSslHostnameVerificationEnabled())
        {
            builder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        builder.setConnectionManager(config.getConnectionManager());
        builder.setMaxConnTotal(config.getMaxTotalConnections());
        builder.setMaxConnPerRoute(config.getMaxConnectionsPerRoute());
        builder.addInterceptorLast(config.getLastRequestInterceptor());
        builder.addInterceptorLast(config.getLastResponseInterceptor());
        builder.setRedirectStrategy(config.getRedirectStrategy());
        builder.setRetryHandler(config.getHttpRequestRetryHandler());
        Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
        requestConfigBuilder.setConnectTimeout(config.getConnectTimeout());
        requestConfigBuilder.setCircularRedirectsAllowed(config.isCircularRedirectsAllowed());
        requestConfigBuilder.setSocketTimeout(config.getSocketTimeout());
        Optional.ofNullable(config.getCookieSpec()).ifPresent(requestConfigBuilder::setCookieSpec);
        builder.setDefaultRequestConfig(requestConfigBuilder.build());
        builder.setDefaultSocketConfig(SocketConfig.copy(SocketConfig.DEFAULT)
                .setSoTimeout(config.getSocketTimeout())
                .build());
        builder.setDnsResolver(config.getDnsResolver());

        HttpClient httpClient = new HttpClient();
        httpClient.setCloseableHttpClient(builder.build());
        if (config.hasBaseUrl())
        {
            httpClient.setHttpHost(HttpHost.create(config.getBaseUrl()));
        }
        httpClient.setSkipResponseEntity(config.isSkipResponseEntity());
        return httpClient;
    }

    public void setSslContextFactory(ISslContextFactory sslContextFactory)
    {
        this.sslContextFactory = sslContextFactory;
    }
}

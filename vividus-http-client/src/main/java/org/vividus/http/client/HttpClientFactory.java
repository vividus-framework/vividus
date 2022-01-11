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

package org.vividus.http.client;

import static org.apache.commons.lang3.Validate.isTrue;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.ContextAwareAuthScheme;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.vividus.http.keystore.IKeyStoreFactory;

public class HttpClientFactory implements IHttpClientFactory
{
    private final SslContextFactory sslContextFactory;
    private final IKeyStoreFactory keyStoreFactory;
    private String privateKeyPassword;

    public HttpClientFactory(SslContextFactory sslContextFactory, IKeyStoreFactory keyStoreFactory)
    {
        this.sslContextFactory = sslContextFactory;
        this.keyStoreFactory = keyStoreFactory;
    }

    @Override
    public IHttpClient buildHttpClient(HttpClientConfig config) throws GeneralSecurityException
    {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setDefaultHeaders(config.createHeaders());
        if (config.hasCookieStore())
        {
            builder.setDefaultCookieStore(config.getCookieStore());
        }

        configureAuth(config, builder);

        SslConfig sslConfig = config.getSslConfig();
        createSslContext(sslConfig.isSslCertificateCheckEnabled()).ifPresent(builder::setSSLContext);

        if (!sslConfig.isSslHostnameVerificationEnabled())
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
        builder.setServiceUnavailableRetryStrategy(config.getServiceUnavailableRetryStrategy());
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
        builder.useSystemProperties();

        HttpClient httpClient = new HttpClient();
        httpClient.setCloseableHttpClient(builder.build());
        if (config.hasBaseUrl())
        {
            httpClient.setHttpHost(HttpHost.create(config.getBaseUrl()));
        }
        httpClient.setSkipResponseEntity(config.isSkipResponseEntity());
        httpClient.setHttpResponseHandlers(Optional.ofNullable(config.getHttpResponseHandlers()).orElseGet(List::of));
        return httpClient;
    }

    private Optional<SSLContext> createSslContext(boolean sslCertificateCheckEnabled) throws GeneralSecurityException
    {
        String protocol = SSLConnectionSocketFactory.SSL;
        if (!sslCertificateCheckEnabled)
        {
            return Optional.of(sslContextFactory.getTrustingAllSslContext(protocol));
        }
        Optional<KeyStore> keyStore = keyStoreFactory.getKeyStore();
        if (keyStore.isPresent())
        {
            return Optional.of(sslContextFactory.getSslContext(protocol, keyStore.get(), privateKeyPassword));
        }
        return Optional.empty();
    }

    private void configureAuth(HttpClientConfig config, HttpClientBuilder builder)
    {
        AuthConfig authConfig = config.getAuthConfig();
        String username = authConfig.getUsername();
        String password = authConfig.getPassword();

        if (username == null && password == null)
        {
            isTrue(!authConfig.isPreemptiveAuthEnabled(),
                    "Preemptive authentication requires username and password to be set");
            return;
        }

        isTrue(username != null && password != null, "The %s is missing", username == null ? "username" : "password");

        Credentials credentials = new UsernamePasswordCredentials(username, password);
        if (authConfig.isPreemptiveAuthEnabled())
        {
            builder.addInterceptorFirst((HttpRequestInterceptor) (req, ctx) ->
            {
                ContextAwareAuthScheme scheme = new BasicScheme(StandardCharsets.UTF_8);
                Header authHeader = scheme.authenticate(credentials, req, ctx);
                req.addHeader(authHeader);
            });
        }
        else
        {
            AuthScope authScope = config.hasAuthScope() ? config.getAuthScope() : AuthScope.ANY;
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(authScope, credentials);
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }
    }

    public void setPrivateKeyPassword(String privateKeyPassword)
    {
        this.privateKeyPassword = privateKeyPassword;
    }
}

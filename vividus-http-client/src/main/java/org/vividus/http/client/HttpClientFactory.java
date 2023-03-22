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

package org.vividus.http.client;

import static org.apache.commons.lang3.Validate.isTrue;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.win.WinHttpClients;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;
import org.vividus.http.keystore.IKeyStoreFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class HttpClientFactory implements IHttpClientFactory
{
    private static final AuthScope ANY_AUTH_SCOPE = new AuthScope(null, -1);

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
        // There is no need to provide user credentials: HttpClient will attempt to access current user security
        // context through Windows platform specific methods via JNI.
        HttpClientBuilder builder = WinHttpClients.custom();

        builder.setDefaultHeaders(config.createHeaders());
        if (config.hasCookieStore())
        {
            builder.setDefaultCookieStore(config.getCookieStore());
        }

        configureAuth(config, builder);

        builder.setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(config.getMaxTotalConnections())
                .setMaxConnPerRoute(config.getMaxConnectionsPerRoute())
                .setDnsResolver(config.getDnsResolver())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMilliseconds(config.getSocketTimeout()))
                        .build())
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofMilliseconds(config.getConnectTimeout()))
                        .setSocketTimeout(Timeout.ofMilliseconds(config.getSocketTimeout()))
                        .build())
                .setSSLSocketFactory(buildSslSocketFactory(config.getSslConfig()))
                .build());

        Optional.ofNullable(config.getFirstRequestInterceptor()).ifPresent(builder::addRequestInterceptorFirst);
        Optional.ofNullable(config.getLastRequestInterceptor()).ifPresent(builder::addRequestInterceptorLast);
        Optional.ofNullable(config.getLastResponseInterceptor()).ifPresent(builder::addResponseInterceptorLast);

        builder.setRedirectStrategy(config.getRedirectStrategy());
        builder.setRetryStrategy(config.getHttpRequestRetryStrategy());
        builder.setDefaultRequestConfig(RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(config.getConnectionRequestTimeout()))
                .setCircularRedirectsAllowed(config.isCircularRedirectsAllowed())
                .setCookieSpec(config.getCookieSpec())
                .build());
        builder.useSystemProperties();

        HttpClient httpClient = new HttpClient();
        httpClient.setCloseableHttpClient(builder.build());
        if (config.hasBaseUrl())
        {
            httpClient.setHttpHost(HttpHost.create(URI.create(config.getBaseUrl())));
        }
        httpClient.setSkipResponseEntity(config.isSkipResponseEntity());
        httpClient.setHttpResponseHandlers(Optional.ofNullable(config.getHttpResponseHandlers()).orElseGet(List::of));
        return httpClient;
    }

    private SSLConnectionSocketFactory buildSslSocketFactory(SslConfig sslConfig) throws GeneralSecurityException
    {
        SSLConnectionSocketFactoryBuilder builder = SSLConnectionSocketFactoryBuilder.create();
        createSslContext(sslConfig.isSslCertificateCheckEnabled()).ifPresent(builder::setSslContext);
        if (!sslConfig.isSslHostnameVerificationEnabled())
        {
            builder.setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        }
        return builder.build();
    }

    private Optional<SSLContext> createSslContext(boolean sslCertificateCheckEnabled) throws GeneralSecurityException
    {
        if (!sslCertificateCheckEnabled)
        {
            return Optional.of(sslContextFactory.getTrustingAllSslContext());
        }
        Optional<KeyStore> keyStore = keyStoreFactory.getKeyStore();
        if (keyStore.isPresent())
        {
            return Optional.of(sslContextFactory.getSslContext(keyStore.get(), privateKeyPassword));
        }
        return Optional.empty();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
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

        Credentials credentials = new UsernamePasswordCredentials(username, password.toCharArray());
        if (authConfig.isPreemptiveAuthEnabled())
        {
            builder.addRequestInterceptorFirst((req, entity, ctx) ->
            {
                BasicScheme scheme = new BasicScheme(StandardCharsets.UTF_8);
                scheme.initPreemptive(credentials);
                String authResponse = scheme.generateAuthResponse(null, req, ctx);
                req.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, authResponse));
            });
        }
        else
        {
            CredentialsStore credentialsStore = new BasicCredentialsProvider();
            credentialsStore.setCredentials(ANY_AUTH_SCOPE, credentials);
            builder.setDefaultCredentialsProvider(credentialsStore);
        }
    }

    public void setPrivateKeyPassword(String privateKeyPassword)
    {
        this.privateKeyPassword = privateKeyPassword;
    }
}

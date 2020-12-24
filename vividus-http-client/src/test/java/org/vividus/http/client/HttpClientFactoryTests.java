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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.keystore.IKeyStoreFactory;

@ExtendWith(MockitoExtension.class)
class HttpClientFactoryTests
{
    private static final AuthScope AUTH_SCOPE = new AuthScope("host1", 1);
    private static final Map<String, String> HEADERS = Collections.singletonMap("header1", "value1");
    private static final String CREDS = "username:pass";

    @Mock private SslContextFactory sslContextFactory;
    @Mock private IKeyStoreFactory keyStoreFactory;
    @InjectMocks private HttpClientFactory httpClientFactory;

    @Mock private HttpClientBuilder mockedHttpClientBuilder;
    @Mock private CloseableHttpClient mockedApacheHttpClient;
    @Mock private CredentialsProvider credentialsProvider;

    private final HttpClientConfig config = new HttpClientConfig();

    @Test
    void testBuildHttpClientWithHeaders() throws GeneralSecurityException
    {
        config.setHeadersMap(HEADERS);

        testBuildHttpClientUsingConfig();
        verifyDefaultHeaderSetting(HEADERS.entrySet().iterator().next());
    }

    @Test
    void testBuildHttpClientWithFullAuthentication() throws GeneralSecurityException
    {
        config.setCredentials(CREDS);
        config.setAuthScope(AUTH_SCOPE);

        try (MockedStatic<ClientBuilderUtils> clientBuilderUtils = mockStatic(ClientBuilderUtils.class))
        {
            clientBuilderUtils.when(() -> ClientBuilderUtils.createCredentialsProvider(AUTH_SCOPE, CREDS)).thenReturn(
                    credentialsProvider);

            testBuildHttpClientUsingConfig();

            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        }
    }

    @Test
    void testBuildHttpClientWithAuthentication() throws GeneralSecurityException
    {
        config.setCredentials(CREDS);

        try (MockedStatic<ClientBuilderUtils> clientBuilderUtils = mockStatic(ClientBuilderUtils.class))
        {
            clientBuilderUtils.when(
                    () -> ClientBuilderUtils.createCredentialsProvider(ClientBuilderUtils.DEFAULT_AUTH_SCOPE, CREDS))
                    .thenReturn(credentialsProvider);

            testBuildHttpClientUsingConfig();

            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        }
    }

    @Test
    void testBuildHttpClientAuthenticationWithoutPass() throws GeneralSecurityException
    {
        try (MockedStatic<ClientBuilderUtils> clientBuilderUtils = mockStatic(ClientBuilderUtils.class))
        {
            testBuildHttpClientUsingConfig();

            verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any(CredentialsProvider.class));
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
            clientBuilderUtils.verifyNoInteractions();
        }
    }

    @Test
    void testBuildTrustedHttpClient() throws GeneralSecurityException
    {
        config.setCredentials(CREDS);
        config.setSslCertificateCheckEnabled(false);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getTrustingAllSslContext(SSLConnectionSocketFactory.SSL)).thenReturn(sslContext);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(credentialsProvider);
        verify(mockedHttpClientBuilder).setSSLContext(sslContext);
    }

    @Test
    void testBuildHostnameVerifierHttpClient() throws GeneralSecurityException
    {
        config.setCredentials(CREDS);
        config.setSslHostnameVerificationEnabled(false);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
    }

    @Test
    void testBuildDnsResolver() throws GeneralSecurityException
    {
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setDnsResolver(resolver);
    }

    @Test
    void testBuildCircularRedirects() throws GeneralSecurityException
    {
        config.setCircularRedirectsAllowed(true);
        try (MockedStatic<ClientBuilderUtils> clientBuilderUtils = mockStatic(ClientBuilderUtils.class);
                MockedStatic<HttpClientBuilder> httpClientBuilder = mockStatic(HttpClientBuilder.class);
                MockedConstruction<HttpClient> httpClient = mockConstruction(HttpClient.class))
        {
            clientBuilderUtils.when(
                    () -> ClientBuilderUtils.createCredentialsProvider(any(AuthScope.class), anyString())).thenReturn(
                    credentialsProvider);

            httpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedHttpClientBuilder);
            when(mockedHttpClientBuilder.build()).thenReturn(mockedApacheHttpClient);

            IHttpClient actualClient = httpClientFactory.buildHttpClient(config);
            assertEquals(httpClient.constructed(), List.of(actualClient));
            verify(mockedHttpClientBuilder).setDefaultRequestConfig(argThat(RequestConfig::isCircularRedirectsAllowed));
        }
    }

    @Test
    void testBuildHttpClientAllPossible() throws GeneralSecurityException
    {
        String baseUrl = "http://somewh.ere/";
        config.setBaseUrl(baseUrl);
        config.setHeadersMap(HEADERS);
        config.setCredentials(CREDS);
        config.setAuthScope(AUTH_SCOPE);
        config.setSslCertificateCheckEnabled(true);
        config.setSkipResponseEntity(true);
        CookieStore cookieStore = new BasicCookieStore();
        config.setCookieStore(cookieStore);
        config.setSslHostnameVerificationEnabled(false);
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);

        KeyStore keyStore = mock(KeyStore.class);
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));

        String privateKeyPassword = "privateKeyPassword";
        httpClientFactory.setPrivateKeyPassword(privateKeyPassword);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, keyStore, privateKeyPassword)).thenReturn(
                sslContext);

        try (MockedStatic<ClientBuilderUtils> clientBuilderUtils = mockStatic(ClientBuilderUtils.class))
        {
            clientBuilderUtils.when(() -> ClientBuilderUtils.createCredentialsProvider(AUTH_SCOPE, CREDS)).thenReturn(
                    credentialsProvider);

            testBuildHttpClientUsingConfig(httpClient -> verify(httpClient).setHttpHost(HttpHost.create(baseUrl)));
            verify(mockedHttpClientBuilder).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

            verify(mockedHttpClientBuilder).setSSLContext(sslContext);
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(mockedHttpClientBuilder).setDefaultCookieStore(cookieStore);
            verify(mockedHttpClientBuilder).setDnsResolver(resolver);
            verify(mockedHttpClientBuilder).useSystemProperties();
            verifyDefaultHeaderSetting(HEADERS.entrySet().iterator().next());
        }
    }

    private void testBuildHttpClientUsingConfig() throws GeneralSecurityException
    {
        testBuildHttpClientUsingConfig(httpClient -> { });
    }

    private void testBuildHttpClientUsingConfig(Consumer<HttpClient> httpClientVerifier) throws GeneralSecurityException
    {
        HttpClientConnectionManager connectionManager = mock(HttpClientConnectionManager.class);
        config.setConnectionManager(connectionManager);
        int maxTotalConnections = 10;
        config.setMaxTotalConnections(maxTotalConnections);
        int maxConnectionsPerRoute = 2;
        config.setMaxConnectionsPerRoute(maxConnectionsPerRoute);
        HttpRequestInterceptor requestInterceptor = mock(HttpRequestInterceptor.class);
        config.setLastRequestInterceptor(requestInterceptor);
        HttpResponseInterceptor responseInterceptor = mock(HttpResponseInterceptor.class);
        config.setLastResponseInterceptor(responseInterceptor);
        RedirectStrategy redirectStrategy = mock(RedirectStrategy.class);
        config.setRedirectStrategy(redirectStrategy);
        int connectionRequestTimeout = 20_000;
        config.setConnectionRequestTimeout(connectionRequestTimeout);
        int connectTimeout = 30_000;
        config.setConnectTimeout(connectTimeout);
        int socketTimeout = 10_000;
        config.setSocketTimeout(socketTimeout);
        String cookieSpec = "cookieSpec";
        config.setCookieSpec(cookieSpec);
        HttpRequestRetryHandler handler = mock(HttpRequestRetryHandler.class);
        config.setHttpRequestRetryHandler(handler);
        ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = mock(ServiceUnavailableRetryStrategy.class);
        config.setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy);
        try (MockedStatic<HttpClientBuilder> httpClientBuilder = mockStatic(HttpClientBuilder.class);
                MockedConstruction<HttpClient> httpClient = mockConstruction(HttpClient.class))
        {
            httpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedHttpClientBuilder);
            when(mockedHttpClientBuilder.build()).thenReturn(mockedApacheHttpClient);

            IHttpClient actualClient = httpClientFactory.buildHttpClient(config);
            assertEquals(httpClient.constructed(), List.of(actualClient));
            verify((HttpClient) actualClient).setCloseableHttpClient(mockedApacheHttpClient);
            verify((HttpClient) actualClient).setSkipResponseEntity(config.isSkipResponseEntity());
            httpClientVerifier.accept((HttpClient) actualClient);
            verify(mockedHttpClientBuilder).build();
            verify(mockedHttpClientBuilder).setRetryHandler(handler);
            verify(mockedHttpClientBuilder).setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy);
            verify(mockedHttpClientBuilder).setConnectionManager(connectionManager);
            verify(mockedHttpClientBuilder).setMaxConnTotal(maxTotalConnections);
            verify(mockedHttpClientBuilder).setMaxConnPerRoute(maxConnectionsPerRoute);
            verify(mockedHttpClientBuilder).addInterceptorLast(requestInterceptor);
            verify(mockedHttpClientBuilder).addInterceptorLast(responseInterceptor);
            verify(mockedHttpClientBuilder).setRedirectStrategy(redirectStrategy);
            verify(mockedHttpClientBuilder).setDefaultRequestConfig(
                    argThat(requestConfig -> requestConfig.getConnectTimeout() == connectTimeout
                            && requestConfig.getConnectionRequestTimeout() == connectionRequestTimeout
                            && requestConfig.getSocketTimeout() == socketTimeout
                            && requestConfig.getCookieSpec().equals(cookieSpec)));
            verify(mockedHttpClientBuilder).setDefaultSocketConfig(
                    argThat(socketConfig -> socketConfig.getSoTimeout() == socketTimeout));
        }
    }

    private void verifyDefaultHeaderSetting(Entry<String, String> headerEntry)
    {
        verify(mockedHttpClientBuilder).setDefaultHeaders(argThat(headers ->
        {
            if (headers.size() == 1)
            {
                Header header = headers.iterator().next();
                return headerEntry.getKey().equals(header.getName())
                        && headerEntry.getValue().equals(header.getValue());
            }
            return false;
        }));
    }
}

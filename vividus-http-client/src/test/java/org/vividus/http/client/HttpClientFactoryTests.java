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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.win.WinHttpClients;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.http.keystore.IKeyStoreFactory;

@ExtendWith(MockitoExtension.class)
class HttpClientFactoryTests
{
    private static final AuthScope ANY_AUTH_SCOPE = new AuthScope(null, -1);
    private static final Map<String, String> HEADERS = Collections.singletonMap("header1", "value1");
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Mock private SslContextFactory sslContextFactory;
    @Mock private IKeyStoreFactory keyStoreFactory;
    @InjectMocks private HttpClientFactory httpClientFactory;

    @Mock private HttpClientBuilder mockedHttpClientBuilder;
    @Mock private CloseableHttpClient mockedApacheHttpClient;

    private HttpClientConfig config;

    @BeforeEach
    void init()
    {
        config = new HttpClientConfig();
        config.setCircularRedirectsAllowed(false);
        SslConfig sslConfig = new SslConfig();
        sslConfig.setSslCertificateCheckEnabled(true);
        sslConfig.setSslHostnameVerificationEnabled(true);
        config.setSslConfig(sslConfig);
        config.setSkipResponseEntity(false);
        config.setConnectionRequestTimeout(0);
        config.setConnectTimeout(0);
        config.setSocketTimeout(0);
    }

    @Test
    void testBuildHttpClientWithHeadersAndInterceptors() throws GeneralSecurityException, URISyntaxException
    {
        config.setHeaders(HEADERS);
        config.setAuthConfig(authConfig(null, null, false));

        HttpRequestInterceptor firstRequestInterceptor = mock();
        config.setFirstRequestInterceptor(firstRequestInterceptor);

        HttpRequestInterceptor lastRequestInterceptor = mock();
        config.setLastRequestInterceptor(lastRequestInterceptor);

        HttpResponseInterceptor responseInterceptor = mock();
        config.setLastResponseInterceptor(responseInterceptor);

        testBuildHttpClientUsingConfig();
        Entry<String, String> headerEntry = HEADERS.entrySet().iterator().next();
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

        verify(mockedHttpClientBuilder).addRequestInterceptorFirst(firstRequestInterceptor);
        verify(mockedHttpClientBuilder).addRequestInterceptorLast(lastRequestInterceptor);
        verify(mockedHttpClientBuilder).addResponseInterceptorLast(responseInterceptor);
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationPreemptive() throws GeneralSecurityException, URISyntaxException
    {
        config.setAuthConfig(authConfig(USERNAME, PASSWORD, true));

        HttpRequest request = mock();
        HttpContext context = mock();
        doAnswer(args -> {
            HttpRequestInterceptor interceptor = args.getArgument(0);
            interceptor.process(request, null, context);
            return null;
        }).when(mockedHttpClientBuilder).addRequestInterceptorFirst(any(HttpRequestInterceptor.class));

        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

        ArgumentCaptor<Header> headerCaptor = ArgumentCaptor.forClass(Header.class);
        verify(request).addHeader(headerCaptor.capture());
        assertEquals("Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=", headerCaptor.getValue().toString());
        verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any());
        verify(connectionManagerBuilder).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationNoPreemptive() throws GeneralSecurityException, URISyntaxException
    {
        config.setAuthConfig(authConfig(USERNAME, PASSWORD, false));

        try (var credentialsProviderConstruction = mockConstruction(BasicCredentialsProvider.class))
        {
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

            ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);

            BasicCredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(credentialsProvider).setCredentials(eq(ANY_AUTH_SCOPE), credentialsCaptor.capture());
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(connectionManagerBuilder).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));

            Credentials credentials = credentialsCaptor.getValue();
            assertInstanceOf(UsernamePasswordCredentials.class, credentials);
            assertEquals(USERNAME, credentials.getUserPrincipal().getName());
            assertArrayEquals(PASSWORD.toCharArray(), credentials.getPassword());
        }

        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorFirst(any());
        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorLast(any());
        verify(mockedHttpClientBuilder, times(0)).addResponseInterceptorLast(any());
    }

    @ParameterizedTest
    @CsvSource({
        "username,,The password is missing",
        ",password,The username is missing"
    })
    void testBuildHttpClientWithAuthenticationInvalidUsernameAndPasswordParams(String username, String password,
            String message)
    {
        config.setAuthConfig(authConfig(username, password, false));

        var thrown = assertThrows(IllegalArgumentException.class, () -> httpClientFactory.buildHttpClient(config));
        assertEquals(message, thrown.getMessage());
    }

    @Test
    void testBuildHttpClientWithAuthenticationInvalidPreemptiveAuthParams()
    {
        config.setAuthConfig(authConfig(null, null, true));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> httpClientFactory.buildHttpClient(config));
        assertEquals("Preemptive authentication requires username and password to be set", thrown.getMessage());
    }

    @Test
    void testBuildHttpClientAuthenticationWithoutPass() throws GeneralSecurityException, URISyntaxException
    {
        config.setAuthConfig(authConfig(null, null, false));

        try (var credentialsProviderConstruction = mockConstruction(BasicCredentialsProvider.class))
        {
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

            verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any(CredentialsProvider.class));
            verify(connectionManagerBuilder).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
            assertThat(credentialsProviderConstruction.constructed(), hasSize(0));
        }
    }

    @Test
    void testBuildHttpClientTrustingAll() throws GeneralSecurityException, URISyntaxException
    {
        config.setAuthConfig(authConfig(USERNAME, PASSWORD, false));
        config.getSslConfig().setSslHostnameVerificationEnabled(false);
        config.getSslConfig().setSslCertificateCheckEnabled(false);

        SSLContext sslContext = mock();
        when(sslContextFactory.getTrustingAllSslContext()).thenReturn(sslContext);

        try (var connectionSocketFactoryBuilderStaticMock = mockStatic(SSLConnectionSocketFactoryBuilder.class))
        {
            SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = mock();
            SSLConnectionSocketFactory sslConnectionSocketFactory = mock();
            when(sslConnectionSocketFactoryBuilder.build()).thenReturn(sslConnectionSocketFactory);
            connectionSocketFactoryBuilderStaticMock.when(SSLConnectionSocketFactoryBuilder::create).thenReturn(
                    sslConnectionSocketFactoryBuilder);

            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

            verify(connectionManagerBuilder).setSSLSocketFactory(sslConnectionSocketFactory);
            verify(sslConnectionSocketFactoryBuilder).setSslContext(sslContext);
            verify(sslConnectionSocketFactoryBuilder).setHostnameVerifier(NoopHostnameVerifier.INSTANCE);
            verify(sslConnectionSocketFactoryBuilder).build();
            verifyNoMoreInteractions(sslConnectionSocketFactory);
        }
    }

    @Test
    void testBuildHttpClientWithCustomSslContext() throws GeneralSecurityException, URISyntaxException
    {
        String baseUrl = "http://somewh.ere";
        config.setBaseUrl(baseUrl);
        config.setAuthConfig(authConfig(null, null, false));
        config.getSslConfig().setSslHostnameVerificationEnabled(true);
        config.getSslConfig().setSslCertificateCheckEnabled(true);

        KeyStore keyStore = mock();
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));

        String privateKeyPassword = "privateKeyPassword";
        httpClientFactory.setPrivateKeyPassword(privateKeyPassword);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getSslContext(keyStore, privateKeyPassword)).thenReturn(sslContext);

        try (var connectionSocketFactoryBuilderStaticMock = mockStatic(SSLConnectionSocketFactoryBuilder.class))
        {
            SSLConnectionSocketFactoryBuilder sslConnectionSocketFactoryBuilder = mock();
            SSLConnectionSocketFactory sslConnectionSocketFactory = mock();
            when(sslConnectionSocketFactoryBuilder.build()).thenReturn(sslConnectionSocketFactory);
            connectionSocketFactoryBuilderStaticMock.when(SSLConnectionSocketFactoryBuilder::create).thenReturn(
                    sslConnectionSocketFactoryBuilder);

            HttpHost httpHost = HttpHost.create(baseUrl);
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig(
                    actualClient -> verify(actualClient).setHttpHost(httpHost)
            );

            verify(connectionManagerBuilder).setSSLSocketFactory(sslConnectionSocketFactory);
            verify(sslConnectionSocketFactoryBuilder).setSslContext(sslContext);
            verify(sslConnectionSocketFactoryBuilder).build();
            verifyNoMoreInteractions(sslConnectionSocketFactory);
        }
    }

    private PoolingHttpClientConnectionManagerBuilder testBuildHttpClientUsingConfig() throws GeneralSecurityException
    {
        return testBuildHttpClientUsingConfig(client -> { });
    }

    private PoolingHttpClientConnectionManagerBuilder testBuildHttpClientUsingConfig(
            Consumer<HttpClient> httpClientVerifier) throws GeneralSecurityException
    {
        HttpResponseHandler responseHandler = mock();
        config.setHttpResponseHandlers(List.of(responseHandler));
        int maxTotalConnections = 10;
        config.setMaxTotalConnections(maxTotalConnections);
        int maxConnectionsPerRoute = 2;
        config.setMaxConnectionsPerRoute(maxConnectionsPerRoute);
        DnsResolver dnsResolver = mock(DnsResolver.class);
        config.setDnsResolver(dnsResolver);
        RedirectStrategy redirectStrategy = mock();
        config.setRedirectStrategy(redirectStrategy);
        int connectTimeout = 30_000;
        config.setConnectTimeout(connectTimeout);
        int socketTimeout = 10_000;
        config.setSocketTimeout(socketTimeout);
        int connectionRequestTimeout = 20_000;
        config.setConnectionRequestTimeout(connectionRequestTimeout);
        boolean circularRedirectsAllowed = true;
        config.setCircularRedirectsAllowed(circularRedirectsAllowed);
        String cookieSpec = "cookieSpec";
        config.setCookieSpec(cookieSpec);
        CookieStore cookieStore = new BasicCookieStore();
        config.setCookieStore(cookieStore);
        HttpRequestRetryStrategy retryStrategy = mock();
        config.setHttpRequestRetryStrategy(retryStrategy);
        try (var connectionManagerBuilderStaticMock = mockStatic(PoolingHttpClientConnectionManagerBuilder.class);
                var winHttpClients = mockStatic(WinHttpClients.class);
                var httpClient = mockConstruction(HttpClient.class))
        {
            winHttpClients.when(WinHttpClients::custom).thenReturn(mockedHttpClientBuilder);
            when(mockedHttpClientBuilder.build()).thenReturn(mockedApacheHttpClient);

            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = mock(Answers.RETURNS_SELF);
            connectionManagerBuilderStaticMock.when(PoolingHttpClientConnectionManagerBuilder::create).thenReturn(
                    connectionManagerBuilder);

            IHttpClient client = httpClientFactory.buildHttpClient(config);
            assertEquals(httpClient.constructed(), List.of(client));
            @SuppressWarnings("PMD.CloseResource")
            HttpClient actualClient = (HttpClient) client;
            verify(actualClient).setCloseableHttpClient(mockedApacheHttpClient);
            verify(actualClient).setSkipResponseEntity(config.isSkipResponseEntity());
            verify(actualClient).setHttpResponseHandlers(List.of(responseHandler));
            httpClientVerifier.accept(actualClient);
            verify(connectionManagerBuilder).setMaxConnTotal(maxTotalConnections);
            verify(connectionManagerBuilder).setMaxConnPerRoute(maxConnectionsPerRoute);
            verify(connectionManagerBuilder).setDnsResolver(dnsResolver);
            verify(connectionManagerBuilder).setDefaultSocketConfig(
                    argThat(socketConfig -> socketConfig.getSoTimeout().toMilliseconds() == socketTimeout));
            verify(connectionManagerBuilder).setDefaultConnectionConfig(
                    argThat(connectionConfig -> connectionConfig.getConnectTimeout().toMilliseconds() == connectTimeout
                            && connectionConfig.getSocketTimeout().toMilliseconds() == socketTimeout));
            verify(mockedHttpClientBuilder).setDefaultCookieStore(cookieStore);
            verify(mockedHttpClientBuilder).setRetryStrategy(retryStrategy);
            verify(mockedHttpClientBuilder).setRedirectStrategy(redirectStrategy);
            verify(mockedHttpClientBuilder).setDefaultRequestConfig(
                    argThat(config -> config.getConnectionRequestTimeout().toMilliseconds() == connectionRequestTimeout
                            && config.isCircularRedirectsAllowed() == circularRedirectsAllowed
                            && config.getCookieSpec().equals(cookieSpec)));
            verify(mockedHttpClientBuilder).build();
            return connectionManagerBuilder;
        }
    }

    private static AuthConfig authConfig(String username, String password, boolean preemptiveAuthEnabled)
    {
        AuthConfig authConfig = new AuthConfig();
        authConfig.setUsername(username);
        authConfig.setPassword(password);
        authConfig.setPreemptiveAuthEnabled(preemptiveAuthEnabled);
        return authConfig;
    }
}

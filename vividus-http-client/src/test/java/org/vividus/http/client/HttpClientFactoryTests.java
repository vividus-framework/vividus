/*
 * Copyright 2019-2025 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.function.FailableSupplier;
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
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    private static final String ANOTHER_USERNAME = "another-username";
    private static final String ANOTHER_PASSWORD = "another-password";
    private static final String ORIGIN = "https://example.com";
    private static final String ANOTHER_ORIGIN = "https://another.com";
    private static final String CUSTOM_SCOPE = "custom-scope";
    private static final String ANOTHER_SCOPE = "another-scope";
    private static final String ANY_SCOPE = "any";
    private static final String ANY_ORIGIN = "*";

    private static final String MISSING_CONFIG_FORMAT = "The '%s' parameter is missing for '%s' %s configuration";
    private static final String AUTHENTICATION = "authentication";
    private static final String HTTP_CONTEXT = "HTTP context";
    private static final String ORIGIN_NAME = "origin";

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
        config.setHttpContextConfig(Map.of());
    }

    @Test
    void testBuildHttpClientWithHeadersAndInterceptors() throws GeneralSecurityException
    {
        config.setHeaders(HEADERS);

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
    void testBuildHttpClientWithFullAuthenticationPreemptiveCustomOrigin()
            throws GeneralSecurityException, URISyntaxException
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(ORIGIN, USERNAME, PASSWORD, true),
            ANOTHER_SCOPE, contextConfig(ANOTHER_ORIGIN, ANOTHER_USERNAME, ANOTHER_PASSWORD, true),
            ANY_SCOPE, contextConfig(ANY_ORIGIN, ANY_SCOPE + USERNAME, ANY_SCOPE + PASSWORD, true)
        ));

        HttpRequest request = mock();
        when(request.getUri()).thenReturn(URI.create(ORIGIN));
        HttpContext context = mock();
        doAnswer(args -> {
            HttpRequestInterceptor interceptor = args.getArgument(0);
            interceptor.process(request, null, context);
            return null;
        }).when(mockedHttpClientBuilder).addRequestInterceptorFirst(any(HttpRequestInterceptor.class));

        runWithHeaderCheck(request);
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationPreemptiveAnyScope()
            throws GeneralSecurityException, URISyntaxException
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(ORIGIN, ANOTHER_USERNAME, ANOTHER_PASSWORD, true),
            ANY_SCOPE, contextConfig(ANY_ORIGIN, USERNAME, PASSWORD, true)
        ));

        HttpRequest request = mock();
        when(request.getUri()).thenReturn(URI.create(ANOTHER_ORIGIN));
        HttpContext context = mock();
        doAnswer(args -> {
            HttpRequestInterceptor interceptor = args.getArgument(0);
            interceptor.process(request, null, context);
            return null;
        }).when(mockedHttpClientBuilder).addRequestInterceptorFirst(any(HttpRequestInterceptor.class));

        runWithHeaderCheck(request);
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationPreemptiveNoMatch()
            throws GeneralSecurityException, URISyntaxException
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(ORIGIN, ANOTHER_USERNAME, ANOTHER_PASSWORD, true)
        ));

        HttpRequest request = mock();
        when(request.getUri()).thenReturn(URI.create(ANOTHER_ORIGIN));
        HttpContext context = mock();
        doAnswer(args -> {
            HttpRequestInterceptor interceptor = args.getArgument(0);
            interceptor.process(request, null, context);
            return null;
        }).when(mockedHttpClientBuilder).addRequestInterceptorFirst(any(HttpRequestInterceptor.class));

        testBuildHttpClientUsingConfig();
        verifyNoMoreInteractions(request);
    }

    private void runWithHeaderCheck(HttpRequest request) throws GeneralSecurityException
    {
        runWithHeaderCheck(request, 1,
                c -> assertEquals("Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=", c.getValue().toString()));
    }

    private void runWithHeaderCheck(HttpRequest request, int times, Consumer<ArgumentCaptor<Header>> captor)
            throws GeneralSecurityException
    {
        testHttpClientBuildWithDefaultSslConfig(() -> {
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

            ArgumentCaptor<Header> headerCaptor = ArgumentCaptor.forClass(Header.class);
            verify(request, times(times)).addHeader(headerCaptor.capture());
            captor.accept(headerCaptor);
            verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any());
            return connectionManagerBuilder;
        });
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationNoPreemptiveCustomOrigin() throws GeneralSecurityException
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(ORIGIN, USERNAME, PASSWORD, false)
        ));

        try (var credentialsProviderConstruction = mockConstruction(BasicCredentialsProvider.class))
        {
            testHttpClientBuildWithDefaultSslConfig(this::testBuildHttpClientUsingConfig);

            ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);
            ArgumentCaptor<AuthScope> authScopeCaptor = ArgumentCaptor.forClass(AuthScope.class);

            BasicCredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(credentialsProvider).setCredentials(authScopeCaptor.capture(), credentialsCaptor.capture());
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);

            validateCredentials(credentialsCaptor);

            AuthScope authScope = authScopeCaptor.getValue();
            assertEquals(URI.create(ORIGIN).getHost(), authScope.getHost());
        }

        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorFirst(any());
        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorLast(any());
        verify(mockedHttpClientBuilder, times(0)).addResponseInterceptorLast(any());
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationNoPreemptiveAnyScope() throws GeneralSecurityException
    {
        config.setHttpContextConfig(Map.of(
            ANY_SCOPE, contextConfig(ANY_ORIGIN, USERNAME, PASSWORD, false)
        ));

        try (var credentialsProviderConstruction = mockConstruction(BasicCredentialsProvider.class))
        {
            testHttpClientBuildWithDefaultSslConfig(this::testBuildHttpClientUsingConfig);

            ArgumentCaptor<Credentials> credentialsCaptor = ArgumentCaptor.forClass(Credentials.class);

            BasicCredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(credentialsProvider).setCredentials(eq(ANY_AUTH_SCOPE), credentialsCaptor.capture());
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);

            validateCredentials(credentialsCaptor);
        }

        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorFirst(any());
        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorLast(any());
        verify(mockedHttpClientBuilder, times(0)).addResponseInterceptorLast(any());
    }

    @Test
    void testBuildHttpClientWithContextHeaders() throws GeneralSecurityException, URISyntaxException
    {
        String header1 = "header-1";
        String value1 = "value-1";
        String header2 = "header-2";
        String value2 = "value-2";
        String header3 = "header-common";
        String value3 = "any-origin-header";

        config.setHttpContextConfig(
                Map.of("any-origin-config", contextConfig(ANY_ORIGIN, Map.of(header2, value2, header1, value1)),
                        "example-com", contextConfig(ORIGIN, Map.of(header3, value3))));

        HttpRequest request = mock();
        when(request.getUri()).thenReturn(URI.create(ORIGIN));
        HttpContext context = mock();
        doAnswer(args -> {
            HttpRequestInterceptor interceptor = args.getArgument(0);
            interceptor.process(request, null, context);
            return null;
        }).when(mockedHttpClientBuilder).addRequestInterceptorFirst(any(HttpRequestInterceptor.class));

        runWithHeaderCheck(request, 3, captor ->
        {
            List<Header> headers = captor.getAllValues();
            assertEquals(3, headers.size());

            Collections.sort(headers, Comparator.comparing(Header::getName));
            validateHeader(headers.get(0), header1, value1);
            validateHeader(headers.get(1), header2, value2);
            validateHeader(headers.get(2), header3, value3);
        });
    }

    @Test
    void testBuildHttpClientWithInvalidOrigin()
    {
        String key = "config-with-invalid-origin";
        config.setHttpContextConfig(Map.of(key, contextConfig(null, Map.of("h1", "v1"))));

        var thrown = assertThrows(IllegalArgumentException.class, () -> httpClientFactory.buildHttpClient(config));
        assertEquals(MISSING_CONFIG_FORMAT.formatted(ORIGIN_NAME, key, HTTP_CONTEXT), thrown.getMessage());
    }

    private void validateHeader(Header header, String headerName, String headerValue)
    {
        assertEquals(headerName, header.getName());
        assertEquals(headerValue, header.getValue());
    }

    @Test
    void testBuildHttpClientWithDefaultConfigs() throws GeneralSecurityException
    {
        config.setHttpContextConfig(Map.of(
            "003e952c9a", contextConfig(ANY_ORIGIN, null, null, false)
        ));

        testHttpClientBuildWithDefaultSslConfig(this::testBuildHttpClientUsingConfig);

        verify(mockedHttpClientBuilder, times(0)).setDefaultCredentialsProvider(any());
        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorFirst(any());
        verify(mockedHttpClientBuilder, times(0)).addRequestInterceptorLast(any());
        verify(mockedHttpClientBuilder, times(0)).addResponseInterceptorLast(any());
    }

    private void validateCredentials(ArgumentCaptor<Credentials> captor)
    {
        Credentials credentials = captor.getValue();
        assertInstanceOf(UsernamePasswordCredentials.class, credentials);
        assertEquals(USERNAME, credentials.getUserPrincipal().getName());
        assertArrayEquals(PASSWORD.toCharArray(),
                ((UsernamePasswordCredentials) credentials).getUserPassword());
    }

    static Stream<Arguments> invalidArgsMatrix()
    {
        return Stream.of(
            arguments(null, USERNAME, PASSWORD, ORIGIN_NAME, HTTP_CONTEXT),
            arguments(ORIGIN, null, PASSWORD, USERNAME, AUTHENTICATION),
            arguments(ORIGIN, USERNAME, null, PASSWORD, AUTHENTICATION),
            arguments(ORIGIN, null, null, USERNAME, AUTHENTICATION)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgsMatrix")
    void testBuildHttpClientWithAuthenticationInvalidParams(String origin, String username, String password,
            String messageKey, String componentKey)
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(origin, username, password, false)
        ));

        var thrown = assertThrows(IllegalArgumentException.class, () -> httpClientFactory.buildHttpClient(config));
        String message = MISSING_CONFIG_FORMAT.formatted(messageKey, CUSTOM_SCOPE, componentKey);
        assertEquals(message, thrown.getMessage());
    }

    @Test
    void testBuildHttpClientWithAuthenticationConflictingScopes()
    {
        config.setHttpContextConfig(Map.of(
            CUSTOM_SCOPE, contextConfig(ORIGIN, USERNAME, PASSWORD, true),
            ANOTHER_SCOPE, contextConfig(ORIGIN, USERNAME, PASSWORD, true)
        ));

        var thrown = assertThrows(IllegalArgumentException.class, () -> httpClientFactory.buildHttpClient(config));
        assertTrue(thrown.getMessage().matches("Found conflicting origin URLs in (custom-scope, another-scope|"
                + "another-scope, custom-scope) configurations"));
    }

    @Test
    void testBuildHttpClientAuthenticationWithoutPass() throws GeneralSecurityException
    {
        testHttpClientBuildWithDefaultSslConfig(() -> {
            try (var credentialsProviderConstruction = mockConstruction(BasicCredentialsProvider.class))
            {
                PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = testBuildHttpClientUsingConfig();

                verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any(CredentialsProvider.class));
                assertThat(credentialsProviderConstruction.constructed(), hasSize(0));

                return connectionManagerBuilder;
            }
        });
    }

    @Test
    void testBuildHttpClientTrustingAll() throws GeneralSecurityException
    {
        config.getSslConfig().setSslHostnameVerificationEnabled(false);
        config.getSslConfig().setSslCertificateCheckEnabled(false);

        SSLContext sslContext = mock();
        when(sslContextFactory.getTrustingAllSslContext()).thenReturn(sslContext);

        testHttpClientBuildWithSslConfig(sslContext, NoopHostnameVerifier.INSTANCE,
                this::testBuildHttpClientUsingConfig);
    }

    @Test
    void testBuildHttpClientWithCustomSslContext() throws GeneralSecurityException
    {
        String baseUrl = "http://somewh.ere";
        config.setBaseUrl(baseUrl);
        config.getSslConfig().setSslHostnameVerificationEnabled(true);
        config.getSslConfig().setSslCertificateCheckEnabled(true);

        KeyStore keyStore = mock();
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));

        String privateKeyPassword = "privateKeyPassword";
        httpClientFactory.setPrivateKeyPassword(privateKeyPassword);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getSslContext(keyStore, privateKeyPassword)).thenReturn(sslContext);

        try (var httpsSupportStaticMock = mockStatic(HttpsSupport.class))
        {
            HostnameVerifier hostnameVerifier = mock();
            httpsSupportStaticMock.when(HttpsSupport::getDefaultHostnameVerifier).thenReturn(hostnameVerifier);
            testHttpClientBuildWithSslConfig(sslContext, hostnameVerifier, () ->
            {
                HttpHost httpHost = HttpHost.create(URI.create(baseUrl));

                return testBuildHttpClientUsingConfig(actualClient -> verify(actualClient).setHttpHost(httpHost));
            });
        }
    }

    private void testHttpClientBuildWithDefaultSslConfig(
            FailableSupplier<PoolingHttpClientConnectionManagerBuilder, GeneralSecurityException> test)
            throws GeneralSecurityException
    {
        try (var sslContextsStaticMock = mockStatic(SSLContexts.class);
                var httpsSupportStaticMock = mockStatic(HttpsSupport.class))
        {
            var sslContext = mock(SSLContext.class);
            sslContextsStaticMock.when(SSLContexts::createSystemDefault).thenReturn(sslContext);

            HostnameVerifier hostnameVerifier = mock();
            httpsSupportStaticMock.when(HttpsSupport::getDefaultHostnameVerifier).thenReturn(hostnameVerifier);

            testHttpClientBuildWithSslConfig(sslContext, hostnameVerifier, test);
        }
    }

    private static void testHttpClientBuildWithSslConfig(SSLContext sslContext, HostnameVerifier hostnameVerifier,
            FailableSupplier<PoolingHttpClientConnectionManagerBuilder, GeneralSecurityException> test)
            throws GeneralSecurityException
    {
        try (var defaultClientTlsStrategy = mockConstruction(DefaultClientTlsStrategy.class, (mock, context) -> {
            assertEquals(1, context.getCount());
            assertEquals(List.of(sslContext, hostnameVerifier), context.arguments());
        }))
        {
            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = test.get();

            verify(connectionManagerBuilder).setTlsSocketStrategy(defaultClientTlsStrategy.constructed().get(0));
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
        var cookieStoreProvider = mock(CookieStoreProvider.class);
        CookieStore cookieStore = new BasicCookieStore();
        when(cookieStoreProvider.getCookieStore()).thenReturn(cookieStore);
        config.setCookieStoreProvider(cookieStoreProvider);
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
            //noinspection resource
            verify(mockedHttpClientBuilder).build();
            return connectionManagerBuilder;
        }
    }

    private static HttpContextConfig contextConfig(String origin, String username, String password,
            boolean preemptiveAuthEnabled)
    {
        return contextConfig(origin, null, username, password, preemptiveAuthEnabled);
    }

    private static HttpContextConfig contextConfig(String origin, Map<String, String> headers)
    {
        HttpContextConfig contextConfig = new HttpContextConfig();
        contextConfig.setOrigin(origin);
        contextConfig.setHeaders(headers);
        contextConfig.setAuth(null);
        return contextConfig;
    }

    private static HttpContextConfig contextConfig(String origin, Map<String, String> headers, String username,
            String password, boolean preemptiveAuthEnabled)
    {
        HttpContextConfig contextConfig = new HttpContextConfig();
        contextConfig.setOrigin(origin);
        contextConfig.setHeaders(headers);

        BasicAuthConfig authConfig = new BasicAuthConfig();
        authConfig.setUsername(username);
        authConfig.setPassword(password);
        authConfig.setPreemptiveAuthEnabled(preemptiveAuthEnabled);
        contextConfig.setAuth(authConfig);

        return contextConfig;
    }
}

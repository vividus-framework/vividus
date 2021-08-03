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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.http.keystore.IKeyStoreFactory;

@ExtendWith(MockitoExtension.class)
class HttpClientFactoryTests
{
    private static final AuthScope AUTH_SCOPE = new AuthScope("host1", 1);
    private static final Map<String, String> HEADERS = Collections.singletonMap("header1", "value1");
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Mock private SslContextFactory sslContextFactory;
    @Mock private IKeyStoreFactory keyStoreFactory;
    @InjectMocks private HttpClientFactory httpClientFactory;

    @Mock private HttpClientBuilder mockedHttpClientBuilder;
    @Mock private CloseableHttpClient mockedApacheHttpClient;

    private final HttpClientConfig config = new HttpClientConfig();

    @Test
    void testBuildHttpClientWithHeaders() throws GeneralSecurityException
    {
        config.setHeadersMap(HEADERS);

        testBuildHttpClientUsingConfig();
        verifyDefaultHeaderSetting(HEADERS.entrySet().iterator().next());
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationNoPreemptive() throws GeneralSecurityException
    {
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setAuthScope(AUTH_SCOPE);

        try (MockedConstruction<BasicCredentialsProvider> credentialsProviderConstruction = mockConstruction(
                BasicCredentialsProvider.class))
        {
            testBuildHttpClientUsingConfig();

            CredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(credentialsProvider).setCredentials(eq(AUTH_SCOPE), argThat(usernamePasswordCredentialsMatcher()));
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        }
    }

    @Test
    void testBuildHttpClientWithFullAuthenticationPreemptive() throws GeneralSecurityException
    {
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setPreemptiveAuthEnabled(true);

        Header header = mock(Header.class);
        try (MockedConstruction<BasicScheme> basicSchemeConstruction = mockConstruction(BasicScheme.class,
                (scheme, context) -> when(scheme.authenticate(argThat(usernamePasswordCredentialsMatcher()),
                        any(HttpRequest.class), any(HttpContext.class))).thenReturn(header)))
        {
            HttpRequest request = mock(HttpRequest.class);
            HttpContext context = mock(HttpContext.class);
            doAnswer(args ->
            {
                HttpRequestInterceptor interceptor = args.getArgument(0);
                interceptor.process(request, context);
                return null;
            }).when(mockedHttpClientBuilder).addInterceptorFirst(any(HttpRequestInterceptor.class));

            testBuildHttpClientUsingConfig();

            verify(request).addHeader(header);
            verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any());
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        }
    }

    @Test
    void testBuildHttpClientWithAuthentication() throws GeneralSecurityException
    {
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        try (MockedConstruction<BasicCredentialsProvider> credentialsProviderConstruction = mockConstruction(
                BasicCredentialsProvider.class))
        {
            testBuildHttpClientUsingConfig();

            CredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(credentialsProvider).setCredentials(eq(AuthScope.ANY),
                    argThat(usernamePasswordCredentialsMatcher()));
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
            verify(mockedHttpClientBuilder, never()).addInterceptorFirst(any(HttpRequestInterceptor.class));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "username,,The password is missing",
        ",password,The username is missing"
    })
    void testBuildHttpClientWithAuthenticationInvalidUsernameAndPasswordParams(String username, String password,
            String message)
    {
        config.setUsername(username);
        config.setPassword(password);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> httpClientFactory.buildHttpClient(config));
        assertEquals(message, thrown.getMessage());
    }

    @Test
    void testBuildHttpClientWithAuthenticationInvalidPreemptiveAuthParams()
    {
        config.setPreemptiveAuthEnabled(true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> httpClientFactory.buildHttpClient(config));
        assertEquals("Preemptive authentication requires username and password to be set", thrown.getMessage());
    }

    @Test
    void testBuildHttpClientAuthenticationWithoutPass() throws GeneralSecurityException
    {
        try (MockedConstruction<BasicCredentialsProvider> credentialsProviderConstruction = mockConstruction(
                BasicCredentialsProvider.class))
        {
            testBuildHttpClientUsingConfig();

            verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any(CredentialsProvider.class));
            verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
            assertThat(credentialsProviderConstruction.constructed(), hasSize(0));
        }
    }

    @Test
    void testBuildTrustedHttpClient() throws GeneralSecurityException
    {
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setSslCertificateCheckEnabled(false);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getTrustingAllSslContext(SSLConnectionSocketFactory.SSL)).thenReturn(sslContext);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setSSLContext(sslContext);
    }

    @Test
    void testBuildHostnameVerifierHttpClient() throws GeneralSecurityException
    {
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
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
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setCircularRedirectsAllowed(true);

        try (MockedConstruction<BasicCredentialsProvider> credentialsProviderConstruction = mockConstruction(
                BasicCredentialsProvider.class);
                MockedStatic<HttpClientBuilder> httpClientBuilder = mockStatic(HttpClientBuilder.class);
                MockedConstruction<HttpClient> httpClient = mockConstruction(HttpClient.class))
        {
            httpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedHttpClientBuilder);
            when(mockedHttpClientBuilder.build()).thenReturn(mockedApacheHttpClient);

            IHttpClient actualClient = httpClientFactory.buildHttpClient(config);
            assertEquals(httpClient.constructed(), List.of(actualClient));
            verify(mockedHttpClientBuilder).setDefaultRequestConfig(argThat(RequestConfig::isCircularRedirectsAllowed));
            CredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(credentialsProvider).setCredentials(eq(AuthScope.ANY),
                    argThat(usernamePasswordCredentialsMatcher()));
        }
    }

    @Test
    void testBuildHttpClientAllPossible() throws GeneralSecurityException
    {
        String baseUrl = "http://somewh.ere/";
        config.setBaseUrl(baseUrl);
        config.setHeadersMap(HEADERS);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setAuthScope(AUTH_SCOPE);
        config.setSslCertificateCheckEnabled(true);
        config.setSkipResponseEntity(true);
        CookieStore cookieStore = new BasicCookieStore();
        config.setCookieStore(cookieStore);
        config.setSslHostnameVerificationEnabled(false);
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);
        HttpResponseHandler handler = mock(HttpResponseHandler.class);
        config.setHttpResponseHandlers(List.of(handler));

        KeyStore keyStore = mock(KeyStore.class);
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));

        String privateKeyPassword = "privateKeyPassword";
        httpClientFactory.setPrivateKeyPassword(privateKeyPassword);

        SSLContext sslContext = mock(SSLContext.class);
        when(sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, keyStore, privateKeyPassword)).thenReturn(
                sslContext);

        try (MockedConstruction<BasicCredentialsProvider> credentialsProviderConstruction = mockConstruction(
                BasicCredentialsProvider.class))
        {
            testBuildHttpClientUsingConfig(httpClient ->
            {
                verify(httpClient).setHttpHost(HttpHost.create(baseUrl));
                verify(httpClient).setHttpResponseHandlers(List.of(handler));
            });
            verify(mockedHttpClientBuilder).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);

            verify(mockedHttpClientBuilder).setSSLContext(sslContext);
            CredentialsProvider credentialsProvider = credentialsProviderConstruction.constructed().get(0);
            verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
            verify(credentialsProvider).setCredentials(eq(AUTH_SCOPE), argThat(usernamePasswordCredentialsMatcher()));
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

    private ArgumentMatcher<Credentials> usernamePasswordCredentialsMatcher()
    {
        return arg ->
        {
            UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) arg;
            return USERNAME.equals(credentials.getUserName()) && PASSWORD.equals(credentials.getPassword());
        };
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

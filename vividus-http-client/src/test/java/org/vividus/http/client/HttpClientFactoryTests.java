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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpClientBuilder.class, ClientBuilderUtils.class, HttpClientFactory.class })
public class HttpClientFactoryTests
{
    private static final AuthScope AUTH_SCOPE = new AuthScope("host1", 1);
    private static final Map<String, String> HEADERS = Collections.singletonMap("header1", "value1");
    private static final String CREDS = "username:pass";

    @Mock
    private ISslContextFactory mockedSSLContextManager;

    private final HttpClientBuilder mockedHttpClientBuilder = PowerMockito.mock(HttpClientBuilder.class);

    @Mock
    private CloseableHttpClient mockedApacheHttpClient;

    @Mock
    private HttpClient mockedHttpClient;

    @InjectMocks
    private HttpClientFactory httpClientFactory;

    @Mock
    private CredentialsProvider credentialsProvider;

    private final HttpClientConfig config = new HttpClientConfig();

    @Before
    public void before() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(HttpClientBuilder.class);
        when(HttpClientBuilder.create()).thenReturn(mockedHttpClientBuilder);
        when(mockedHttpClientBuilder.build()).thenReturn(mockedApacheHttpClient);

        PowerMockito.mock(HttpClient.class);
        PowerMockito.whenNew(HttpClient.class).withNoArguments().thenReturn(mockedHttpClient);
    }

    @Test
    public void testBuildHttpClientWithHeaders()
    {
        config.setHeadersMap(HEADERS);

        testBuildHttpClientUsingConfig();
        verifyDefaultHeaderSetting(HEADERS.entrySet().iterator().next());
    }

    @Test
    public void testBuildHttpClientWithFullAuthentication()
    {
        config.setCredentials(CREDS);
        config.setAuthScope(AUTH_SCOPE);

        prepareClientBuilderUtilsMock();

        testBuildHttpClientUsingConfig();

        verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
        verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        PowerMockito.verifyStatic(ClientBuilderUtils.class);
        ClientBuilderUtils.createCredentialsProvider(AUTH_SCOPE, CREDS);
    }

    @Test
    public void testBuildHttpClientWithAuthentication()
    {
        config.setCredentials(CREDS);
        prepareClientBuilderUtilsMock();

        testBuildHttpClientUsingConfig();

        verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
        verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        PowerMockito.verifyStatic(ClientBuilderUtils.class);
        ClientBuilderUtils.createCredentialsProvider(ClientBuilderUtils.DEFAULT_AUTH_SCOPE, CREDS);
    }

    @Test
    public void testBuildHttpClientAuthenticationWithoutPass()
    {
        prepareClientBuilderUtilsMock();

        testBuildHttpClientUsingConfig();

        verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(any(CredentialsProvider.class));
        verify(mockedHttpClientBuilder, never()).setSSLSocketFactory(any(SSLConnectionSocketFactory.class));
        PowerMockito.verifyStatic(ClientBuilderUtils.class, never());
        ClientBuilderUtils.createCredentialsProvider(any(AuthScope.class), anyString(), anyString());
    }

    @Test
    public void testBuildTrustedHttpClient()
    {
        config.setCredentials(CREDS);
        config.setSslCertificateCheckEnabled(false);

        SSLContext mockedSSLContext = mock(SSLContext.class);
        when(mockedSSLContextManager.getSslContext(SSLConnectionSocketFactory.SSL, true))
                .thenReturn(Optional.of(mockedSSLContext));
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder, never()).setDefaultCredentialsProvider(credentialsProvider);
        verify(mockedHttpClientBuilder).setSSLContext(mockedSSLContext);
    }

    @Test
    public void testBuildHostnameVerifierHttpClient()
    {
        config.setCredentials(CREDS);
        config.setSslHostnameVerificationEnabled(false);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
    }

    @Test
    public void testBuildDnsResolver()
    {
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);
        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setDnsResolver(resolver);
    }

    @Test
    public void testBuildCircularRedirects()
    {
        config.setCircularRedirectsAllowed(true);
        prepareClientBuilderUtilsMock();
        httpClientFactory.buildHttpClient(config);
        verify(mockedHttpClientBuilder).setDefaultRequestConfig(argThat(RequestConfig::isCircularRedirectsAllowed));
    }

    @Test
    public void testBuildHttpClientAllPossible()
    {
        String baseUrl = "http://somewh.ere/";
        config.setBaseUrl(baseUrl);
        config.setHeadersMap(HEADERS);
        config.setCredentials(CREDS);
        config.setAuthScope(AUTH_SCOPE);
        config.setSslCertificateCheckEnabled(false);
        config.setSkipResponseEntity(true);
        CookieStore cookieStore = new BasicCookieStore();
        config.setCookieStore(cookieStore);
        config.setSslHostnameVerificationEnabled(false);
        DnsResolver resolver = mock(DnsResolver.class);
        config.setDnsResolver(resolver);

        SSLContext mockedSSLContext = mock(SSLContext.class);
        when(mockedSSLContextManager.getSslContext(SSLConnectionSocketFactory.SSL, true))
                .thenReturn(Optional.of(mockedSSLContext));

        prepareClientBuilderUtilsMock();

        testBuildHttpClientUsingConfig();
        verify(mockedHttpClientBuilder).setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        verify(mockedHttpClient).setHttpHost(HttpHost.create(baseUrl));
        verify(mockedHttpClient).setSkipResponseEntity(config.isSkipResponseEntity());
        verify(mockedHttpClientBuilder).setSSLContext(mockedSSLContext);
        verify(mockedHttpClientBuilder).setDefaultCredentialsProvider(credentialsProvider);
        verify(mockedHttpClientBuilder).setDefaultCookieStore(cookieStore);
        verify(mockedHttpClientBuilder).setDnsResolver(resolver);
        verifyDefaultHeaderSetting(HEADERS.entrySet().iterator().next());
        PowerMockito.verifyStatic(ClientBuilderUtils.class);
        ClientBuilderUtils.createCredentialsProvider(AUTH_SCOPE, CREDS);
    }

    private void testBuildHttpClientUsingConfig()
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
        IHttpClient actualClient = httpClientFactory.buildHttpClient(config);
        verifyBaseClientCreationPath(actualClient);
        verify(mockedHttpClientBuilder).setRetryHandler(handler);
        verify(mockedHttpClientBuilder).setConnectionManager(connectionManager);
        verify(mockedHttpClientBuilder).setMaxConnTotal(maxTotalConnections);
        verify(mockedHttpClientBuilder).setMaxConnPerRoute(maxConnectionsPerRoute);
        verify(mockedHttpClientBuilder).addInterceptorLast(requestInterceptor);
        verify(mockedHttpClientBuilder).addInterceptorLast(responseInterceptor);
        verify(mockedHttpClientBuilder).setRedirectStrategy(redirectStrategy);
        verify(mockedHttpClientBuilder)
                .setDefaultRequestConfig(argThat(requestConfig -> requestConfig.getConnectTimeout() == connectTimeout
                        && requestConfig.getConnectionRequestTimeout() == connectionRequestTimeout
                        && requestConfig.getSocketTimeout() == socketTimeout
                        && requestConfig.getCookieSpec().equals(cookieSpec)));
        verify(mockedHttpClientBuilder)
                .setDefaultSocketConfig(argThat(socketConfig -> socketConfig.getSoTimeout() == socketTimeout));
    }

    private void prepareClientBuilderUtilsMock()
    {
        PowerMockito.mockStatic(ClientBuilderUtils.class);
        when(ClientBuilderUtils.createCredentialsProvider(any(AuthScope.class), anyString()))
                .thenReturn(credentialsProvider);
    }

    private void verifyBaseClientCreationPath(IHttpClient actualClient)
    {
        assertEquals(mockedHttpClient, actualClient);
        PowerMockito.verifyStatic(HttpClientBuilder.class);
        HttpClientBuilder.create();
        verify(mockedHttpClient).setCloseableHttpClient(mockedApacheHttpClient);
        verify(mockedHttpClientBuilder).build();
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

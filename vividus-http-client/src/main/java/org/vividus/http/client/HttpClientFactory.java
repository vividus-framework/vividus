/*
 * Copyright 2019-2024 the original author or authors.
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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.Validate.isTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
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
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.vividus.http.keystore.IKeyStoreFactory;
import org.vividus.util.UriUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class HttpClientFactory implements IHttpClientFactory
{
    private static final AuthScope ANY_AUTH_SCOPE = new AuthScope(null, -1);
    private static final String ANY_ORIGIN = "*";

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
                .setTlsSocketStrategy(createTlsSocketStrategy(config.getSslConfig()))
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

    private TlsSocketStrategy createTlsSocketStrategy(SslConfig sslConfig) throws GeneralSecurityException
    {
        SSLContext sslContext = createSslContext(sslConfig.isSslCertificateCheckEnabled());
        HostnameVerifier hostnameVerifier =
                sslConfig.isSslHostnameVerificationEnabled() ? HttpsSupport.getDefaultHostnameVerifier()
                        : NoopHostnameVerifier.INSTANCE;

        return new DefaultClientTlsStrategy(sslContext, hostnameVerifier);
    }

    private SSLContext createSslContext(boolean sslCertificateCheckEnabled) throws GeneralSecurityException
    {
        if (!sslCertificateCheckEnabled)
        {
            return sslContextFactory.getTrustingAllSslContext();
        }
        Optional<KeyStore> keyStore = keyStoreFactory.getKeyStore();
        if (keyStore.isPresent())
        {
            return sslContextFactory.getSslContext(keyStore.get(), privateKeyPassword);
        }
        return SSLContexts.createSystemDefault();
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    private void configureAuth(HttpClientConfig config, HttpClientBuilder builder)
    {
        Map<String, BasicAuthConfig> auths = filterDefaultConfig(config.getBasicAuthConfig());
        if (auths.isEmpty())
        {
            return;
        }

        validateAuthConfiguration(auths);

        Map<String, Credentials> preemptiveAuthConfigs = new HashMap<>();
        Map<AuthScope, Credentials> scopeToCredentials = new HashMap<>();

        auths.forEach((key, data) ->
        {
            Credentials credentials = new UsernamePasswordCredentials(data.getUsername(),
                    data.getPassword().toCharArray());
            if (data.isPreemptiveAuthEnabled())
            {
                preemptiveAuthConfigs.put(data.getOrigin(), credentials);
                return;
            }

            String origin = data.getOrigin();
            AuthScope scope = isAnyOrigin(origin) ? ANY_AUTH_SCOPE : new AuthScope(asHost(origin));
            scopeToCredentials.put(scope, credentials);
        });

        if (!scopeToCredentials.isEmpty())
        {
            CredentialsStore credentialsStore = new BasicCredentialsProvider();
            scopeToCredentials.forEach(credentialsStore::setCredentials);
            builder.setDefaultCredentialsProvider(credentialsStore);
        }

        if (!preemptiveAuthConfigs.isEmpty())
        {
            builder.addRequestInterceptorFirst((req, entity, ctx) ->
            {
                HttpHost requestOrigin = HttpHost.create(getRequestUri(req));

                Entry<String, Credentials> authConfig = preemptiveAuthConfigs.entrySet().stream()
                        .filter(bac -> !ANY_ORIGIN.equals(bac.getKey()))
                        .filter(bac -> requestOrigin.equals(asHost(bac.getKey())))
                        .findFirst()
                        .orElseGet(() -> preemptiveAuthConfigs.entrySet().stream()
                                .filter(bac -> isAnyOrigin(bac.getKey())).findFirst().orElse(null));

                if (authConfig != null)
                {
                    BasicScheme scheme = new BasicScheme();
                    scheme.initPreemptive(authConfig.getValue());
                    String authResponse = scheme.generateAuthResponse(null, req, ctx);
                    req.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, authResponse));
                }
            });
        }
    }

    @Deprecated(forRemoval = true, since = "0.8.0")
    private Map<String, BasicAuthConfig> filterDefaultConfig(Map<String, BasicAuthConfig> configs)
    {
        return configs.entrySet().stream().filter(e ->
        {
            BasicAuthConfig c = e.getValue();
            return !"003e952c9a".equals(e.getKey()) && isNotEmpty(c.getUsername()) || isNotEmpty(c.getPassword())
                    || !ANY_ORIGIN.equals(c.getOrigin());
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void validateAuthConfiguration(Map<String, BasicAuthConfig> auths)
    {
        String errorFormat = "The '%s' parameter is missing for '%s' authentication configuration";
        auths.forEach((key, data) ->
        {
            isTrue(isNotEmpty(data.getUsername()), errorFormat.formatted("username", key));
            isTrue(isNotEmpty(data.getPassword()), errorFormat.formatted("password", key));
            isTrue(isNotEmpty(data.getOrigin()), errorFormat.formatted("origin", key));
        });

        auths.entrySet().stream()
                        .collect(groupingBy(e ->
                        {
                            String origin = e.getValue().getOrigin();
                            return isAnyOrigin(origin) ? ANY_ORIGIN : asHost(origin).toHostString();
                        }, mapping(Entry::getKey, toList())))
                        .forEach((host, keys) -> isTrue(keys.size() == 1,
                            "Found conflicting origin URLs in %s configurations", String.join(", ", keys)));
    }

    private boolean isAnyOrigin(String origin)
    {
        return ANY_ORIGIN.equals(origin);
    }

    private HttpHost asHost(String endpoint)
    {
        return HttpHost.create(UriUtils.createUri(endpoint));
    }

    private static URI getRequestUri(HttpRequest request)
    {
        try
        {
            return request.getUri();
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void setPrivateKeyPassword(String privateKeyPassword)
    {
        this.privateKeyPassword = privateKeyPassword;
    }
}

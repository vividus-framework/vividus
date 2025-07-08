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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.RoutingSupport;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIAuthority;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

public class HttpClient implements IHttpClient, AutoCloseable
{
    private static final boolean USE_PREEMPTIVE_BASIC_AUTH_IF_AVAILABLE = false;

    private CloseableHttpClient closeableHttpClient;
    private HttpHost httpHost;
    private boolean skipResponseEntity;
    private List<HttpResponseHandler> httpResponseHandlers;

    @Override
    public HttpHost getHttpHost()
    {
        return httpHost;
    }

    @Override
    public HttpResponse doHttpGet(URI uri) throws IOException
    {
        return doHttpGet(uri, USE_PREEMPTIVE_BASIC_AUTH_IF_AVAILABLE);
    }

    @Override
    public HttpResponse doHttpGet(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException
    {
        return execute(new HttpGet(uri), null, usePreemptiveBasicAuthIfAvailable);
    }

    @Override
    public HttpResponse doHttpHead(URI uri) throws IOException
    {
        return doHttpHead(uri, USE_PREEMPTIVE_BASIC_AUTH_IF_AVAILABLE);
    }

    @Override
    public HttpResponse doHttpHead(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException
    {
        return execute(new HttpHead(uri), null, usePreemptiveBasicAuthIfAvailable);
    }

    @Override
    public HttpResponse execute(ClassicHttpRequest request) throws IOException
    {
        return execute(request, null);
    }

    @Override
    public HttpResponse execute(ClassicHttpRequest request, HttpClientContext context) throws IOException
    {
        return execute(request, context, USE_PREEMPTIVE_BASIC_AUTH_IF_AVAILABLE);
    }

    private HttpResponse execute(ClassicHttpRequest request, HttpClientContext context,
            boolean usePreemptiveBasicAuthIfAvailable) throws IOException
    {
        URI uri = getRequestUri(request);

        HttpClientContext internalContext = Optional.ofNullable(context).orElseGet(HttpClientContext::create);

        Optional.ofNullable(request.getAuthority()).map(URIAuthority::getUserInfo).ifPresent(userInfo ->
        {
            HttpHost host = RoutingSupport.normalize(HttpHost.create(uri), DefaultSchemePortResolver.INSTANCE);
            configureBasicAuth(usePreemptiveBasicAuthIfAvailable, userInfo, host, internalContext);
            request.setAuthority(new URIAuthority(host));
        });

        HttpClientResponseHandler<HttpResponse> responseHandler = response -> {
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setMethod(request.getMethod());
            httpResponse.setFrom(uri);
            HttpEntity entity = response.getEntity();
            if (entity != null)
            {
                if (!skipResponseEntity)
                {
                    httpResponse.setResponseBody(EntityUtils.toByteArray(entity));
                }
                else
                {
                    EntityUtils.consume(entity);
                }
            }
            httpResponse.setResponseHeaders(response.getHeaders());
            httpResponse.setStatusCode(response.getCode());
            return httpResponse;
        };

        StopWatch watch = new StopWatch();
        watch.start();
        HttpResponse httpResponse = httpHost == null ? closeableHttpClient.execute(request, internalContext,
                responseHandler) : closeableHttpClient.execute(httpHost, request, internalContext, responseHandler);
        watch.stop();
        httpResponse.setResponseTimeInMs(watch.getDuration().toMillis());
        httpResponse.setRedirectLocations(internalContext.getRedirectLocations());

        for (HttpResponseHandler handler : httpResponseHandlers)
        {
            handler.handle(httpResponse);
        }

        return httpResponse;
    }

    private static URI getRequestUri(ClassicHttpRequest request) throws IOException
    {
        try
        {
            return request.getUri();
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
    }

    private static void configureBasicAuth(boolean usePreemptiveBasicAuthIfAvailable, String plainUserInfo,
            HttpHost host, HttpClientContext internalContext)
    {
        String plainUserInfoDecoded = URLDecoder.decode(plainUserInfo, StandardCharsets.UTF_8);
        UserInfo userInfo = UriUtils.parseUserInfo(plainUserInfoDecoded);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo.user(),
                userInfo.password().toCharArray());
        HttpHost normalizedHost = RoutingSupport.normalize(host, DefaultSchemePortResolver.INSTANCE);
        if (usePreemptiveBasicAuthIfAvailable)
        {
            BasicScheme authScheme = new BasicScheme();
            authScheme.initPreemptive(credentials);
            internalContext.resetAuthExchange(normalizedHost, authScheme);
        }
        else
        {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(normalizedHost), credentials);
            internalContext.setCredentialsProvider(credentialsProvider);
        }
    }

    public void setCloseableHttpClient(CloseableHttpClient closeableHttpClient)
    {
        this.closeableHttpClient = closeableHttpClient;
    }

    public void setHttpHost(HttpHost httpHost)
    {
        this.httpHost = httpHost;
    }

    public void setSkipResponseEntity(boolean skipResponseEntity)
    {
        this.skipResponseEntity = skipResponseEntity;
    }

    public void setHttpResponseHandlers(List<HttpResponseHandler> httpResponseHandlers)
    {
        this.httpResponseHandlers = httpResponseHandlers;
    }

    @Override
    public void close() throws IOException
    {
        closeableHttpClient.close();
    }
}

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
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.util.UriUtils;
import org.vividus.util.UriUtils.UserInfo;

public class HttpClient implements IHttpClient, AutoCloseable
{
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
        return doHttpGet(uri, false);
    }

    @Override
    public HttpResponse doHttpGet(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException
    {
        return executeWithUserInfoHandling(uri, usePreemptiveBasicAuthIfAvailable, HttpGet::new);
    }

    @Override
    public HttpResponse doHttpHead(URI uri) throws IOException
    {
        return doHttpHead(uri, false);
    }

    @Override
    public HttpResponse doHttpHead(URI uri, boolean usePreemptiveBasicAuthIfAvailable) throws IOException
    {
        return executeWithUserInfoHandling(uri, usePreemptiveBasicAuthIfAvailable, HttpHead::new);
    }

    private HttpResponse executeWithUserInfoHandling(URI uri, boolean usePreemptiveBasicAuthIfAvailable,
            Function<URI, ClassicHttpRequest> requestFactory) throws IOException
    {
        UserInfo userInfo = UriUtils.getUserInfo(uri);
        if (userInfo != null)
        {
            ContextBuilder contextBuilder = ContextBuilder.create();

            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userInfo.user(),
                    userInfo.password().toCharArray());
            HttpHost host = HttpHost.create(uri);
            if (usePreemptiveBasicAuthIfAvailable)
            {
                contextBuilder = contextBuilder.preemptiveBasicAuth(host, credentials);
            }
            else
            {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(host), credentials);
                contextBuilder.useCredentialsProvider(credentialsProvider);
            }
            URI targetUri = UriUtils.removeUserInfo(uri);
            HttpClientContext httpClientContext = contextBuilder.build();
            return execute(requestFactory.apply(targetUri), httpClientContext);
        }
        return execute(requestFactory.apply(uri));
    }

    @Override
    public HttpResponse execute(ClassicHttpRequest request) throws IOException
    {
        return execute(request, null);
    }

    @Override
    public HttpResponse execute(ClassicHttpRequest request, HttpClientContext context) throws IOException
    {
        HttpClientResponseHandler<HttpResponse> responseHandler = response -> {
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setMethod(request.getMethod());
            try
            {
                httpResponse.setFrom(request.getUri());
            }
            catch (URISyntaxException e)
            {
                throw new IOException(e);
            }
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

        HttpClientContext internalContext = Optional.ofNullable(context).orElseGet(HttpClientContext::create);

        StopWatch watch = new StopWatch();
        watch.start();
        HttpResponse httpResponse = httpHost == null ? closeableHttpClient.execute(request, internalContext,
                responseHandler) : closeableHttpClient.execute(httpHost, request, internalContext, responseHandler);
        watch.stop();
        httpResponse.setResponseTimeInMs(watch.getTime());
        httpResponse.setRedirectLocations(internalContext.getRedirectLocations());

        for (HttpResponseHandler handler : httpResponseHandlers)
        {
            handler.handle(httpResponse);
        }

        return httpResponse;
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

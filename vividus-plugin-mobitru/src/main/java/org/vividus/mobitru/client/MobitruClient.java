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

package org.vividus.mobitru.client;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.net.URIBuilder;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.mobitru.client.exception.MobitruDeviceSearchException;
import org.vividus.mobitru.client.exception.MobitruDeviceTakeException;
import org.vividus.mobitru.client.exception.MobitruOperationException;

public class MobitruClient
{
    private static final String DEVICE_PATH = "/device";

    private final IHttpClient httpClient;

    private final String apiBasePath;

    private String apiUrl;

    public MobitruClient(IHttpClient httpClient, String billingUnit)
    {
        this.httpClient = httpClient;
        this.apiBasePath = String.format("/billing/unit/%s/automation/api", billingUnit);
    }

    public byte[] findDevices(String platform, Map<String, String> parameters) throws MobitruOperationException
    {
        URIBuilder uriBuilder = new URIBuilder().setPath(DEVICE_PATH).appendPath(platform);
        parameters.forEach(uriBuilder::addParameter);
        return executeRequest(uriBuilder.toString(), HttpMethod.GET, UnaryOperator.identity(), HttpStatus.SC_OK,
                MobitruDeviceSearchException::new);
    }

    public byte[] takeDevice(String requestedDevice) throws MobitruOperationException
    {
        return executePost(DEVICE_PATH, requestedDevice, HttpStatus.SC_OK);
    }

    public byte[] getArtifacts() throws MobitruOperationException
    {
        return executeGet("/v1/spaces/artifacts", HttpStatus.SC_OK);
    }

    public void returnDevice(String deviceId) throws MobitruOperationException
    {
        executeRequest(DEVICE_PATH + "/" + deviceId, HttpMethod.DELETE, UnaryOperator.identity(), HttpStatus.SC_OK);
    }

    public void installApp(String deviceId, String appId) throws MobitruOperationException
    {
        executeGet(String.format("/storage/install/%s/%s", deviceId, appId), HttpStatus.SC_CREATED);
    }

    private byte[] executeGet(String relativeUrl, int expectedResponseCode) throws MobitruOperationException
    {
        return executeRequest(relativeUrl, HttpMethod.GET, UnaryOperator.identity(), expectedResponseCode);
    }

    private byte[] executePost(String relativeUrl, String payload, int expectedResponseCode)
        throws MobitruOperationException
    {
        return executeRequest(relativeUrl, HttpMethod.POST,
                rb -> rb.withContent(payload, ContentType.APPLICATION_JSON), expectedResponseCode,
            MobitruDeviceTakeException::new);
    }

    private byte[] executeRequest(String relativeUrl, HttpMethod httpMethod,
            UnaryOperator<HttpRequestBuilder> configurator, int expectedResponseCode) throws MobitruOperationException
    {
        return executeRequest(relativeUrl, httpMethod, configurator, expectedResponseCode,
                MobitruOperationException::new);
    }

    private byte[] executeRequest(String relativeUrl, HttpMethod httpMethod,
        UnaryOperator<HttpRequestBuilder> configurator, int expectedResponseCode,
        Function<String, MobitruOperationException> exceptionFactory) throws MobitruOperationException
    {
        try
        {
            ClassicHttpRequest request = configurator.apply(HttpRequestBuilder.create()
                            .withEndpoint(apiUrl)
                            .withHttpMethod(httpMethod)
                            .withRelativeUrl(apiBasePath + relativeUrl))
                    .build();
            HttpResponse httpResponse = httpClient.execute(request);
            verifyStatusCode(expectedResponseCode, httpResponse, exceptionFactory);
            return httpResponse.getResponseBody();
        }
        catch (IOException e)
        {
            throw new MobitruOperationException(e);
        }
    }

    private static void verifyStatusCode(int expectedResponseCode, HttpResponse httpResponse,
        Function<String, MobitruOperationException> exceptionFactory)
            throws MobitruOperationException
    {
        int responseCode = httpResponse.getStatusCode();
        if (responseCode != expectedResponseCode)
        {
            String errorMessage = String.format("Expected status code `%s` but got `%s`.", expectedResponseCode,
                    responseCode);
            String responseBody = httpResponse.getResponseBodyAsString();
            if (null != responseBody)
            {
                errorMessage = errorMessage + " Response body: " + responseBody;
            }
            throw exceptionFactory.apply(errorMessage);
        }
    }

    public void setApiUrl(String apiUrl)
    {
        this.apiUrl = apiUrl;
    }
}

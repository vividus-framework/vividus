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

package org.vividus.mobitru.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestBuilder;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.mobitru.client.exception.MobitruDeviceTakeException;
import org.vividus.mobitru.client.exception.MobitruOperationException;

@ExtendWith(MockitoExtension.class)
class MobitruClientTests
{
    private static final String VIVIDUS = "vividus";
    private static final byte[] RESPONSE = {1, 0, 1};
    private static final String ENDPOINT = "https://app.mobitry.com";
    private static final String CONTENT = "content";
    private static final String DEVICE_ENDPOINT = "/billing/unit/vividus/automation/api/device/deviceid";
    private static final String DEVICE_ID = "deviceid";
    private static final String TAKE_DEVICE_ENDPOINT = "/billing/unit/vividus/automation/api/device";
    private static final String UDID = "Z3CT103D2DZ";
    private static final String DEVICE_RECORDING_ENDPOINT = TAKE_DEVICE_ENDPOINT + "/" + UDID + "/recording";

    @Mock private IHttpClient httpClient;
    @Mock private HttpResponse httpResponse;

    private MobitruClient mobitruClient;

    @BeforeEach
    void beforeEach()
    {
        mobitruClient = new MobitruClient(httpClient, VIVIDUS, null);
        mobitruClient.setApiUrl(ENDPOINT);
    }

    @Test
    void shouldFindDevicesBySearchParameters() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.GET)).thenReturn(builder);
            when(builder.withRelativeUrl(TAKE_DEVICE_ENDPOINT + "/ios?type=phone&version=15")).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            var searchParameters = new LinkedHashMap<String, String>();
            searchParameters.put("type", "phone");
            searchParameters.put("version", "15");
            assertArrayEquals(RESPONSE, mobitruClient.findDevices("ios", searchParameters));
        }
    }

    @Test
    void shouldTakeDevice() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withContent(CONTENT, ContentType.APPLICATION_JSON)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.POST)).thenReturn(builder);
            when(builder.withRelativeUrl(TAKE_DEVICE_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            assertArrayEquals(RESPONSE, mobitruClient.takeDevice(CONTENT));
        }
    }

    @Test
    void shouldStartRecording() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.POST)).thenReturn(builder);
            when(builder.withRelativeUrl(DEVICE_RECORDING_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
            mobitruClient.startDeviceScreenRecording(UDID);
            verify(httpResponse).getResponseBody();
            verifyNoMoreInteractions(httpClient, httpResponse);
        }
    }

    @Test
    void shouldStopRecording() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.DELETE)).thenReturn(builder);
            when(builder.withRelativeUrl(DEVICE_RECORDING_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            assertArrayEquals(RESPONSE, mobitruClient.stopDeviceScreenRecording(UDID));
        }
    }

    @Test
    void shouldDownloadRecording() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.GET)).thenReturn(builder);
            var recordingId = "5eefb29c-78a4-4f39-ac52-bd54a22c5243";
            when(builder.withRelativeUrl("/billing/unit/vividus/automation/api/recording/" + recordingId)).thenReturn(
                    builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            assertArrayEquals(RESPONSE, mobitruClient.downloadDeviceScreenRecording(recordingId));
        }
    }

    @Test
    void shouldThrowDeviceExceptionIfInvalidStatusCodeReturned() throws IOException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withContent(CONTENT, ContentType.APPLICATION_JSON)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.POST)).thenReturn(builder);
            when(builder.withRelativeUrl(TAKE_DEVICE_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
            var mdse = assertThrows(MobitruDeviceTakeException.class, () -> mobitruClient.takeDevice(CONTENT));
            assertEquals("Unable to take device with configuration content. Expected status code `200` but got `404`.",
                    mdse.getMessage());
        }
    }

    @Test
    void shouldWrapIOException() throws IOException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withContent(CONTENT, ContentType.APPLICATION_JSON)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.POST)).thenReturn(builder);
            when(builder.withRelativeUrl(TAKE_DEVICE_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            var exception = new IOException();
            when(httpClient.execute(httpRequest)).thenThrow(exception);
            var moe = assertThrows(MobitruOperationException.class, () -> mobitruClient.takeDevice(CONTENT));
            assertEquals(exception, moe.getCause());
        }
    }

    @Test
    void shouldGetApps() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.GET)).thenReturn(builder);
            when(builder.withRelativeUrl("/billing/unit/vividus/automation/api/v1/spaces/artifacts"))
                .thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            assertArrayEquals(RESPONSE, mobitruClient.getArtifacts());
        }
    }

    @ParameterizedTest
    @CsvSource({
            "true, true, noResign=false&doInjection=true",
            "true, false, noResign=false&doInjection=false",
            "false, true, noResign=true&doInjection=true",
            "false, false, noResign=true&doInjection=false"
    })
    void shouldInstallApp(boolean resign, boolean injection, String urlQuery)
            throws IOException, MobitruOperationException
    {
        var installApplicationOptions = new InstallApplicationOptions(resign, injection);
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.GET)).thenReturn(builder);
            when(builder.withRelativeUrl(
                    "/billing/unit/vividus/automation/api/storage/install/udid/fileid?" + urlQuery)).thenReturn(
                    builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_CREATED);
            mobitruClient.installApp("udid", "fileid", installApplicationOptions);
            verify(httpClient).execute(httpRequest);
        }
    }

    @Test
    void shouldStopUsingDevice() throws IOException, MobitruOperationException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.DELETE)).thenReturn(builder);
            when(builder.withRelativeUrl(DEVICE_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            mobitruClient.returnDevice(DEVICE_ID);
            verify(httpClient).execute(httpRequest);
        }
    }

    @CsvSource({
            ",Expected status code `200` but got `404`.",
            "{},Expected status code `200` but got `404`. Response body: {}"
    })
    @ParameterizedTest
    void shouldThrowAnExceptionInCaseOfUnexpectedStatusCode(String responseBody, String expectedMessage)
            throws IOException
    {
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.DELETE)).thenReturn(builder);
            when(builder.withRelativeUrl(DEVICE_ENDPOINT)).thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
            when(httpResponse.getResponseBodyAsString()).thenReturn(responseBody);
            var moe = assertThrows(
                MobitruOperationException.class, () -> mobitruClient.returnDevice(DEVICE_ID));
            assertEquals(expectedMessage, moe.getMessage());
        }
    }

    @Test
    void shouldGetAppsFromWorkspace() throws IOException, MobitruOperationException
    {
        mobitruClient = new MobitruClient(httpClient, VIVIDUS, "vividus-dedicated-space");
        mobitruClient.setApiUrl(ENDPOINT);
        try (var builderMock = mockStatic(HttpRequestBuilder.class))
        {
            HttpRequestBuilder builder = mock();
            builderMock.when(HttpRequestBuilder::create).thenReturn(builder);
            when(builder.withEndpoint(ENDPOINT)).thenReturn(builder);
            when(builder.withHttpMethod(HttpMethod.GET)).thenReturn(builder);
            when(builder.withRelativeUrl(
                    "/billing/unit/vividus/workspace/vividus-dedicated-space/automation/api/v1/spaces/artifacts"))
                .thenReturn(builder);
            ClassicHttpRequest httpRequest = mock();
            when(builder.build()).thenReturn(httpRequest);
            when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
            when(httpResponse.getResponseBody()).thenReturn(RESPONSE);
            when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
            assertArrayEquals(RESPONSE, mobitruClient.getArtifacts());
        }
    }
}

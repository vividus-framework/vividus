/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.functions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class ResponseCapturingHttpPipelinePolicyTests
{
    private static final String BODY = "body";
    private static final int SC_OK = 200;
    private static final String URL = "https://azure.com/function";
    @Mock private HttpPipelineCallContext context;
    @Mock private HttpPipelineNextPolicy next;

    private ResponseCapturingHttpPipelinePolicy underTest = new ResponseCapturingHttpPipelinePolicy("function");

    @SuppressWarnings("unchecked")
    @Test
    void shouldCaptureResponseData() throws MalformedURLException
    {
        HttpResponse response = mock(HttpResponse.class);
        Mono<HttpResponse> mono = mock(Mono.class);
        Mono<String> body = Mono.just(BODY);
        when(response.getBodyAsString()).thenReturn(body);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.getHeaders()).thenReturn(headers);
        Map<String, String> headersMap = Map.of("header", "value");
        when(headers.toMap()).thenReturn(headersMap);
        HttpRequest request = mock(HttpRequest.class);
        URL url = URI.create(URL).toURL();
        when(request.getUrl()).thenReturn(url).thenReturn(URI.create("https://google.com").toURL());
        when(response.getRequest()).thenReturn(request);
        when(response.getStatusCode()).thenReturn(SC_OK);
        when(mono.doOnSuccess(argThat(c -> {
            c.accept(response);
            return true;
        }))).thenReturn(mono);
        when(next.process()).thenReturn(mono);

        underTest.process(context, next);
        underTest.process(context, next);

        Map<String, Object> captured = underTest.getResponses();
        Assertions.assertAll(
            () -> assertEquals(headersMap, captured.get("headers")),
            () -> assertEquals(body, captured.get(BODY)),
            () -> assertEquals(SC_OK, captured.get("status-code")),
            () -> assertEquals(URL, captured.get("url")));
    }
}

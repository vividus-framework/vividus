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

package org.vividus.ssllabs;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.wait.DurationBasedWaiter;

public class SslLabsClient
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SslLabsClient.class);
    private static final String SSL_SCAN_FAILURE =
            "SSL scan has not been performed successfully during specified waiting period";
    private static final String ERROR_MESSAGE = "Status message '{}' received for host {}";
    private static final String API_VERSION = "/api/v3";
    private static final String ANALYZE_CALL = "/analyze?host=%s&fromCache=on&maxAge=1";
    private static final int SERVICE_IS_OVERLOADED = 529;
    private static final DurationBasedWaiter WAITER = new DurationBasedWaiter(Duration.ofMinutes(10),
            Duration.ofSeconds(30));

    private final IHttpClient httpClient;
    private final JsonUtils jsonUtils;
    private final String sslLabHost;

    public SslLabsClient(IHttpClient httpClient, JsonUtils jsonUtils, String sslLabHost)
    {
        this.httpClient = httpClient;
        this.jsonUtils = jsonUtils;
        this.sslLabHost = sslLabHost;
    }

    public Optional<Grade> performSslScan(String host)
    {
        return waitReadyStatus(host).map(AnalyzeResponse::getEndpoints).stream()
                .flatMap(List::stream)
                .map(Endpoint::getGrade)
                .map(Grade::fromString)
                .min(Comparator.comparingInt(Grade::getGradeValue));
    }

    private Optional<AnalyzeResponse> waitReadyStatus(String host)
    {
        Optional<AnalyzeResponse> response = WAITER.wait(() -> analyze(host).map(r ->
        {
            if (Status.ERROR.name().equals(r.getStatus()))
            {
                LOGGER.atError().addArgument(r.getStatusMessage()).addArgument(r.getHost()).log(ERROR_MESSAGE);
                return null;
            }
            return r;
        }), r -> r.isEmpty() || r.get().getStatus().equals(Status.READY.name()));

        if (response.isPresent() && response.get().getStatus().equals(Status.READY.name()))
        {
            return response;
        }
        LOGGER.atError().log(SSL_SCAN_FAILURE);
        return Optional.empty();
    }

    private Optional<AnalyzeResponse> analyze(String host)
    {
        Optional<HttpResponse> sslLabsResponse = WAITER.wait(() ->
        {
            try
            {
                HttpResponse response = httpClient.doHttpGet(URI.create(
                        String.format("%s%s%s", sslLabHost, API_VERSION, String.format(ANALYZE_CALL, host))));
                return Optional.of(response);
            }
            catch (IOException e)
            {
                LOGGER.atError().setCause(e).log("Unable to process request");
                return Optional.empty();
            }
        }, response -> {
            if (response.isEmpty())
            {
                return true;
            }
            HttpResponse httpResponse = response.get();
            int statusCode = httpResponse.getStatusCode();
            if (statusCode == HttpStatus.SC_OK)
            {
                return true;
            }
            LOGGER.atWarn().addArgument(statusCode).addArgument(httpResponse.getResponseBodyAsString()).log(
                    "Unexpected status code received: {}\nResponse body: {}");
            if (statusCode == HttpStatus.SC_TOO_MANY_REQUESTS || statusCode == SERVICE_IS_OVERLOADED
                    || statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE || statusCode == HttpStatus.SC_BAD_GATEWAY)
            {
                return false;
            }
            LOGGER.atError().log(SSL_SCAN_FAILURE);
            return true;
        });
        return sslLabsResponse.filter(r -> r.getStatusCode() == HttpStatus.SC_OK)
                .map(r -> jsonUtils.toObject(r.getResponseBodyAsString(), AnalyzeResponse.class));
    }

    private enum Status
    {
        READY, ERROR
    }
}

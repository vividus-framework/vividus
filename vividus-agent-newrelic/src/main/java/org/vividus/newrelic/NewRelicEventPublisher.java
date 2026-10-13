/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.newrelic;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.EnumFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.newrelic.model.NewRelicEvent;
import org.vividus.util.json.JsonUtils;

public class NewRelicEventPublisher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NewRelicEventPublisher.class);

    private static final String API_KEY_HEADER = "Api-Key";

    private final IHttpClient httpClient;
    private final JsonUtils jsonUtils;
    private final String apiKey;
    private final String endpoint;

    public NewRelicEventPublisher(IHttpClient httpClient, String apiKey, String endpoint)
    {
        this.httpClient = httpClient;
        this.jsonUtils = createJsonUtils();
        this.apiKey = apiKey;
        this.endpoint = endpoint;
    }

    private static JsonUtils createJsonUtils()
    {
        ObjectMapper objectMapper = JsonMapper.builder().enable(EnumFeature.WRITE_ENUMS_TO_LOWERCASE).build();
        return new JsonUtils(objectMapper);
    }

    public void postEvent(NewRelicEvent event)
    {
        try
        {
            String body = jsonUtils.toJson(List.of(event));
            ClassicHttpRequest request = HttpMethod.POST.createEntityEnclosingRequest(URI.create(endpoint),
                    new StringEntity(body, ContentType.APPLICATION_JSON));
            request.setHeader(API_KEY_HEADER, apiKey);
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusCode();
            if (!isSuccessful(statusCode))
            {
                LOGGER.info("Unable to send New Relic event, response status code is {}", statusCode);
            }
        }
        catch (IOException | IllegalArgumentException e)
        {
            LOGGER.info("Unable to send New Relic event", e);
        }
    }

    private static boolean isSuccessful(int statusCode)
    {
        return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
    }
}

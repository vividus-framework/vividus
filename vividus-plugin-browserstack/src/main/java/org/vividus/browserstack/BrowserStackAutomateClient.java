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

package org.vividus.browserstack;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.browserstack.client.BrowserStackClient;
import com.browserstack.client.BrowserStackRequest;
import com.browserstack.client.exception.BrowserStackException;
import com.google.api.client.http.ByteArrayContent;

import org.apache.commons.lang3.StringUtils;
import org.vividus.util.UriUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

public class BrowserStackAutomateClient extends BrowserStackClient
{
    private static final int DEFAULT_RETRY = 6;
    private static final int DEFAULT_SECONDS = 90;

    private final Waiter waiter = new DurationBasedWaiter(
            new WaitMode(Duration.ofSeconds(DEFAULT_SECONDS), DEFAULT_RETRY));

    private final JsonUtils jsonUtils;

    public BrowserStackAutomateClient(String endpoint, String username, String accessKey, JsonUtils jsonUtils)
    {
        super(endpoint, username, accessKey);
        this.jsonUtils = jsonUtils;
    }

    public String getNetworkLogs(String sessionId) throws BrowserStackException
    {
        String baseUrl = getSession(sessionId).getBrowserUrl();
        URI networkLogsUri = UriUtils.createUri(baseUrl + "/networklogs");

        BrowserStackRequest request = newRequest(Method.GET, networkLogsUri.getPath());
        request.getHttpRequest().getHeaders().put("Accept", List.of("*/*"));

        return waiter.wait(() ->
        {
            try
            {
                return request.asString();
            }
            catch (BrowserStackException e)
            {
                if (e.getMessage().contains("404 Not Found"))
                {
                    return StringUtils.EMPTY;
                }
                throw e;
            }
        }, jsonUtils::isJson);
    }

    public void updateSessionStatus(String sessionId, String status) throws BrowserStackException
    {
        BrowserStackRequest request = newRequest(Method.PUT, "/sessions/{sessionId}.json").routeParam("sessionId",
                sessionId);
        ByteArrayContent content = ByteArrayContent.fromString("application/json",
                jsonUtils.toJson(Map.of("status", status)));
        request.body(content).asString();
    }
}

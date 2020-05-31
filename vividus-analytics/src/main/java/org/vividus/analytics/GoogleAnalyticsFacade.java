/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.analytics;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.http.HttpMethod;
import org.vividus.http.client.IHttpClient;

public class GoogleAnalyticsFacade
{
    private static final String CLIENT_ID = "cid";

    private static final String TRACKING_ID = "tid";

    private static final String HIT_TYPE = "t";

    private static final String MEASUREMENT_API_VERSION = "v";

    private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("(.+)(-\\d.+)?(\\\\|/)?");

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleAnalyticsFacade.class);

    private URI analyticsUri;
    private String trackingId;
    private List<NameValuePair> defaultParameters;
    private final IHttpClient httpClient;

    public GoogleAnalyticsFacade(IHttpClient httpClient)
    {
        this.httpClient = httpClient;
    }

    public void init()
    {
        defaultParameters = new ArrayList<>();
        defaultParameters.add(pairOf(MEASUREMENT_API_VERSION, "1"));
        defaultParameters.add(pairOf(HIT_TYPE, "event"));
        defaultParameters.add(pairOf(TRACKING_ID, trackingId));
        defaultParameters.add(pairOf(CLIENT_ID, cid()));
    }

    private static String cid()
    {
        String[] pathParts = System.getProperty("user.dir").split("\\\\|/");
        String projectName = pathParts[pathParts.length - 1];
        if ("scripts".equals(projectName))
        {
            projectName = PROJECT_NAME_PATTERN.matcher(pathParts[pathParts.length - 2]).replaceFirst("$1");
        }
        return UUID.nameUUIDFromBytes(DigestUtils.sha512Hex(projectName).getBytes(UTF_8)).toString();
    }

    public void postEvent(AnalyticsEvent analyticsEvent)
    {
        List<NameValuePair> params = new ArrayList<>();
        params.addAll(defaultParameters);
        params.addAll(convertToNameValuePairs(analyticsEvent));
        StringEntity entity = new StringEntity(URLEncodedUtils.format(params, UTF_8), UTF_8);
        post(entity);
    }

    private List<BasicNameValuePair> convertToNameValuePairs(AnalyticsEvent analyticsEvent)
    {
        return analyticsEvent.getPayload()
                             .entrySet()
                             .stream()
                             .map(e -> pairOf(e.getKey(), e.getValue()))
                             .collect(Collectors.toList());
    }

    private BasicNameValuePair pairOf(String key, String value)
    {
        return new BasicNameValuePair(key, value);
    }

    private void post(HttpEntity entity)
    {
        try
        {
            HttpEntityEnclosingRequestBase post = HttpMethod.POST
                    .createEntityEnclosingRequest(analyticsUri, entity);
            post.setHeader("User-Agent", "");
            httpClient.execute(post);
        }
        catch (IOException e)
        {
            LOGGER.info("Unable to send analytics", e);
        }
    }

    public void setAnalyticsUri(URI analyticsUri)
    {
        this.analyticsUri = analyticsUri;
    }

    public void setTrackingId(String trackingId)
    {
        this.trackingId = trackingId;
    }
}

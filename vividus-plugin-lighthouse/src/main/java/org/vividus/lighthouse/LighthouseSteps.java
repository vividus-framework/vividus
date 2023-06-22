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

package org.vividus.lighthouse;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.pagespeedonline.v5.PagespeedInsights;
import com.google.api.services.pagespeedonline.v5.model.LighthouseResultV5;

import org.jbehave.core.annotations.When;
import org.vividus.lighthouse.model.PerformanceMetricRule;
import org.vividus.lighthouse.model.ScanType;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;

public class LighthouseSteps
{
    private static final int DEFAULT_TIMEOUT = 0;

    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;
    private final PagespeedInsights pagespeedInsights;

    private final String apiKey;
    private final List<String> categories;

    public LighthouseSteps(String applicationName, String apiKey, List<String> categories,
            IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
            throws GeneralSecurityException, IOException
    {
        this.attachmentPublisher = attachmentPublisher;
        this.softAssert = softAssert;
        this.pagespeedInsights = new PagespeedInsights.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                req ->
                {
                    req.setConnectTimeout(DEFAULT_TIMEOUT);
                    req.setReadTimeout(DEFAULT_TIMEOUT);
                    req.setWriteTimeout(DEFAULT_TIMEOUT);
                }
            )
            .setApplicationName(applicationName)
            .build();
        this.apiKey = apiKey;
        this.categories = categories;
    }

    /**
     * Performs Lighthouse scan of the specified web page and validates performance metrics against expected thresholds.
     *
     * @param scanType           The scan type to use, either <b>full</b> or <b>desktop</b> or <b>mobile</b>.
     * @param webPageUrl         The web page URL to perform scan on.
     * @param metricsValidations The metrics validations.
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @SuppressWarnings("unchecked")
    @When("I perform Lighthouse $scanType scan of `$webPageUrl` page:$metricsValidations")
    public void performLighthouseScan(ScanType scanType, String webPageUrl,
            List<PerformanceMetricRule> metricsValidations) throws IOException
    {
        JsonFactory jsonFactory = pagespeedInsights.getJsonFactory();

        for (String strategy : scanType.getStrategies())
        {
            LighthouseResultV5 result = executePagespeed(webPageUrl, strategy);

            String resultAsString = jsonFactory.toString(result);

            attachmentPublisher.publishAttachment("/org/vividus/lighthouse/lighthouse.ftl",
                    Map.of("result", resultAsString),
                    String.format("Lighthouse %s scan report for page: %s", strategy, webPageUrl));

            List<ArrayMap<String, BigDecimal>> items = (List<ArrayMap<String, BigDecimal>>) result.getAudits()
                    .get("metrics").getDetails().get("items");
            ArrayMap<String, BigDecimal> metrics = items.get(0);

            attachmentPublisher.publishAttachment(jsonFactory.toPrettyString(metrics).getBytes(StandardCharsets.UTF_8),
                    strategy + "-metrics.json");

            softAssert.runIgnoringTestFailFast(() -> metricsValidations.forEach(validation ->
            {
                String metricKey = validation.getMetric();
                String processedMetricKey = metricKey.replaceAll("\\s*", "");

                metrics.keySet().stream()
                        .filter(m -> m.equalsIgnoreCase(processedMetricKey))
                        .findFirst()
                        .ifPresentOrElse(
                            m -> softAssert.assertThat(String.format("[%s] %s", strategy, metricKey),
                                metrics.get(m), validation.getRule().getComparisonRule(validation.getThreshold())),
                            () -> softAssert.recordFailedAssertion(
                                String.format("Unknown metric name '%s', available names are: %s", metricKey,
                                        String.join(", ", metrics.keySet()))));
            }));
        }
    }

    private LighthouseResultV5 executePagespeed(String url, String strategy) throws IOException
    {
        return pagespeedInsights.pagespeedapi()
                                .runpagespeed(url)
                                .setKey(apiKey)
                                .setStrategy(strategy)
                                .setCategory(categories)
                                .execute()
                                .getLighthouseResult();
    }
}

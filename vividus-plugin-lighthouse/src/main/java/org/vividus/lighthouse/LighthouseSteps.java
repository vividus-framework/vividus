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

import static org.apache.commons.lang3.Validate.isTrue;
import static org.vividus.lighthouse.model.PerformanceValidationStrategy.HIGHEST;
import static org.vividus.lighthouse.model.PerformanceValidationStrategy.NONE;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.pagespeedonline.v5.PagespeedInsights;
import com.google.api.services.pagespeedonline.v5.model.Categories;
import com.google.api.services.pagespeedonline.v5.model.LighthouseAuditResultV5;
import com.google.api.services.pagespeedonline.v5.model.LighthouseCategoryV5;
import com.google.api.services.pagespeedonline.v5.model.LighthouseResultV5;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.lighthouse.model.MetricRule;
import org.vividus.lighthouse.model.PerformanceValidationStrategy;
import org.vividus.lighthouse.model.ScanType;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.wait.MaxTimesBasedWaiter;

public class LighthouseSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LighthouseSteps.class);

    private static final Map<String, Function<Categories, LighthouseCategoryV5>> CUSTOM_METRIC_FATORIES = Map.of(
        "accessibilityScore", Categories::getAccessibility,
        "bestPracticesScore", Categories::getBestPractices,
        "performanceScore", Categories::getPerformance,
        "pwaScore", Categories::getPwa,
        "seoScore", Categories::getSeo
    );

    private static final int DEFAULT_TIMEOUT = 0;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int MAX_SCORE = 100;

    private static final int MAX_CACHED_MEASUREMENT_NUMBER = 5;
    private static final int MEASUREMENT_NUMBER = 3;
    private static final Duration MEASUREMENT_WAIT_DURATION = Duration.ofSeconds(25);
    private static final MaxTimesBasedWaiter MEASUREMENT_WAITER = new MaxTimesBasedWaiter(MEASUREMENT_WAIT_DURATION,
            MAX_CACHED_MEASUREMENT_NUMBER);

    private final IAttachmentPublisher attachmentPublisher;
    private final ISoftAssert softAssert;
    private final PagespeedInsights pagespeedInsights;
    private final JsonFactory jsonFactory;

    private final String apiKey;
    private final List<String> categories;
    private final int acceptableScorePercentageDelta;
    private final PerformanceValidationStrategy performanceValidationStrategy;

    public LighthouseSteps(String applicationName, String apiKey, List<String> categories,
            int acceptableScorePercentageDelta, PerformanceValidationStrategy performanceValidationStrategy,
            IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
            throws GeneralSecurityException, IOException
    {
        boolean hasPerformance = categories.contains("performance");

        isTrue(!(performanceValidationStrategy == null && hasPerformance),
                "Categories for validation contains 'performance', but performance validation strategy was not set."
                        + " Either remove 'performance' from validation categories, or set the strategy to 'NONE', "
                        + "'HIGHEST' or 'LOWEST'");
        isTrue(!(performanceValidationStrategy != null && performanceValidationStrategy != NONE && !hasPerformance),
                "Categories for validation doesn't contain 'performance', but performance validation strategy was set "
                        + "to %s. Either add 'performance' to validated categories, or set the strategy to 'NONE'",
                performanceValidationStrategy);
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
        this.jsonFactory = pagespeedInsights.getJsonFactory();
        this.apiKey = apiKey;
        this.categories = categories;
        this.acceptableScorePercentageDelta = acceptableScorePercentageDelta;
        this.performanceValidationStrategy = performanceValidationStrategy;
    }

    /**
     * Performs Lighthouse scan of the specified web page and validates performance metrics against expected thresholds.
     *
     * @param scanType           The scan type to use, either <b>full</b> or <b>desktop</b> or <b>mobile</b>.
     * @param webPageUrl         The web page URL to perform scan on.
     * @param metricsValidations The metrics validations.
     * @throws IOException if an I/O exception of some sort has occurred
     */
    @When("I perform Lighthouse $scanType scan of `$webPageUrl` page:$metricsValidations")
    public void performLighthouseScan(ScanType scanType, String webPageUrl,
            List<MetricRule> metricsValidations) throws IOException
    {
        for (String strategy : scanType.getStrategies())
        {
            LighthouseResultV5 result = executePagespeedTest(webPageUrl, strategy);

            String resultAsString = jsonFactory.toString(result);

            attachmentPublisher.publishAttachment("/org/vividus/lighthouse/lighthouse.ftl",
                    Map.of("result", resultAsString),
                    String.format("Lighthouse %s scan report for page: %s", strategy, webPageUrl));

            Map<String, BigDecimal> metrics = getMetrics(result);

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

    /**
     * Performs a Lighthouse scan on both the baseline and checkpoint pages validating that the audit scores of the
     * checkpoint page have not worsened compared to the audit scores of the baseline page.
     *
     * @param scanType       The scan type to use, either <b>full</b> or <b>desktop</b> or <b>mobile</b>.
     * @param checkpointPage The checkpoint page.
     * @param baselinePage   The baseline page.
     * @throws IOException if an I/O exception of some sort has occurred
     * @throws ExecutionException if the computation threw an exception
     * @throws InterruptedException in case of thread interruption
     */
    @SuppressWarnings("MagicNumber")
    @Then("Lighthouse $scanType audit scores for `$checkpointPage` page are not less than for `$baselinePage` page")
    public void performLighthouseScanWithComparison(ScanType scanType, String checkpointPage, String baselinePage)
            throws IOException, InterruptedException, ExecutionException
    {
        for (String strategy : scanType.getStrategies())
        {
            CompletableFuture<LighthouseResultV5> baselineFuture = schedulePagespeedTest(strategy, baselinePage);
            CompletableFuture<LighthouseResultV5> checkpointFuture = schedulePagespeedTest(strategy, checkpointPage);

            CompletableFuture.allOf(baselineFuture, checkpointFuture).join();

            LighthouseResultV5 baseline = baselineFuture.get();
            LighthouseResultV5 checkpoint = checkpointFuture.get();

            Map<String, Integer> checkpointScores = getCategoryScores(checkpoint);

            softAssert.runIgnoringTestFailFast(() -> getCategoryScores(baseline).forEach((categoryKey, baselineScore) ->
            {
                Integer checkpointScore = checkpointScores.get(categoryKey);

                if (checkpointScore >= baselineScore)
                {
                    softAssert.recordPassedAssertion(String.format(
                            "[%s] The %s audit is passed as checkpoint score (%s) is not less than baseline score (%s)",
                            strategy, categoryKey, checkpointScore, baselineScore));
                    return;
                }

                int scoreDecrease = (int) Math
                        .abs(((checkpointScore.doubleValue() - baselineScore) / baselineScore) * 100);

                if (scoreDecrease <= acceptableScorePercentageDelta)
                {
                    softAssert.recordPassedAssertion(String.format(
                            "[%s] The %s audit is passed as checkpoint score (%s) fits acceptable delta in %s percents"
                            + " from baseline score (%s)",
                            strategy, categoryKey, checkpointScore, acceptableScorePercentageDelta, baselineScore));
                    return;
                }

                softAssert.recordFailedAssertion(String.format("[%s] The %s audit score is degraded on %d percents",
                        strategy, categoryKey, scoreDecrease));
            }));

            // Its expected to use .ftl file format for attachments, but for the sake of viewer's build process
            // simplification in this place we use .html file
            attachmentPublisher.publishAttachment(
                    "/allure-customization/webjars/vividus-lighthouse-viewer-adaptation/index.html",
                    Map.of("baseline", jsonFactory.toString(baseline), "checkpoint", jsonFactory.toString(checkpoint)),
                    String.format("[%s] Lighthouse reports comparison", strategy));
        }
    }

    private Map<String, Integer> getCategoryScores(LighthouseResultV5 result)
    {
        return result.getCategories().entrySet().stream().collect(
                Collectors.toMap(Entry::getKey, e -> getScore((LighthouseCategoryV5) e.getValue()).intValue()));
    }

    private CompletableFuture<LighthouseResultV5> schedulePagespeedTest(String strategy, String page)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try
            {
                return executePagespeedTest(page, strategy);
            }
            catch (IOException thrown)
            {
                throw new CompletionException(thrown);
            }
        });
    }

    private BigDecimal getScore(LighthouseCategoryV5 category)
    {
        BigDecimal score = (BigDecimal) category.getScore();
        return score.movePointRight(2);
    }

    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> getMetrics(LighthouseResultV5 result)
    {
        Map<String, BigDecimal> metrics = new HashMap<>();

        LighthouseAuditResultV5 performanceMetrics = result.getAudits().get("metrics");
        if (performanceMetrics != null)
        {
            List<ArrayMap<String, BigDecimal>> items = (List<ArrayMap<String, BigDecimal>>) performanceMetrics
                    .getDetails().get("items");
            metrics.putAll(items.get(0));
        }

        Categories scanCategories = result.getCategories();
        CUSTOM_METRIC_FATORIES.forEach((m, f) ->
        {
            LighthouseCategoryV5 categoryValue = f.apply(scanCategories);
            if (categoryValue != null)
            {
                metrics.put(m, getScore(categoryValue));
            }
        });

        return metrics;
    }

    private LighthouseResultV5 executePagespeedTest(String url, String strategy)
            throws IOException
    {
        if (performanceValidationStrategy == null || performanceValidationStrategy == NONE)
        {
            return executePagespeed(url, strategy, true);
        }

        Deque<LighthouseResultV5> results = new LinkedList<>();

        for (int index = 0; index < MEASUREMENT_NUMBER; index++)
        {
            LighthouseResultV5 result = MEASUREMENT_WAITER.wait(() -> executePagespeed(url, strategy, true), current ->
            {
                LighthouseCategoryV5 currentPerformance = getPerformance(current);
                return results.isEmpty() || !currentPerformance.equals(getPerformance(results.getLast()));
            });

            isTrue(result != null, "Unable to get non-cached result after %s measurement retries",
                    MAX_CACHED_MEASUREMENT_NUMBER);

            results.add(result);

            int performanceScore = getPerformanceScore(result);

            LOGGER.atInfo().addArgument(index + 1)
                           .addArgument(performanceScore)
                           .log("The performance score of the measurement #{} is {}");

            // There is no point to continue measurements if we hit 100 performance because all the subsequent
            // request will be cached with long expiration time
            if (performanceScore == MAX_SCORE)
            {
                return result;
            }
        }

        BiPredicate<LighthouseResultV5, LighthouseResultV5> resultPredicate = performanceValidationStrategy == HIGHEST
                ? (l, r) -> getPerformanceScore(l) > getPerformanceScore(r)
                : (l, r) -> getPerformanceScore(l) < getPerformanceScore(r);

        return results.stream().reduce((l, r) -> resultPredicate.test(l, r) ? l : r).get();
    }

    private LighthouseCategoryV5 getPerformance(LighthouseResultV5 result)
    {
        return result.getCategories().getPerformance();
    }

    private int getPerformanceScore(LighthouseResultV5 result)
    {
        return getScore(getPerformance(result)).intValue();
    }

    private LighthouseResultV5 executePagespeed(String url, String strategy, boolean retryOnUnknownServerError)
            throws IOException
    {
        try
        {
            return pagespeedInsights.pagespeedapi()
                    .runpagespeed(url)
                    .setKey(apiKey)
                    .setStrategy(strategy)
                    .setCategory(categories)
                    .execute()
                    .getLighthouseResult();
        }
        catch (GoogleJsonResponseException thrown)
        {
            if (retryOnUnknownServerError && INTERNAL_SERVER_ERROR == thrown.getStatusCode()
                    && "Lighthouse returned error: Something went wrong.".equals(thrown.getDetails().get("message")))
            {
                return executePagespeed(url, strategy, false);
            }

            throw thrown;
        }
    }
}

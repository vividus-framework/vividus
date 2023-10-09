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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.ArrayMap;
import com.google.api.services.pagespeedonline.v5.PagespeedInsights;
import com.google.api.services.pagespeedonline.v5.PagespeedInsights.Pagespeedapi;
import com.google.api.services.pagespeedonline.v5.PagespeedInsights.Pagespeedapi.Runpagespeed;
import com.google.api.services.pagespeedonline.v5.model.Categories;
import com.google.api.services.pagespeedonline.v5.model.LighthouseAuditResultV5;
import com.google.api.services.pagespeedonline.v5.model.LighthouseCategoryV5;
import com.google.api.services.pagespeedonline.v5.model.LighthouseResultV5;
import com.google.api.services.pagespeedonline.v5.model.PagespeedApiPagespeedResponseV5;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.lighthouse.LighthouseSteps.PerformanceValidationConfiguration;
import org.vividus.lighthouse.model.MetricRule;
import org.vividus.lighthouse.model.ScanType;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.FailableRunnable;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;

@SuppressWarnings("MethodCount")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class LighthouseStepsTests
{
    private static final String APP_NAME = "app-name";
    private static final String API_KEY = "api-token";
    private static final String URL = "https://example.com";
    private static final String SI_METRIC = "speedIndex";
    private static final String FCP_METRIC = "firstContentfulPaint";
    private static final String INTERACTIVE_METRIC = "interactive";
    private static final String PERF_SCORE_METRIC = "performanceScore";
    private static final String SEO_SCORE_METRIC = "seoScore";
    private static final BigDecimal SCORE_METRIC_VAL = new BigDecimal(0.99f);
    private static final String RESULT_AS_STRING = "{}";
    private static final String SEO = "seo";
    private static final List<String> CATEGORIES = List.of("performance", "pwa", "best-practices", "accessibility",
            SEO);
    private static final String DESKTOP_STRATEGY = ScanType.DESKTOP.getStrategies()[0];
    private static final String UNKNOWN_ERROR_MESSAGE = "Lighthouse returned error: Something went wrong.";
    private static final String PERFORMANCE_SCORE_LOG = "The performance score of the measurement #{} is {}";
    private static final String GREATER_VALUE_FORMAT = "a value greater than <%s>";
    private static final String PERCENTILE_RANGE_ERROR = "The percentile value should be in range between 0 and 100"
            + " exclusively.";
    private static final int DEFAULT_MEASUREMENTS_NUMBER = 3;
    private static final int DEFAULT_PERCENTILE = 90;

    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @Mock private PagespeedInsights pagespeedInsights;
    @Mock private JsonFactory jsonFactory;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LighthouseSteps.class);

    @ParameterizedTest
    @EnumSource(value = ScanType.class, names = "FULL", mode = EnumSource.Mode.EXCLUDE)
    void shouldPerformLighthouseScan(ScanType scanType) throws Exception
    {
        performTest(() ->
        {
            mockRun();
            assertThat(scanType.getStrategies(), arrayWithSize(1));
            for (String key : scanType.getStrategies())
            {
                ArrayMap<String, BigDecimal> metrics = createMetrics();
                Pagespeedapi pagespeedapi = mockPagespeedapiCall(key, metrics);
                when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);

                LighthouseSteps steps = createSteps(CATEGORIES, 0, Optional.empty());

                MetricRule speedIndex = createSpeedIndexRule();
                MetricRule firstContentfulPaint = createRule(FCP_METRIC, 1500);
                MetricRule performanceScore = createRule(PERF_SCORE_METRIC, 100);
                MetricRule unknown = createRule("Unknown Metric", 100);
                steps.performLighthouseScan(scanType, URL,
                        List.of(speedIndex, firstContentfulPaint, performanceScore, unknown));

                verifyAttachments(key);
                validateMetric(key, speedIndex, metrics.get(SI_METRIC));
                validateMetric(key, firstContentfulPaint, metrics.get(FCP_METRIC));
                validateMetric(key, performanceScore, alignScore(SCORE_METRIC_VAL));
                ArgumentCaptor<String> failMesageCaptor = ArgumentCaptor.forClass(String.class);
                verify(softAssert).recordFailedAssertion(failMesageCaptor.capture());
                String message = failMesageCaptor.getValue();
                assertThat(message,
                        startsWith("Unknown metric name '" + unknown.getMetric() + "', available names are: "));
                assertThat(message, containsString(SI_METRIC));
                assertThat(message, containsString(FCP_METRIC));
                assertThat(message, containsString(INTERACTIVE_METRIC));
                assertThat(message, containsString(PERF_SCORE_METRIC));
            }
        });
    }

    @Test
    void shouldWrapIOExceptionIntoCompletionException() throws Exception
    {
        performTest(() ->
        {
            Pagespeedapi pagespeedapi = mock();
            IOException thrown = mock(IOException.class);
            doThrow(thrown).when(pagespeedapi).runpagespeed(any());
            when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);

            LighthouseSteps steps = createSteps(CATEGORIES, 5, Optional.empty());

            CompletionException completionThrown = assertThrows(CompletionException.class,
                    () -> steps.performLighthouseScanWithComparison(ScanType.DESKTOP, URL, URL));
            assertEquals(thrown, completionThrown.getCause());
        });
    }

    @Test
    void shouldFailIfCantGetUncachedResultParticularNumberOfTimes() throws Exception
    {
        performTest(() ->
        {
            PagespeedApiPagespeedResponseV5 response = mock();
            Pagespeedapi api = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, response);
            LighthouseResultV5 result = createLighthouseResult(0.50, 0.50, 0.50, 0.50);
            when(response.getLighthouseResult()).thenReturn(result);

            when(pagespeedInsights.pagespeedapi()).thenReturn(api);

            LighthouseSteps steps = createSteps(CATEGORIES, 0,
                    createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, DEFAULT_PERCENTILE));

            List<MetricRule> metricsValidations = List.of();
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                    () -> steps.performLighthouseScan(ScanType.DESKTOP, URL, metricsValidations));
            assertEquals("Unable to get non-cached result after 5 measurement retries", thrown.getMessage());
        });
    }

    @Test
    void shouldPerformLighthouseScanWithoutPerformanceCategory() throws Exception
    {
        performTest(() ->
        {
            mockRun();

            PagespeedApiPagespeedResponseV5 pagespeedApiPagespeedResponseV5 = mock();
            LighthouseResultV5 lighthouseResultV5 = mock();
            when(pagespeedApiPagespeedResponseV5.getLighthouseResult()).thenReturn(lighthouseResultV5);
            when(jsonFactory.toString(lighthouseResultV5)).thenReturn(RESULT_AS_STRING);
            when(lighthouseResultV5.getAudits()).thenReturn(Map.of());
            mockCategory(lighthouseResultV5, (c, cts) -> when(c.getSeo()).thenReturn(cts), SCORE_METRIC_VAL);
            when(jsonFactory.toPrettyString(any())).thenReturn(RESULT_AS_STRING);

            List<String> categories = List.of(SEO);
            Pagespeedapi pagespeedapi = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, categories,
                    pagespeedApiPagespeedResponseV5);

            when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);

            LighthouseSteps steps = createSteps(categories, 0, Optional.empty());

            MetricRule seoRule = createRule(SEO_SCORE_METRIC, 100);
            steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of(seoRule));

            verifyAttachments(DESKTOP_STRATEGY);
            validateMetric(DESKTOP_STRATEGY, seoRule, SCORE_METRIC_VAL.movePointRight(2));
        });
    }

    @Test
    void shouldPerformLighthouseFullScan() throws Exception
    {
        performTest(() ->
        {
            mockRun();
            String [] strategies = ScanType.FULL.getStrategies();
            assertThat(strategies, arrayWithSize(2));

            String desktopKey = strategies[0];
            ArrayMap<String, BigDecimal> desktopMetrics = createMetrics();
            Pagespeedapi desktopPagespeedapi = mockPagespeedapiCall(desktopKey, desktopMetrics);

            String mobileKey = strategies[1];
            ArrayMap<String, BigDecimal> mobileMetrics = createMetrics();
            Pagespeedapi mobilePagespeedapi = mockPagespeedapiCall(mobileKey, mobileMetrics);

            when(pagespeedInsights.pagespeedapi()).thenReturn(desktopPagespeedapi).thenReturn(mobilePagespeedapi);

            LighthouseSteps steps = createSteps(CATEGORIES, 0, Optional.empty());

            MetricRule speedIndex = createSpeedIndexRule();
            steps.performLighthouseScan(ScanType.FULL, URL, List.of(speedIndex));

            verifyAttachments(desktopKey);
            verifyAttachments(mobileKey);
            validateMetric(desktopKey, speedIndex, desktopMetrics.get(SI_METRIC));
            validateMetric(mobileKey, speedIndex, mobileMetrics.get(SI_METRIC));
        });
    }

    @SuppressWarnings("VariableDeclarationUsageDistance")
    @ParameterizedTest
    @CsvSource({
        "99, 0.9",
        "1, 0.1"
    })
    void shouldPerformLighthouseScanWithRetry(int percentile, double score) throws Exception
    {
        performTest(() ->
        {
            mockRun();

            PagespeedApiPagespeedResponseV5 firstResponse = mock();
            Pagespeedapi firstApi = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, firstResponse);
            LighthouseResultV5 firstResult = createLighthouseResult(0.50, 0.50, 0.50, 0.50);
            when(firstResponse.getLighthouseResult()).thenReturn(firstResult);

            PagespeedApiPagespeedResponseV5 firstResponseCached = mock();
            Pagespeedapi firstApiCached = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, firstResponseCached);
            LighthouseResultV5 firstResultCached = createLighthouseResult(0.50, 0.50, 0.50, 0.50);
            when(firstResponseCached.getLighthouseResult()).thenReturn(firstResultCached);

            PagespeedApiPagespeedResponseV5 secondResponse = mock();
            Pagespeedapi secondApi = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, secondResponse);
            LighthouseResultV5 secondResult = createLighthouseResult(score, score, score, score);
            when(secondResponse.getLighthouseResult()).thenReturn(secondResult);
            mockMetricItems(secondResult, new ArrayMap<>());
            when(jsonFactory.toString(secondResult)).thenReturn(RESULT_AS_STRING);
            when(jsonFactory.toPrettyString(any())).thenReturn(RESULT_AS_STRING);

            PagespeedApiPagespeedResponseV5 thirdResponse = mock();
            Pagespeedapi thirdApi = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, thirdResponse);
            LighthouseResultV5 thirdResult = createLighthouseResult(0.70, 0.70, 0.70, 0.70);
            when(thirdResponse.getLighthouseResult()).thenReturn(thirdResult);

            when(pagespeedInsights.pagespeedapi()).thenReturn(firstApi).thenReturn(firstApiCached).thenReturn(secondApi)
                    .thenReturn(thirdApi);

            LighthouseSteps steps = createSteps(CATEGORIES, 0,
                    createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, percentile));

            MetricRule perfScoreRule = createRule(PERF_SCORE_METRIC, 85, ComparisonRule.GREATER_THAN);
            steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of(perfScoreRule));

            BigDecimal performanceScore = (BigDecimal) secondResult.getCategories().getPerformance().getScore();
            validateMetric(DESKTOP_STRATEGY, perfScoreRule, alignScore(performanceScore), GREATER_VALUE_FORMAT);
            verifyNoMoreInteractions(pagespeedInsights);

            assertThat(logger.getLoggingEvents(), is(List.of(
                info(PERFORMANCE_SCORE_LOG, 1, getPerformanceScore(firstResult).intValue()),
                info(PERFORMANCE_SCORE_LOG, 2, getPerformanceScore(secondResult).intValue()),
                info(PERFORMANCE_SCORE_LOG, 3, getPerformanceScore(thirdResult).intValue())
            )));
        });
    }

    @ParameterizedTest
    @ValueSource(doubles = {
        1,
        9.9
    })
    void shouldPerformLighthouseScanWithOneRetryIfScoreIsOneHundredOrNinetyNine(double value) throws Exception
    {
        performTest(() ->
        {
            mockRun();

            PagespeedApiPagespeedResponseV5 response = mock();
            Pagespeedapi api = mockPagespeedapiCall(URL, DESKTOP_STRATEGY, response);
            LighthouseResultV5 result = createLighthouseResult(value, value, value, value);
            when(response.getLighthouseResult()).thenReturn(result);

            mockMetricItems(result, new ArrayMap<>());
            when(jsonFactory.toString(result)).thenReturn(RESULT_AS_STRING);
            when(jsonFactory.toPrettyString(any())).thenReturn(RESULT_AS_STRING);

            when(pagespeedInsights.pagespeedapi()).thenReturn(api);

            LighthouseSteps steps = createSteps(CATEGORIES, 0,
                    createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, DEFAULT_PERCENTILE));

            MetricRule perfScoreRule = createRule(PERF_SCORE_METRIC, 85, ComparisonRule.GREATER_THAN);
            steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of(perfScoreRule));

            validateMetric(DESKTOP_STRATEGY, perfScoreRule, getPerformanceScore(result), GREATER_VALUE_FORMAT);
            verifyNoMoreInteractions(pagespeedInsights);

            assertThat(logger.getLoggingEvents(),
                    is(List.of(info(PERFORMANCE_SCORE_LOG, 1, getPerformanceScore(result).intValue()))));
        });
    }

    private BigDecimal getPerformanceScore(LighthouseResultV5 result)
    {
        return alignScore((BigDecimal) result.getCategories().getPerformance().getScore());
    }

    private BigDecimal alignScore(BigDecimal score)
    {
        return score.movePointRight(2);
    }

    static Stream<Arguments> inputs()
    {
        return Stream.of(
                arguments(CATEGORIES, createConfiguration(null, DEFAULT_PERCENTILE),
                        "The measurements number value of performance validation configuration should be set."),
                arguments(CATEGORIES, createConfiguration(-1, DEFAULT_PERCENTILE),
                        "The measurements number value should be greater than 0."),
                arguments(CATEGORIES, createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, null),
                        "The percentile value of performance validation configuration should be set."),
                arguments(CATEGORIES, createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, -1), PERCENTILE_RANGE_ERROR),
                arguments(CATEGORIES, createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, 101), PERCENTILE_RANGE_ERROR),
                arguments(List.of(SEO), createConfiguration(DEFAULT_MEASUREMENTS_NUMBER, 1),
                        "Categories for validation doesn't contain 'performance', but performance validation "
                                + "configuration was set. Either add 'performance' to validated categories, or remove"
                                + " performance validation configuration.")
        );
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void shouldFailIfPerformanceValidationStrategyIsNotValid(List<String> categories,
            Optional<PerformanceValidationConfiguration> config, String message)
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> createSteps(categories, 0, config));
        assertEquals(message, thrown.getMessage());
    }

    static Stream<GoogleJsonResponseException> errors()
    {
        return Stream.of(
            createResponseError(400, "Lighthouse returned error: ERRORED_DOCUMENT_REQUEST."),
            createResponseError(500, "Lighthouse returned error: NO_FCP."),
            createResponseError(500, UNKNOWN_ERROR_MESSAGE)
        );
    }

    @ParameterizedTest
    @MethodSource("errors")
    void shouldNotPerformRetryOnErrors(GoogleJsonResponseException thrown) throws Exception
    {
        performTest(() ->
        {
            Pagespeedapi pagespeedapi = mock();
            when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);
            Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, URL, DESKTOP_STRATEGY, CATEGORIES);
            doThrow(thrown).when(runpagespeed).execute();

            LighthouseSteps steps = createSteps(CATEGORIES, 0, Optional.empty());

            GoogleJsonResponseException actual = assertThrows(GoogleJsonResponseException.class,
                    () -> steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of()));
            assertEquals(thrown, actual);
            verifyNoMoreInteractions(softAssert, attachmentPublisher);
        });
    }

    @Test
    void shouldRetryOnUnknownServerError() throws Exception
    {
        performTest(() ->
        {
            mockRun();
            ArrayMap<String, BigDecimal> metrics = createMetrics();
            Pagespeedapi pagespeedapi = mock();
            Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, URL, DESKTOP_STRATEGY, CATEGORIES);
            GoogleJsonResponseException thrown = createResponseError(500, UNKNOWN_ERROR_MESSAGE);
            PagespeedApiPagespeedResponseV5 response = mockResponse(metrics);
            doThrow(thrown).doReturn(response).when(runpagespeed).execute();
            when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);

            LighthouseSteps steps = createSteps(CATEGORIES, 0, Optional.empty());

            MetricRule speedIndex = createSpeedIndexRule();
            steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of(speedIndex));

            verifyAttachments(DESKTOP_STRATEGY);
            validateMetric(DESKTOP_STRATEGY, speedIndex, metrics.get(SI_METRIC));
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void shouldCompareAuditScoresBetweenTwoSites() throws Exception
    {
        performTest(() ->
        {
            mockRun();

            try (MockedStatic<CompletableFuture> cfs = mockStatic(CompletableFuture.class))
            {
                CompletableFuture<LighthouseResultV5> baselineFuture = mock();
                CompletableFuture<LighthouseResultV5> checkpointFuture = mock();

                ArgumentCaptor<Supplier<LighthouseResultV5>> captor = ArgumentCaptor.forClass(Supplier.class);
                cfs.when(() -> CompletableFuture.supplyAsync(captor.capture())).thenReturn(baselineFuture)
                        .thenReturn(checkpointFuture);

                CompletableFuture<Void> end = mock();
                cfs.when(() -> CompletableFuture.allOf(baselineFuture, checkpointFuture)).thenReturn(end);

                doAnswer(a ->
                {
                    Supplier<LighthouseResultV5> supplier = captor.getAllValues().get(0);
                    return supplier.get();
                }).when(baselineFuture).get();

                doAnswer(a ->
                {
                    Supplier<LighthouseResultV5> supplier = captor.getAllValues().get(1);
                    return supplier.get();
                }).when(checkpointFuture).get();

                String baselineUrl = "https://baseline.com/my-page";
                PagespeedApiPagespeedResponseV5 baselineResponse = mock();
                Pagespeedapi baselineApi = mockPagespeedapiCall(baselineUrl, DESKTOP_STRATEGY, baselineResponse);
                LighthouseResultV5 baselineResult = createLighthouseResult(0.90, 0.90, 0.90, 0.90);
                when(baselineResponse.getLighthouseResult()).thenReturn(baselineResult);

                String checkpointUrl = "https://checkpoint.com/my-page";
                PagespeedApiPagespeedResponseV5 checkpointResponse = mock();
                Pagespeedapi checkpointApi = mockPagespeedapiCall(checkpointUrl, DESKTOP_STRATEGY, checkpointResponse);
                LighthouseResultV5 checkpointResult = createLighthouseResult(0.90, 0.951, 0.20, 0.89);
                when(checkpointResponse.getLighthouseResult()).thenReturn(checkpointResult);

                when(pagespeedInsights.pagespeedapi()).thenReturn(baselineApi).thenReturn(checkpointApi);

                when(jsonFactory.toString(baselineResult)).thenReturn(RESULT_AS_STRING);
                when(jsonFactory.toString(checkpointResult)).thenReturn(RESULT_AS_STRING);

                LighthouseSteps steps = createSteps(CATEGORIES, 5, Optional.empty());

                steps.performLighthouseScanWithComparison(ScanType.DESKTOP, checkpointUrl, baselineUrl);

                verify(softAssert).recordPassedAssertion("[desktop] The performance audit is passed as checkpoint "
                        + "score (90) is not less than baseline score (90)");
                verify(softAssert).recordPassedAssertion("[desktop] The seo audit is passed as checkpoint score (95)"
                        + " is not less than baseline score (90)");
                verify(softAssert).recordPassedAssertion("[desktop] The best-practices audit is passed as checkpoint"
                        + " score (89) fits acceptable delta in 5 percents from baseline score (90)");
                verify(softAssert)
                        .recordFailedAssertion("[desktop] The accessibility audit score is degraded on 77 percents");
                verifyNoMoreInteractions(softAssert);

                verify(attachmentPublisher).publishAttachment(
                        "/allure-customization/webjars/vividus-lighthouse-viewer-adaptation/index.html",
                        Map.of("baseline", RESULT_AS_STRING, "checkpoint", RESULT_AS_STRING),
                        "[desktop] Lighthouse reports comparison");
            }
        });
    }

    private LighthouseResultV5 createLighthouseResult(double performance, double seo, double accessibility,
            double bestPractices)
    {
        LighthouseResultV5 results = mock();
        Categories categories = new Categories();
        when(results.getCategories()).thenReturn(categories);

        categories.setPerformance(createCategory(performance));
        categories.setSeo(createCategory(seo));
        categories.setAccessibility(createCategory(accessibility));
        categories.setBestPractices(createCategory(bestPractices));

        return results;
    }

    private static LighthouseCategoryV5 createCategory(double score)
    {
        LighthouseCategoryV5 category = new LighthouseCategoryV5();
        category.setScore(new BigDecimal(score));
        return category;
    }

    private static GoogleJsonResponseException createResponseError(int code, String message)
    {
        HttpResponseException.Builder builder = new HttpResponseException.Builder(code, "", new HttpHeaders());
        GoogleJsonError error = mock();
        lenient().when(error.get("message")).thenReturn(message);
        return new GoogleJsonResponseException(builder, error);
    }

    @SuppressWarnings("rawtypes")
    private void mockRun()
    {
        doAnswer(a -> {
            FailableRunnable runnable = a.getArgument(0);
            runnable.run();
            return null;
        }).when(softAssert).runIgnoringTestFailFast(any());
    }

    private void performTest(FailableRunnable<Exception> testRunner) throws Exception
    {
        when(pagespeedInsights.getJsonFactory()).thenReturn(jsonFactory);
        HttpRequest httpRequest = mock();
        try (MockedConstruction<PagespeedInsights.Builder> mockedConstruction = mockConstruction(
                PagespeedInsights.Builder.class, (mock, context) ->
                {
                    HttpRequestInitializer httpRequestInitializer = (HttpRequestInitializer) context.arguments().get(2);
                    httpRequestInitializer.initialize(httpRequest);
                    when(mock.setApplicationName(APP_NAME)).thenReturn(mock);
                    when(mock.build()).thenReturn(pagespeedInsights);
                }))
        {
            testRunner.run();

            verify(httpRequest).setConnectTimeout(0);
            verify(httpRequest).setReadTimeout(0);
            verify(httpRequest).setWriteTimeout(0);
        }
    }

    private void verifyAttachments(String key)
    {
        verify(attachmentPublisher).publishAttachment("/org/vividus/lighthouse/lighthouse.ftl",
                Map.of("result", RESULT_AS_STRING), String.format("Lighthouse %s scan report for page: %s", key, URL));
        verify(attachmentPublisher).publishAttachment(RESULT_AS_STRING.getBytes(StandardCharsets.UTF_8),
                key + "-metrics.json");
    }

    private static Optional<PerformanceValidationConfiguration> createConfiguration(Integer measurementsNumber,
            Integer percentile)
    {
        PerformanceValidationConfiguration configuration = new PerformanceValidationConfiguration();
        configuration.setMeasurementsNumber(measurementsNumber);
        configuration.setPercentile(percentile);
        return Optional.of(configuration);
    }

    private LighthouseSteps createSteps(List<String> categories, int delta,
            Optional<PerformanceValidationConfiguration> configuration) throws Exception
    {
        return new LighthouseSteps(APP_NAME, API_KEY, categories, delta, configuration, attachmentPublisher,
                softAssert);
    }

    private Pagespeedapi mockPagespeedapiCall(String strategy, ArrayMap<String, BigDecimal> metrics) throws IOException
    {
        PagespeedApiPagespeedResponseV5 pagespeedApiPagespeedResponseV5 = mockResponse(metrics);
        return mockPagespeedapiCall(URL, strategy, CATEGORIES, pagespeedApiPagespeedResponseV5);
    }

    private Pagespeedapi mockPagespeedapiCall(String url, String strategy, PagespeedApiPagespeedResponseV5 response)
            throws IOException
    {
        return mockPagespeedapiCall(url, strategy, CATEGORIES, response);
    }

    private Pagespeedapi mockPagespeedapiCall(String url, String strategy, List<String> categories,
            PagespeedApiPagespeedResponseV5 response) throws IOException
    {
        Pagespeedapi pagespeedapi = mock();
        Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, url, strategy, categories);
        when(runpagespeed.execute()).thenReturn(response);
        return pagespeedapi;
    }

    private PagespeedApiPagespeedResponseV5 mockResponse(ArrayMap<String, BigDecimal> metrics) throws IOException
    {
        PagespeedApiPagespeedResponseV5 pagespeedApiPagespeedResponseV5 = mock();
        LighthouseResultV5 lighthouseResultV5 = mock();
        when(pagespeedApiPagespeedResponseV5.getLighthouseResult()).thenReturn(lighthouseResultV5);
        when(jsonFactory.toString(lighthouseResultV5)).thenReturn(RESULT_AS_STRING);
        mockMetricItems(lighthouseResultV5, metrics);
        Categories categories = mock();
        when(lighthouseResultV5.getCategories()).thenReturn(categories);
        mockCategory(lighthouseResultV5, (c, cts) -> when(c.getPerformance()).thenReturn(cts), SCORE_METRIC_VAL);

        ArrayMap<String, BigDecimal> actualMetrics = ArrayMap.create();
        actualMetrics.putAll(metrics);
        actualMetrics.put(PERF_SCORE_METRIC, alignScore(SCORE_METRIC_VAL));
        when(jsonFactory.toPrettyString(actualMetrics)).thenReturn(RESULT_AS_STRING);
        return pagespeedApiPagespeedResponseV5;
    }

    private void mockCategory(LighthouseResultV5 lighthouseResultV5,
            BiConsumer<Categories, LighthouseCategoryV5> categoryMocker, BigDecimal value)
    {
        Categories categories = mock();
        when(lighthouseResultV5.getCategories()).thenReturn(categories);
        LighthouseCategoryV5 category = mock();
        categoryMocker.accept(categories, category);
        when(category.getScore()).thenReturn(SCORE_METRIC_VAL);
    }

    private void mockMetricItems(LighthouseResultV5 lighthouseResultV5, ArrayMap<String, BigDecimal> metrics)
            throws IOException
    {
        LighthouseAuditResultV5 lighthouseAuditResultV5 = mock();
        when(lighthouseResultV5.getAudits()).thenReturn(Map.of("metrics", lighthouseAuditResultV5));
        List<ArrayMap<String, BigDecimal>> items = List.of(metrics);
        when(lighthouseAuditResultV5.getDetails()).thenReturn(Map.of("items", items));
    }

    private Runpagespeed mockConfiguration(Pagespeedapi pagespeedapi, String url, String strategy,
            List<String> categories) throws IOException
    {
        Runpagespeed runpagespeed = mock();
        when(pagespeedapi.runpagespeed(url)).thenReturn(runpagespeed);
        when(runpagespeed.setKey(API_KEY)).thenReturn(runpagespeed);
        when(runpagespeed.setStrategy(strategy)).thenReturn(runpagespeed);
        when(runpagespeed.setCategory(categories)).thenReturn(runpagespeed);
        return runpagespeed;
    }

    private ArrayMap<String, BigDecimal> createMetrics()
    {
        ArrayMap<String, BigDecimal> metrics = new ArrayMap<>();
        metrics.put(SI_METRIC, new BigDecimal(3792));
        metrics.put(FCP_METRIC, new BigDecimal(1082));
        metrics.put(INTERACTIVE_METRIC, new BigDecimal(3969));
        return metrics;
    }

    private void validateMetric(String key, MetricRule rule, BigDecimal actual)
    {
        validateMetric(key, rule, actual, "a value less than <%s>");
    }

    private void validateMetric(String key, MetricRule rule, BigDecimal actual, String matcherFormat)
    {
        verify(softAssert).assertThat(eq(String.format("[%s] %s", key, rule.getMetric())), eq(actual),
                argThat(arg -> String.format(matcherFormat, rule.getThreshold().intValue()).equals(arg.toString())));
    }

    private MetricRule createSpeedIndexRule()
    {
        return createRule("Speed Index", 3800);
    }

    private MetricRule createRule(String name, int value)
    {
        return createRule(name, value, ComparisonRule.LESS_THAN);
    }

    private MetricRule createRule(String name, int value, ComparisonRule comparisonRule)
    {
        MetricRule rule = new MetricRule();
        rule.setMetric(name);
        rule.setRule(comparisonRule);
        rule.setThreshold(new BigDecimal(value));
        return rule;
    }
}

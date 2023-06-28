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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.lighthouse.model.MetricRule;
import org.vividus.lighthouse.model.ScanType;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.FailableRunnable;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;

@ExtendWith(MockitoExtension.class)
class LighthouseStepsTests
{
    private static final String APP_NAME = "app-name";
    private static final String API_KEY = "api-token";
    private static final String URL = "https://example.com";
    private static final String SI_METRIC = "speedIndex";
    private static final String FCP_METRIC = "firstContentfulPaint";
    private static final String INTERACTIVE_METRIC = "interactive";
    private static final String PERF_SCORE_METRIC = "performanceScore";
    private static final BigDecimal PERF_SCORE_METRIC_VAL = new BigDecimal(0.99f);
    private static final String RESULT_AS_STRING = "{}";
    private static final List<String> CATEGORIES = List.of("performance", "pwa", "best-practices", "accessibility",
            "seo");
    private static final String DESKTOP_STRATEGY = ScanType.DESKTOP.getStrategies()[0];
    private static final String UNKNOWN_ERROR_MESSAGE = "Lighthouse returned error: Something went wrong.";

    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @Mock private PagespeedInsights pagespeedInsights;
    @Mock private JsonFactory jsonFactory;

    @BeforeEach
    void init() throws IOException
    {
        when(pagespeedInsights.getJsonFactory()).thenReturn(jsonFactory);
    }

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

                LighthouseSteps steps = new LighthouseSteps(APP_NAME, API_KEY, CATEGORIES, attachmentPublisher,
                        softAssert);

                MetricRule speedIndex = createSpeedIndexRule();
                MetricRule firstContentfulPaint = createRule(FCP_METRIC, 1500);
                MetricRule performanceScore = createRule(PERF_SCORE_METRIC, 100);
                MetricRule unknown = createRule("Unknown Metric", 100);
                steps.performLighthouseScan(scanType, URL,
                        List.of(speedIndex, firstContentfulPaint, performanceScore, unknown));

                verifyAttachments(key);
                validateMetric(key, speedIndex, metrics.get(SI_METRIC));
                validateMetric(key, firstContentfulPaint, metrics.get(FCP_METRIC));
                validateMetric(key, performanceScore, PERF_SCORE_METRIC_VAL.movePointRight(2));
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

            LighthouseSteps steps = new LighthouseSteps(APP_NAME, API_KEY, CATEGORIES, attachmentPublisher, softAssert);

            MetricRule speedIndex = createSpeedIndexRule();
            steps.performLighthouseScan(ScanType.FULL, URL, List.of(speedIndex));

            verifyAttachments(desktopKey);
            verifyAttachments(mobileKey);
            validateMetric(desktopKey, speedIndex, desktopMetrics.get(SI_METRIC));
            validateMetric(mobileKey, speedIndex, mobileMetrics.get(SI_METRIC));
        });
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
            Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, DESKTOP_STRATEGY);
            doThrow(thrown).when(runpagespeed).execute();

            LighthouseSteps steps = new LighthouseSteps(APP_NAME, API_KEY, CATEGORIES, attachmentPublisher, softAssert);

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
            Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, DESKTOP_STRATEGY);
            GoogleJsonResponseException thrown = createResponseError(500, UNKNOWN_ERROR_MESSAGE);
            PagespeedApiPagespeedResponseV5 response = mockResponse(metrics);
            doThrow(thrown).doReturn(response).when(runpagespeed).execute();
            when(pagespeedInsights.pagespeedapi()).thenReturn(pagespeedapi);

            LighthouseSteps steps = new LighthouseSteps(APP_NAME, API_KEY, CATEGORIES, attachmentPublisher, softAssert);

            MetricRule speedIndex = createSpeedIndexRule();
            steps.performLighthouseScan(ScanType.DESKTOP, URL, List.of(speedIndex));

            verifyAttachments(DESKTOP_STRATEGY);
            validateMetric(DESKTOP_STRATEGY, speedIndex, metrics.get(SI_METRIC));
        });
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

    private Pagespeedapi mockPagespeedapiCall(String strategy, ArrayMap<String, BigDecimal> metrics) throws IOException
    {
        Pagespeedapi pagespeedapi = mock();
        Runpagespeed runpagespeed = mockConfiguration(pagespeedapi, strategy);
        PagespeedApiPagespeedResponseV5 pagespeedApiPagespeedResponseV5 = mockResponse(metrics);
        when(runpagespeed.execute()).thenReturn(pagespeedApiPagespeedResponseV5);
        return pagespeedapi;
    }

    private PagespeedApiPagespeedResponseV5 mockResponse(ArrayMap<String, BigDecimal> metrics) throws IOException
    {
        PagespeedApiPagespeedResponseV5 pagespeedApiPagespeedResponseV5 = mock();
        LighthouseResultV5 lighthouseResultV5 = mock();
        when(pagespeedApiPagespeedResponseV5.getLighthouseResult()).thenReturn(lighthouseResultV5);
        when(jsonFactory.toString(lighthouseResultV5)).thenReturn(RESULT_AS_STRING);
        LighthouseAuditResultV5 lighthouseAuditResultV5 = mock();
        when(lighthouseResultV5.getAudits()).thenReturn(Map.of("metrics", lighthouseAuditResultV5));
        List<ArrayMap<String, BigDecimal>> items = List.of(metrics);
        when(lighthouseAuditResultV5.getDetails()).thenReturn(Map.of("items", items));
        Categories categories = mock();
        when(lighthouseResultV5.getCategories()).thenReturn(categories);
        LighthouseCategoryV5 category = mock();
        when(categories.getPerformance()).thenReturn(category);
        when(category.getScore()).thenReturn(PERF_SCORE_METRIC_VAL);

        ArrayMap<String, BigDecimal> actualMetrics = ArrayMap.create();
        actualMetrics.putAll(metrics);
        actualMetrics.put(PERF_SCORE_METRIC, PERF_SCORE_METRIC_VAL.movePointRight(2));
        when(jsonFactory.toPrettyString(actualMetrics)).thenReturn(RESULT_AS_STRING);
        return pagespeedApiPagespeedResponseV5;
    }

    private Runpagespeed mockConfiguration(Pagespeedapi pagespeedapi, String strategy) throws IOException
    {
        Runpagespeed runpagespeed = mock();
        when(pagespeedapi.runpagespeed(URL)).thenReturn(runpagespeed);
        when(runpagespeed.setKey(API_KEY)).thenReturn(runpagespeed);
        when(runpagespeed.setStrategy(strategy)).thenReturn(runpagespeed);
        when(runpagespeed.setCategory(CATEGORIES)).thenReturn(runpagespeed);
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
        verify(softAssert).assertThat(eq(String.format("[%s] %s", key, rule.getMetric())), eq(actual), argThat(
                arg -> String.format("a value less than <%s>", rule.getThreshold().intValue()).equals(arg.toString())));
    }

    private MetricRule createSpeedIndexRule()
    {
        return createRule("Speed Index", 3800);
    }

    private MetricRule createRule(String name, int value)
    {
        MetricRule rule = new MetricRule();
        rule.setMetric(name);
        rule.setRule(ComparisonRule.LESS_THAN);
        rule.setThreshold(new BigDecimal(value));
        return rule;
    }
}

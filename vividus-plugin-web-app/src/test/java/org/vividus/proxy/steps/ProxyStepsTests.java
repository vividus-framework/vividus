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

package org.vividus.proxy.steps;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.util.HttpMessageInfo;
import com.browserup.harreader.model.Har;
import com.browserup.harreader.model.HarCreatorBrowser;
import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarLog;
import com.browserup.harreader.model.HarPostData;
import com.browserup.harreader.model.HarPostDataParam;
import com.browserup.harreader.model.HarQueryParam;
import com.browserup.harreader.model.HarRequest;
import com.browserup.harreader.model.HarResponse;
import com.browserup.harreader.model.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.proxy.IProxy;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IWebWaitActions;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

@ExtendWith(MockitoExtension.class)
class ProxyStepsTests
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String URL = "www.test.com";
    private static final String REQUESTS_MATCHING_URL_ASSERTION_PATTERN =
            "Number of HTTP %s requests matching URL pattern '%s'";
    private static final String VARIABLE_NAME = "variable";
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String MIME_TYPE = "mimeType";
    private static final String TEXT = "text";
    private static final String COMMENT = "comment";
    private static final ExamplesTable HEADERS = new ExamplesTable("|name|value|\n|name1|value1|");
    private static final Pattern URL_PATTERN = Pattern.compile(URL);

    @Mock
    private ISoftAssert nonFailingAssert;

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private IProxy proxy;

    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @Mock
    private IWebWaitActions waitActions;

    @InjectMocks
    private ProxySteps proxySteps;

    @Test
    void testClearProxyLog()
    {
        proxySteps.clearProxyLog();
        verify(proxy).clearRecordedData();
    }

    @Test
    void checkHarEntryExistenceWithHttpMethodAndUrlPattern() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        mockHar(httpMethod, HttpStatus.SC_OK);
        long callsNumber = 1;
        ComparisonRule rule = ComparisonRule.EQUAL_TO;
        String message = String.format(REQUESTS_MATCHING_URL_ASSERTION_PATTERN, "GET, POST", URL);
        mockSizeAssertion(message, callsNumber, rule, callsNumber);
        proxySteps.checkNumberOfRequests(EnumSet.of(httpMethod, HttpMethod.GET), URL_PATTERN, rule, callsNumber);
        verifySizeAssertion(message, callsNumber, rule, callsNumber);
        verifyNoInteractions(attachmentPublisher);
    }

    @ParameterizedTest
    @CsvSource({
            "GET,  200",
            "POST, 302"
    })
    void checkHarEntryExistenceWithHttpMethodAndUrlPatternNoCalls(HttpMethod httpMethodInHar, int statusCode)
            throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        byte[] data = mockHar(httpMethodInHar, statusCode);
        String message = String.format(REQUESTS_MATCHING_URL_ASSERTION_PATTERN, httpMethod, URL);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verifySizeAssertion(message, 0, ComparisonRule.EQUAL_TO, 1);
        verify(attachmentPublisher).publishAttachment(data, "har.har");
        verifyNoInteractions(bddVariableContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureQueryStringFromHarEntry() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        mockHar(httpMethod, HttpStatus.SC_OK);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveURLQuery(EnumSet.of(httpMethod), URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value -> {
            Map<String, String> map = (Map<String, String>) value;
            return map.size() == 2 && map.containsKey(KEY1) && VALUE1.equals(map.get(KEY1)) && map.containsKey(KEY2)
                    && VALUE2.equals(map.get(KEY2));
        }));
    }

    @Test
    void shouldSaveUrlFromCapturedHar() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        mockHar(httpMethod, HttpStatus.SC_OK);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(variableScopes, VARIABLE_NAME, URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureRequestDataFromHarEntry() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        int statusCode = HttpStatus.SC_OK;
        mockHar(httpMethod, statusCode);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveRequestData(EnumSet.of(httpMethod), URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value -> {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, String> urlQuery = (Map<String, String>) map.get("query");
            Map<String, String> requestBody = (Map<String, String>) map.get("requestBody");
            Map<String, String> requestBodyParameters = (Map<String, String>) map.get("requestBodyParameters");
            Integer responseStatus = (Integer) map.get("responseStatus");
            assertAll(
                    () -> assertEquals(VALUE1, urlQuery.get(KEY1)),
                    () -> assertEquals(VALUE2, urlQuery.get(KEY2)),
                    () -> assertEquals(MIME_TYPE, requestBody.get(MIME_TYPE)),
                    () -> assertEquals(COMMENT, requestBody.get(COMMENT)),
                    () -> assertEquals(VALUE1, requestBodyParameters.get(KEY1)),
                    () -> assertEquals(VALUE2, requestBodyParameters.get(KEY2)),
                    () -> assertEquals(statusCode, responseStatus)
            );
            return true;
        }));
    }

    @Test
    void checkCaptureQueryStringSeveralHarEntriesFound() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        ProxySteps spy = spy(proxySteps);
        Mockito.lenient().doReturn(List.of(mock(HarEntry.class), mock(HarEntry.class))).when(spy).checkNumberOfRequests(
                EnumSet.of(httpMethod), URL_PATTERN, ComparisonRule.EQUAL_TO, 1);
        verifyNoInteractions(bddVariableContext);
    }

    @ParameterizedTest
    @CsvSource({
            "POST, true",
            "PUT, false"
    })
    void testWaitRequestInProxyLog(HttpMethod actualHttpMethod, boolean waitSuccessful) throws IOException
    {
        mockHar(actualHttpMethod, HttpStatus.SC_OK);
        proxySteps.waitRequestInProxyLog(EnumSet.of(HttpMethod.POST), URL_PATTERN);
        verify(waitActions).wait(eq(URL_PATTERN), argThat((Function<Pattern, Boolean> e) ->
                "waiting for HTTP POST request with URL pattern www.test.com".equals(e.toString())
                        && e.apply(URL_PATTERN) == waitSuccessful));
    }

    @Test
    void testWaitAnyOfRequestInProxyLog() throws IOException
    {
        mockHar(HttpMethod.PUT, HttpStatus.SC_OK);
        proxySteps.waitRequestInProxyLog(EnumSet.of(HttpMethod.POST, HttpMethod.PUT), URL_PATTERN);
        verify(waitActions).wait(eq(URL_PATTERN), argThat((Function<Pattern, Boolean> e) ->
                "waiting for HTTP POST or PUT request with URL pattern www.test.com".equals(e.toString())
                        && e.apply(URL_PATTERN)));
    }

    @Test
    void shouldAddHeadersToProxyRequestIfUrlMatches()
    {
        HttpHeaders httpHeaders = mock(HttpHeaders.class);
        HttpRequest request = mock(HttpRequest.class);
        when(request.headers()).thenReturn(httpHeaders);
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(URL);
        proxySteps.addHeadersToProxyRequest(StringComparisonRule.IS_EQUAL_TO, URL, HEADERS);
        ArgumentCaptor<RequestFilter> filterCaptor = ArgumentCaptor.forClass(RequestFilter.class);
        verify(proxy).addRequestFilter(filterCaptor.capture());
        assertNull(filterCaptor.getValue().filterRequest(request, null, messageInfo));
        verify(httpHeaders).add(
                argThat(headers -> headers instanceof DefaultHttpHeaders && headers.size() == 1 && VALUE1
                        .equals(headers.get("name1"))));
    }

    @Test
    void shouldNotAddHeadersToProxyRequestIfUrlDoesNotMatch()
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxySteps.addHeadersToProxyRequest(StringComparisonRule.IS_EQUAL_TO, URL, HEADERS);
        verify(proxy).addRequestFilter(argThat(filter -> filter.filterRequest(null, null, messageInfo) == null));
    }

    private void verifySizeAssertion(String message, long actualMatchedEntriesNumber, ComparisonRule rule,
            long callsNumber)
    {
        verify(nonFailingAssert).assertThat(eq(message), eq(actualMatchedEntriesNumber),
                argThat(object -> object != null && object.toString()
                        .equals(rule.getComparisonRule(callsNumber).toString())));
    }

    private void mockSizeAssertion(String message, long actualMatchedEntriesNumber, ComparisonRule rule,
            long callsNumber)
    {
        when(nonFailingAssert.assertThat(eq(message), eq(actualMatchedEntriesNumber),
                argThat(object -> object != null && object.toString()
                        .equals(rule.getComparisonRule(callsNumber).toString())))).thenReturn(true);
    }

    private byte[] mockHar(HttpMethod httpMethod, int statusCode) throws IOException
    {
        HarEntry harEntry = createHarEntry(httpMethod, statusCode);

        HarCreatorBrowser browser = new HarCreatorBrowser();
        browser.setName("chrome");
        browser.setVersion("66");

        HarLog harLog = new HarLog();
        harLog.setBrowser(browser);
        harLog.setCreator(browser);
        harLog.setEntries(List.of(harEntry));

        Har har = new Har();
        har.setLog(harLog);
        when(proxy.getRecordedData()).thenReturn(har);
        return OBJECT_MAPPER.writeValueAsBytes(har);
    }

    private HarEntry createHarEntry(HttpMethod httpMethod, int statusCode)
    {
        HarPostData postData = new HarPostData();
        postData.setMimeType(MIME_TYPE);
        postData.setText(TEXT);
        postData.setComment(COMMENT);
        postData.setParams(List.of(createHarPostDataParam(KEY1, VALUE1), createHarPostDataParam(KEY2, VALUE2)));

        HarRequest request = new HarRequest();
        request.setMethod(httpMethod);
        request.setUrl(URL);
        request.setQueryString(List.of(createHarQueryParam(KEY1, VALUE1), createHarQueryParam(KEY2, VALUE2)));
        request.setPostData(postData);

        HarResponse response = new HarResponse();
        response.setStatus(statusCode);

        HarEntry harEntry = new HarEntry();
        harEntry.setRequest(request);
        harEntry.setResponse(response);
        return harEntry;
    }

    private HarQueryParam createHarQueryParam(String key, String value)
    {
        HarQueryParam harQueryParam = new HarQueryParam();
        harQueryParam.setName(key);
        harQueryParam.setValue(value);
        return harQueryParam;
    }

    private HarPostDataParam createHarPostDataParam(String key, String value)
    {
        HarPostDataParam postDataParam = new HarPostDataParam();
        postDataParam.setName(key);
        postDataParam.setValue(value);
        return postDataParam;
    }
}

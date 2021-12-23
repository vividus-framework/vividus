/*
 * Copyright 2019-2022 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.model.HttpMessagePart;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.DataWrapper;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.action.IWaitActions;
import org.vividus.variable.VariableScope;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@ExtendWith(MockitoExtension.class)
class ProxyStepsTests
{
    private static final String CONTENT_LENGTH = "Content-Length";
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
    private static final String CONTENT_LENGTH_VALUE = "6";
    private static final DefaultHttpHeaders HEADERS = new DefaultHttpHeaders();
    private static final Pattern URL_PATTERN = Pattern.compile(URL);

    @Mock private ISoftAssert nonFailingAssert;
    @Mock private VariableContext variableContext;
    @Mock private IProxy proxy;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private IWaitActions waitActions;
    @InjectMocks private ProxySteps proxySteps;

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
        int callsNumber = 1;
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
        mockHar(httpMethodInHar, statusCode);
        String message = String.format(REQUESTS_MATCHING_URL_ASSERTION_PATTERN, httpMethod, URL);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, HttpMessagePart.URL,
                Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verifySizeAssertion(message, 0, ComparisonRule.EQUAL_TO, 1);
        verifyNoInteractions(variableContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureQueryStringFromHarEntry() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        mockHar(httpMethod, HttpStatus.SC_OK);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, HttpMessagePart.URL_QUERY,
                variableScopes, VARIABLE_NAME);
        verify(variableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value ->
            value.equals(Map.of(
                    KEY1, List.of(VALUE1, VALUE2),
                    KEY2, List.of(VALUE2)
            ))
        ));
    }

    @Test
    void shouldSaveUrlFromCapturedHar() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        mockHar(httpMethod, HttpStatus.SC_OK);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, HttpMessagePart.URL, variableScopes,
                VARIABLE_NAME);
        verify(variableContext).putVariable(variableScopes, VARIABLE_NAME, URL);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureRequestDataFromHarEntry() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        int statusCode = HttpStatus.SC_OK;
        mockHar(httpMethod, statusCode);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        proxySteps.captureRequestAndSaveURL(EnumSet.of(httpMethod), URL_PATTERN, HttpMessagePart.REQUEST_DATA,
                variableScopes, VARIABLE_NAME);
        verify(variableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value -> {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, List<String>> urlQuery = (Map<String, List<String>>) map.get("query");
            Map<String, String> requestBody = (Map<String, String>) map.get("requestBody");
            Map<String, List<String>> formData = (Map<String, List<String>>) map.get("requestBodyParameters");
            Integer responseStatus = (Integer) map.get("responseStatus");
            assertAll(
                    () -> assertEquals(List.of(VALUE1, VALUE2), urlQuery.get(KEY1)),
                    () -> assertEquals(List.of(VALUE2), urlQuery.get(KEY2)),
                    () -> assertEquals(MIME_TYPE, requestBody.get(MIME_TYPE)),
                    () -> assertEquals(List.of(VALUE1, VALUE2), formData.get(KEY1)),
                    () -> assertEquals(List.of(VALUE2), formData.get(KEY2)),
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
        verifyNoInteractions(variableContext);
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
        verify(httpHeaders).add(HEADERS);
    }

    @Test
    void shouldNotAddHeadersToProxyRequestIfUrlDoesNotMatch()
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        proxySteps.addHeadersToProxyRequest(StringComparisonRule.IS_EQUAL_TO, URL, HEADERS);
        verify(proxy).addRequestFilter(argThat(filter -> filter.filterRequest(null, null, messageInfo) == null));
    }

    static Stream<Arguments> contentSource()
    {
        return Stream.of(Arguments.of(VALUE1), Arguments.of(VALUE1.getBytes(StandardCharsets.UTF_8)));
    }

    @ParameterizedTest
    @MethodSource("contentSource")
    void shouldMockARequestWithTheContent(Object content)
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(URL);
        HttpRequest request = mock(HttpRequest.class);
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(KEY1, VALUE2);
        proxySteps.mockHttpRequests(StringComparisonRule.CONTAINS, URL, 200, new DataWrapper(content), headers);

        ArgumentCaptor<RequestFilter> filterCaptor = ArgumentCaptor.forClass(RequestFilter.class);
        verify(proxy).addRequestFilter(filterCaptor.capture());
        FullHttpResponse response = (FullHttpResponse) filterCaptor.getValue().filterRequest(request, null,
                messageInfo);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals(VALUE1, response.content().toString(StandardCharsets.UTF_8));
        assertEquals(VALUE2, response.headers().get(KEY1));
        assertEquals(CONTENT_LENGTH_VALUE, response.headers().get(CONTENT_LENGTH));
        assertEquals(HttpVersion.HTTP_1_1, response.protocolVersion());
    }

    @Test
    void shouldMockARequestWithTheContentByMethods()
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(URL);
        HttpRequest request = mock(HttpRequest.class);
        when(messageInfo.getOriginalRequest()).thenReturn(request);
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.POST);
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(KEY1, VALUE2);
        proxySteps.mockHttpRequests(Set.of(HttpMethod.GET, HttpMethod.POST), StringComparisonRule.CONTAINS,
                URL, 200, new DataWrapper(VALUE1), headers);
        verifyResponse(request, messageInfo);
    }

    @Test
    void shouldMockARequestWithTheContentByMethodsNotMatch()
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(URL);
        HttpRequest request = mock(HttpRequest.class);
        when(messageInfo.getOriginalRequest()).thenReturn(request);
        when(request.method()).thenReturn(io.netty.handler.codec.http.HttpMethod.OPTIONS);
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(KEY1, VALUE2);
        proxySteps.mockHttpRequests(Set.of(HttpMethod.GET, HttpMethod.POST), StringComparisonRule.CONTAINS,
                URL, 200, new DataWrapper(VALUE1), headers);
        ArgumentCaptor<RequestFilter> filterCaptor = ArgumentCaptor.forClass(RequestFilter.class);
        verify(proxy).addRequestFilter(filterCaptor.capture());
        FullHttpResponse response = (FullHttpResponse) filterCaptor.getValue().filterRequest(request, null,
                messageInfo);
        assertNull(response);
    }

    @Test
    void shouldMockARequestWithoutAContent()
    {
        HttpMessageInfo messageInfo = mock(HttpMessageInfo.class);
        when(messageInfo.getUrl()).thenReturn(URL);
        HttpRequest request = mock(HttpRequest.class);
        when(request.protocolVersion()).thenReturn(HttpVersion.HTTP_1_1);
        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(KEY1, VALUE2);
        proxySteps.mockHttpRequests(StringComparisonRule.CONTAINS, URL, 200, headers);

        ArgumentCaptor<RequestFilter> filterCaptor = ArgumentCaptor.forClass(RequestFilter.class);
        verify(proxy).addRequestFilter(filterCaptor.capture());
        HttpResponse response = filterCaptor.getValue().filterRequest(request, null, messageInfo);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals(VALUE2, response.headers().get(KEY1));
        assertNull(response.headers().get(CONTENT_LENGTH));
        assertEquals(HttpVersion.HTTP_1_1, response.protocolVersion());
    }

    @Test
    void shouldClearFilters()
    {
        proxySteps.resetMocks();
        verify(proxy).clearRequestFilters();
    }

    private void verifySizeAssertion(String message, int actualMatchedEntriesNumber, ComparisonRule rule,
            int callsNumber)
    {
        verify(nonFailingAssert).assertThat(eq(message), eq(actualMatchedEntriesNumber),
                argThat(object -> object != null && object.toString()
                        .equals(rule.getComparisonRule(callsNumber).toString())));
    }

    private void verifyResponse(HttpRequest request, HttpMessageInfo messageInfo)
    {
        ArgumentCaptor<RequestFilter> filterCaptor = ArgumentCaptor.forClass(RequestFilter.class);
        verify(proxy).addRequestFilter(filterCaptor.capture());
        FullHttpResponse response = (FullHttpResponse) filterCaptor.getValue().filterRequest(request, null,
                messageInfo);
        assertEquals(HttpResponseStatus.OK, response.status());
        assertEquals(VALUE1, response.content().toString(StandardCharsets.UTF_8));
        assertEquals(VALUE2, response.headers().get(KEY1));
        assertEquals(CONTENT_LENGTH_VALUE, response.headers().get(CONTENT_LENGTH));
        assertEquals(HttpVersion.HTTP_1_1, response.protocolVersion());
    }

    private void mockSizeAssertion(String message, int actualMatchedEntriesNumber, ComparisonRule rule, int callsNumber)
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
        postData.setParams(List.of(
                createHarPostDataParam(KEY1, VALUE1),
                createHarPostDataParam(KEY1, VALUE2),
                createHarPostDataParam(KEY2, VALUE2)
        ));

        HarRequest request = new HarRequest();
        request.setMethod(httpMethod);
        request.setUrl(URL);
        request.setQueryString(List.of(
                createHarQueryParam(KEY1, VALUE1),
                createHarQueryParam(KEY1, VALUE2),
                createHarQueryParam(KEY2, VALUE2)
        ));
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

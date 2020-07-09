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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.browserup.bup.BrowserUpProxy;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.ProxyLog;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IWaitActions;

@ExtendWith(MockitoExtension.class)
class ProxyStepsTests
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String URL_PATTERN = "www.test.com";
    private static final String REQUESTS_MATCHING_URL_ASSERTION_PATTERN = "Number of HTTP %s requests matching URL "
            + "pattern '%s'";
    private static final String ATTACHMENT_FILENAME = "har.har";
    private static final String VARIABLE_NAME = "variable";
    private static final HttpMethod HTTP_METHOD = HttpMethod.POST;
    private static final String KEY1 = "key1";
    private static final String VALUE1 = "value1";
    private static final String KEY2 = "key2";
    private static final String VALUE2 = "value2";
    private static final String MIME_TYPE = "mimeType";
    private static final String TEXT = "text";
    private static final String COMMENT = "comment";

    @Mock
    private ISoftAssert nonFailingAssert;

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private IProxy proxy;

    @Mock
    private ProxyLog proxyLog;

    @Mock
    private IAttachmentPublisher attachmentPublisher;

    @Mock
    private IWaitActions waitActions;

    @InjectMocks
    private ProxySteps proxySteps;

    @Test
    void testClearProxyLog()
    {
        when(proxy.getLog()).thenReturn(proxyLog);
        proxySteps.clearProxyLog();
        verify(proxyLog).clear();
    }

    @Test
    void checkHarEntryExistenceWithHttpMethodAndUrlPattern() throws IOException
    {
        when(proxy.getLog()).thenReturn(proxyLog);
        HttpMethod httpMethod = HttpMethod.POST;
        HarEntry harEntry = mock(HarEntry.class);
        HarResponse harResponse = mock(HarResponse.class);
        when(proxyLog.getLogEntries(httpMethod, URL_PATTERN)).thenReturn(List.of(harEntry));
        when(harEntry.getResponse()).thenReturn(harResponse);
        when(harResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        long callsNumber = 1;
        ComparisonRule rule = ComparisonRule.EQUAL_TO;
        String message = String.format(REQUESTS_MATCHING_URL_ASSERTION_PATTERN, httpMethod, URL_PATTERN);
        mockSizeAssertion(message, callsNumber, rule, callsNumber);
        proxySteps.checkNumberOfRequests(httpMethod, URL_PATTERN, rule, callsNumber);
        verifySizeAssertion(message, callsNumber, rule, callsNumber);
        verifyNoInteractions(attachmentPublisher);
    }

    @Test
    void checkHarEntryExistenceWithHttpMethodAndUrlPatternNoCalls() throws IOException
    {
        when(proxy.getLog()).thenReturn(proxyLog);
        HttpMethod httpMethod = HttpMethod.POST;
        when(proxyLog.getLogEntries(httpMethod, URL_PATTERN)).thenReturn(Collections.emptyList());
        long callsNumber = 1;
        ComparisonRule rule = ComparisonRule.EQUAL_TO;
        String message = String.format(REQUESTS_MATCHING_URL_ASSERTION_PATTERN, httpMethod,
                URL_PATTERN);
        byte[] data = mockProxyLog();
        proxySteps.checkNumberOfRequests(httpMethod, URL_PATTERN, rule, callsNumber);
        verifySizeAssertion(message, 0, rule, callsNumber);
        verify(attachmentPublisher).publishAttachment(data, ATTACHMENT_FILENAME);
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureQueryStringFromHarEntry() throws IOException
    {
        ProxySteps spy = spy(proxySteps);
        HarEntry harEntry = mock(HarEntry.class);
        doReturn(List.of(harEntry)).when(spy).checkNumberOfRequests(HTTP_METHOD, URL_PATTERN,
                ComparisonRule.EQUAL_TO, 1);
        HarRequest harRequest = mock(HarRequest.class);
        when(harEntry.getRequest()).thenReturn(harRequest);
        when(harRequest.getQueryString())
                .thenReturn(List.of(
                        getHarQueryParam(KEY1, VALUE1),
                        getHarQueryParam(KEY2, VALUE2)
                ));
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        spy.captureRequestAndSaveURLQuery(HTTP_METHOD, URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value ->
        {
            Map<String, String> map = (Map<String, String>) value;
            return map.size() == 2
                    && map.containsKey(KEY1)
                    && VALUE1.equals(map.get(KEY1))
                    && map.containsKey(KEY2)
                    && VALUE2.equals(map.get(KEY2));
        }));
    }

    @SuppressWarnings("unchecked")
    @Test
    void checkCaptureRequestDataFromHarEntry() throws IOException
    {
        ProxySteps spy = spy(proxySteps);
        HarEntry harEntry = mock(HarEntry.class);
        doReturn(List.of(harEntry)).when(spy).checkNumberOfRequests(HTTP_METHOD, URL_PATTERN,
                ComparisonRule.EQUAL_TO, 1);
        HarResponse harResponse = mock(HarResponse.class);
        when(harEntry.getResponse()).thenReturn(harResponse);
        HarRequest harRequest = mock(HarRequest.class);
        when(harEntry.getRequest()).thenReturn(harRequest);
        when(harRequest.getQueryString())
                .thenReturn(List.of(
                        getHarQueryParam(KEY1, VALUE1),
                        getHarQueryParam(KEY2, VALUE2)
                ));
        when(harRequest.getPostData()).thenReturn(getHarPostData());
        when(harResponse.getStatus()).thenReturn(200);
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        spy.captureRequestAndSaveRequestData(HTTP_METHOD, URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value ->
        {
            Map<String, Object> map = (Map<String, Object>) value;
            Map<String, String> urlQuery = (Map<String, String>) map.get("query");
            Map<String, String> requestBody = (Map<String, String>) map.get("requestBody");
            Map<String, String> requestBodyParameters = (Map<String, String>) map.get("requestBodyParameters");
            Integer responseStatus = (Integer) map.get("responseStatus");
            Assertions.assertAll(
                () -> Assertions.assertEquals(VALUE1, urlQuery.get(KEY1)),
                () -> Assertions.assertEquals(VALUE2, urlQuery.get(KEY2)),
                () -> Assertions.assertEquals(MIME_TYPE, requestBody.get(MIME_TYPE)),
                () -> Assertions.assertEquals(COMMENT, requestBody.get(COMMENT)),
                () -> Assertions.assertEquals(VALUE1, requestBodyParameters.get(KEY1)),
                () -> Assertions.assertEquals(VALUE2, requestBodyParameters.get(KEY2)),
                () -> Assertions.assertEquals(200, responseStatus));
            return true;
        }));
    }

    private HarPostData getHarPostData()
    {
        HarPostData postData = new HarPostData();
        postData.setMimeType(MIME_TYPE);
        postData.setText(TEXT);
        postData.setComment(COMMENT);
        postData.setParams(List.of(
                getHarPostDataParam(KEY1, VALUE1),
                getHarPostDataParam(KEY2, VALUE2))
        );
        return postData;
    }

    private HarPostDataParam getHarPostDataParam(String key, String value)
    {
        HarPostDataParam postDataParam = new HarPostDataParam();
        postDataParam.setName(key);
        postDataParam.setValue(value);
        return postDataParam;
    }

    private HarQueryParam getHarQueryParam(String key, String value)
    {
        HarQueryParam harQueryParam = new HarQueryParam();
        harQueryParam.setName(key);
        harQueryParam.setValue(value);
        return harQueryParam;
    }

    @Test
    void checkCaptureQueryStringSeveralHarEntriesFound() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        ProxySteps spy = spy(proxySteps);
        Mockito.lenient().doReturn(List.of(mock(HarEntry.class), mock(HarEntry.class))).when(spy)
                .checkNumberOfRequests(httpMethod, URL_PATTERN, ComparisonRule.EQUAL_TO, 1);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testWaitRequestInProxyLog()
    {
        HttpMethod httpMethod = HttpMethod.POST;
        proxySteps.waitRequestInProxyLog(httpMethod, URL_PATTERN);
        verify(waitActions).wait(eq(URL_PATTERN),
                argThat(e -> String.format("waiting for HTTP %s request with URL pattern %s",
                        httpMethod, URL_PATTERN).equals(e.toString())));
    }

    private byte[] mockProxyLog() throws IOException
    {
        HarCreatorBrowser browser = new HarCreatorBrowser();
        browser.setName("chrome");
        browser.setVersion("66");
        HarLog harLog = new HarLog();
        Har har = new Har();
        BrowserUpProxy mockBrowserUpProxy = mock(BrowserUpProxy.class);
        harLog.setBrowser(browser);
        harLog.setCreator(browser);
        har.setLog(harLog);
        when(mockBrowserUpProxy.getHar()).thenReturn(har);
        when(proxy.getProxyServer()).thenReturn(mockBrowserUpProxy);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OBJECT_MAPPER.writeValue(byteArrayOutputStream, har);
        return byteArrayOutputStream.toByteArray();
    }

    private void verifySizeAssertion(String message, long actualMatchedEntriesNumber, ComparisonRule rule,
            long callsNumber)
    {
        verify(nonFailingAssert).assertThat(eq(message), eq(actualMatchedEntriesNumber), argThat(
            object -> object != null && object.toString().equals(rule.getComparisonRule(callsNumber).toString())));
    }

    private void mockSizeAssertion(String message, long actualMatchedEntriesNumber, ComparisonRule rule,
            long callsNumber)
    {
        when(nonFailingAssert.assertThat(eq(message), eq(actualMatchedEntriesNumber),
                argThat(object -> object != null && object.toString()
                        .equals(rule.getComparisonRule(callsNumber).toString())))).thenReturn(true);
    }
}

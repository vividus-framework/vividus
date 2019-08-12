/*
 * Copyright 2019 the original author or authors.
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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpMethod;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.ProxyLog;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IWaitActions;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import net.lightbody.bmp.core.har.HarLog;
import net.lightbody.bmp.core.har.HarNameValuePair;
import net.lightbody.bmp.core.har.HarNameVersion;
import net.lightbody.bmp.core.har.HarRequest;
import net.lightbody.bmp.core.har.HarResponse;

@ExtendWith(MockitoExtension.class)
class ProxyStepsTests
{
    private static final String URL_PATTERN = "www.test.com";
    private static final String REQUESTS_MATCHING_URL_ASSERTION_PATTERN = "Number of HTTP %s requests matching URL "
            + "pattern '%s'";
    private static final String ATTACHMENT_FILENAME = "har.har";
    private static final String VARIABLE_NAME = "variable";

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
        verifyZeroInteractions(attachmentPublisher);
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
        HttpMethod httpMethod = HttpMethod.POST;
        ProxySteps spy = spy(proxySteps);
        HarEntry harEntry = mock(HarEntry.class);
        doReturn(List.of(harEntry)).when(spy).checkNumberOfRequests(httpMethod, URL_PATTERN,
                ComparisonRule.EQUAL_TO, 1);
        HarRequest harRequest = mock(HarRequest.class);
        when(harEntry.getRequest()).thenReturn(harRequest);
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
        String value2 = "value2";
        when(harRequest.getQueryString())
                .thenReturn(List.of(new HarNameValuePair(key1, value1), new HarNameValuePair(key2, value2)));
        Set<VariableScope> variableScopes = Set.of(VariableScope.SCENARIO);
        spy.captureRequestAndSaveURLQuery(httpMethod, URL_PATTERN, variableScopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(variableScopes), eq(VARIABLE_NAME), argThat(value ->
        {
            Map<String, String> map = (Map<String, String>) value;
            return map.size() == 2
                    && map.containsKey(key1)
                    && value1.equals(map.get(key1))
                    && map.containsKey(key2)
                    && value2.equals(map.get(key2));
        }));
    }

    @Test
    void checkCaptureQueryStringSeveralHarEntriesFound() throws IOException
    {
        HttpMethod httpMethod = HttpMethod.POST;
        ProxySteps spy = spy(proxySteps);
        Mockito.lenient().doReturn(List.of(mock(HarEntry.class), mock(HarEntry.class))).when(spy)
                .checkNumberOfRequests(httpMethod, URL_PATTERN, ComparisonRule.EQUAL_TO, 1);
        verifyZeroInteractions(bddVariableContext);
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
        HarNameVersion browser = new HarNameVersion("chrome", "66");
        HarLog harLog = new HarLog();
        Har har = new Har();
        BrowserMobProxy mockBrowserMobProxy = mock(BrowserMobProxy.class);
        harLog.setBrowser(browser);
        harLog.setCreator(browser);
        har.setLog(harLog);
        when(mockBrowserMobProxy.getHar()).thenReturn(har);
        when(proxy.getProxyServer()).thenReturn(mockBrowserMobProxy);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        har.writeTo(byteArrayOutputStream);
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

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

package org.vividus.steps.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.steps.StringComparisonRule.IS_EQUAL_TO;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.hamcrest.Matchers;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.http.ConnectionDetails;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ByteArrayValidationRule;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class HttpResponseValidationStepsTests
{
    private static final String ACCEPT_ENCODING_HEADER_NAME = "Accept-Encoding";
    private static final Header ACCEPT_ENCODING_HEADER = new BasicHeader(ACCEPT_ENCODING_HEADER_NAME,
            "deflate, gzip;q=1.0, *;q=0.5");
    private static final String HEADER_ELEMENT_NAME = "gzip";
    private static final List<String> HEADER_ELEMENT_NAMES = List.of("deflate", HEADER_ELEMENT_NAME, "*");

    private static final String HEADER_IS_PRESENT = " header is present";
    private static final String RESPONSE_BODY = "testResponse";
    private static final String VARIABLE_NAME = "variableName";
    private static final String NUMBER_OF_RESPONSE_HEADERS = "Number of the response headers with name '%s'";
    private static final String HTTP_RESPONSE_IS_NOT_NULL = "HTTP response is not null";
    private static final String TLS_V1_3 = "TLSv1.3";
    private static final String CONNECTION_SECURE_ASSERTION = "Connection is secure";
    private static final String HTTP_RESPONSE_STATUS_CODE = "HTTP response status code";
    private static final int RETRY_TIMES = 3;
    private static final int RESPONSE_CODE = 200;
    private static final int RESPONSE_CODE_ERROR = 404;
    private static final Duration DURATION = Duration.ofSeconds(9);
    private static final String RESPONSE_WITH_NO_BODY = "The response does not contain body";
    private static final String SECURITY_PROTOCOL = "Security protocol";
    private static final String HEADER_VALUE = "'%s' header value";
    private static final String EQUAL_ARRAYS = "Expected and actual arrays are equal";
    private static final String ARRAY_SIZE = "Arrays size";
    private static final String RESPONSE_BODY_TXT = "/requestBody.txt";

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private VariableContext variableContext;

    @Spy
    private final JsonUtils jsonUtils = new JsonUtils();

    @InjectMocks
    private HttpResponseValidationSteps httpResponseValidationSteps;

    private final HttpResponse httpResponse = new HttpResponse();

    @Test
    void shouldCheckThatHeaderWithNameContainsElements()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders(ACCEPT_ENCODING_HEADER);
        when(softAssert.assertTrue(ACCEPT_ENCODING_HEADER_NAME + HEADER_IS_PRESENT, true)).thenReturn(true);
        ExamplesTable elements = new ExamplesTable(String.format("|element|%n|%s|", HEADER_ELEMENT_NAME));
        httpResponseValidationSteps.checkHeaderContainsElements(ACCEPT_ENCODING_HEADER_NAME, elements);
        verify(softAssert).assertThat(
                eq(String.format("%s header contains %s element", ACCEPT_ENCODING_HEADER_NAME, HEADER_ELEMENT_NAME)),
                eq(HEADER_ELEMENT_NAMES),
                argThat(matcher -> matcher.toString().equals(Matchers.contains(HEADER_ELEMENT_NAME).toString())));
    }

    @Test
    void shouldNotCheckHeaderElementsIfHeaderWithNameIsNotFound()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn("Accept");
        httpResponse.setResponseHeaders(header);
        ExamplesTable attribute = new ExamplesTable("|element|\n|value|/|");
        httpResponseValidationSteps.checkHeaderContainsElements(ACCEPT_ENCODING_HEADER_NAME, attribute);
        verify(softAssert).assertTrue(ACCEPT_ENCODING_HEADER_NAME + HEADER_IS_PRESENT, false);
    }

    @Test
    void shouldNotCheckHeaderElementsIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.checkHeaderContainsElements(ACCEPT_ENCODING_HEADER_NAME, ExamplesTable.empty());
        verifyNoHttpResponse();
    }

    @Test
    void shouldValidateResponseTime()
    {
        mockHttpResponse();
        long responseTime = 1000L;
        httpResponse.setResponseTimeInMs(responseTime);
        httpResponseValidationSteps.validateResponseTime(ComparisonRule.LESS_THAN, responseTime);
        verify(softAssert).assertThat(eq("HTTP response time"), eq(responseTime),
                argThat(matcher -> lessThan(responseTime).toString().equals(matcher.toString())));
    }

    @Test
    void shouldNotValidateResponseTimeIfHttpCallWasNotPerfmed()
    {
        httpResponseValidationSteps.validateResponseTime(ComparisonRule.EQUAL_TO, 100L);
        verifyNoHttpResponse();
    }

    @Test
    void testDecompressedResponseBodySizeNoHttpResponse()
    {
        httpResponseValidationSteps.doesDecompressedResponseBodySizeConfirmRule(ComparisonRule.LESS_THAN, 10);
        verifyNoHttpResponse();
    }

    @Test
    void testDecompressedResponseBodySizeEqualTo()
    {
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        String body = RESPONSE_BODY;
        when(softAssert.assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, httpResponse)).thenReturn(true);
        httpResponse.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        httpResponseValidationSteps.doesDecompressedResponseBodySizeConfirmRule(ComparisonRule.EQUAL_TO, 10);
        verify(softAssert).assertThat(eq("Size of decompressed HTTP response body"),
                eq(body.getBytes(StandardCharsets.UTF_8).length),
                argThat(m -> "a value equal to <10>".equals(m.toString())));
    }

    @Test
    void shouldValidateResponseCode()
    {
        mockHttpResponse();
        int validCode = 200;
        httpResponse.setStatusCode(validCode);
        httpResponseValidationSteps.validateResponseCode(ComparisonRule.EQUAL_TO, validCode);
        verify(softAssert).assertThat(eq(HTTP_RESPONSE_STATUS_CODE), eq(validCode), argThat(
                matcher -> matcher.toString().equals(ComparisonRule.EQUAL_TO.getComparisonRule(validCode).toString())));
    }

    @Test
    void shouldNotValidateResponseCodeIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.validateResponseCode(ComparisonRule.EQUAL_TO, 200);
        verifyNoHttpResponse();
    }

    @Test
    void shouldCompareResponseBodyAgainstResourceByPath()
    {
        mockHttpResponse();
        when(softAssert.assertEquals(ARRAY_SIZE, 6, 6)).thenReturn(true);
        httpResponse.setResponseBody(new byte[] { 123, 98, 111, 100, 121, 125 });
        httpResponseValidationSteps.compareResponseBodyAgainstResource(ByteArrayValidationRule.IS_EQUAL_TO,
                RESPONSE_BODY_TXT);
        verify(softAssert).recordPassedAssertion(EQUAL_ARRAYS);
    }

    @Test
    void shouldNotCompareResponseBodyAgainstResourceByPathIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.compareResponseBodyAgainstResource(ByteArrayValidationRule.IS_EQUAL_TO, "data.txt");
        verifyNoHttpResponse();
    }

    @Test
    void testContentTypeOfResponseBody()
    {
        mockHttpResponse();
        httpResponse.setResponseBody(ResourceUtils.loadResourceAsByteArray(getClass(), "swf.swf"));
        testContentTypeOfResponseBody("application/x-shockwave-flash");
    }

    @ParameterizedTest
    @CsvSource({
            "{},             application/json",
            "[],             application/json",
            "' {\"key\":1}', application/json",
            "{',             text/plain",
            "1',             text/plain"
    })
    void testContentTypeOfResponseBodyWithText(String responseBody, String expectedContentType)
    {
        mockHttpResponse();
        httpResponse.setResponseBody(responseBody.getBytes(StandardCharsets.UTF_8));
        testContentTypeOfResponseBody(expectedContentType);
    }

    @Test
    void testContentTypeOfResponseBodyNoHttpResponse()
    {
        httpResponseValidationSteps.assertContentTypeOfResponseBody(IS_EQUAL_TO, "text/plain");
        verifyNoHttpResponse();
    }

    private void testContentTypeOfResponseBody(String contentType)
    {
        httpResponseValidationSteps.assertContentTypeOfResponseBody(IS_EQUAL_TO, contentType);
        verify(softAssert).assertThat(eq("Content type of response body"), eq(contentType),
                argThat(matcher -> matcher.toString().contains(contentType)));
    }

    @Test
    void shouldSaveHeaderValueToVariable()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        httpResponseValidationSteps.saveHeaderValueToVariable(ACCEPT_ENCODING_HEADER_NAME, scopes, VARIABLE_NAME);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, headerValue);
    }

    @Test
    void shouldNotSaveHeaderValueToVariableIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.saveHeaderValueToVariable(ACCEPT_ENCODING_HEADER_NAME,
                Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verifyNoHttpResponse();
    }

    @Test
    void shouldValidateHeaderValue()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        httpResponseValidationSteps.validateHeaderValue(ACCEPT_ENCODING_HEADER_NAME, IS_EQUAL_TO,
                headerValue);
        verify(softAssert).assertThat(
                eq(String.format(HEADER_VALUE, ACCEPT_ENCODING_HEADER_NAME)),
                eq(headerValue),
                argThat(matcher -> matcher.toString().equals(equalTo(headerValue).toString()))
        );
    }

    @Test
    void shouldNotValidateHeaderValueIfHttpCallWasNotPerfor()
    {
        httpResponseValidationSteps.validateHeaderValue(ACCEPT_ENCODING_HEADER_NAME, IS_EQUAL_TO, "header value");
        verifyNoHttpResponse();
    }

    @Test
    void shouldNotSaveHeaderValueIfHeaderByNameIsNotFound()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.saveHeaderValueToVariable(ACCEPT_ENCODING_HEADER_NAME,
                Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
        verify(softAssert).assertTrue(ACCEPT_ENCODING_HEADER_NAME + HEADER_IS_PRESENT, false);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldCheckThatTheResponseDoesNotContainABody()
    {
        mockHttpResponse();
        httpResponse.setResponseBody(null);
        httpResponseValidationSteps.doesResponseContainNoBody();
        verify(softAssert).assertNull(RESPONSE_WITH_NO_BODY, null);
    }

    @Test
    void shouldNotCheckResponseIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.doesResponseContainNoBody();
        verifyNoHttpResponse();
    }

    @Test
    void shouldFailIfResponseDoesntContainDesiredNumberOfHeaders()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.validateNumberOfResponseHeaders(ACCEPT_ENCODING_HEADER_NAME,
                ComparisonRule.EQUAL_TO, 1);
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_RESPONSE_HEADERS, ACCEPT_ENCODING_HEADER_NAME)), eq(0),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    @Test
    void shouldValidateNumberOfHeadersInResponse()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(ACCEPT_ENCODING_HEADER_NAME);
        httpResponse.setResponseHeaders(header, header);
        httpResponseValidationSteps.validateNumberOfResponseHeaders(ACCEPT_ENCODING_HEADER_NAME,
                ComparisonRule.EQUAL_TO, 2);
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_RESPONSE_HEADERS, ACCEPT_ENCODING_HEADER_NAME)), eq(2),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
    }

    @Test
    void shouldNotValidateNumberOfHeadersInResponseIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.validateNumberOfResponseHeaders(ACCEPT_ENCODING_HEADER_NAME,
                ComparisonRule.EQUAL_TO, 1);
        verifyNoHttpResponse();
    }

    @Test
    void shouldValidateNonSecuredConnectionDeprecated()
    {
        boolean secure = false;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);

        when(httpTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.FALSE);
        httpResponseValidationSteps.validateConnectionIsSecured(TLS_V1_3);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldValidateSecuredConnection()
    {
        boolean secure = true;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);
        connectionDetails.setSecurityProtocol(TLS_V1_3);

        when(httpTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.TRUE);
        httpResponseValidationSteps.validateConnectionIsSecured(TLS_V1_3);
        verify(softAssert).assertEquals(SECURITY_PROTOCOL, TLS_V1_3, TLS_V1_3);
    }

    @Test
    void shouldValidateNonSecuredConnection()
    {
        boolean secure = false;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);

        when(httpTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.FALSE);
        httpResponseValidationSteps.validateConnectionIsSecured(TLS_V1_3);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testWaitForResponseCode()
    {
        mockHttpResponse();
        SubSteps stepsToExecute = mock(SubSteps.class);
        httpResponseValidationSteps.waitForResponseCode(RESPONSE_CODE, DURATION, RETRY_TIMES, stepsToExecute);
        verify(stepsToExecute, atLeast(2)).execute(Optional.empty());
    }

    @Test
    void testWaitForResponseCodeWhenResponseCodeIsEqualToExpected()
    {
        SubSteps stepsToExecute = mock(SubSteps.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(softAssert.assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, httpResponse)).thenReturn(true);
        when(httpResponse.getStatusCode()).thenReturn(RESPONSE_CODE_ERROR, RESPONSE_CODE);
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        httpResponseValidationSteps.waitForResponseCode(RESPONSE_CODE, DURATION, RETRY_TIMES, stepsToExecute);
        verify(stepsToExecute, times(2)).execute(Optional.empty());
        verify(softAssert).assertEquals(HTTP_RESPONSE_STATUS_CODE, RESPONSE_CODE, RESPONSE_CODE);
    }

    @Test
    void testWaitForResponseCodeWhenResponseCodeIsNotEqualToExpected()
    {
        SubSteps stepsToExecute = mock(SubSteps.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(softAssert.assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, httpResponse)).thenReturn(true);
        when(httpResponse.getStatusCode()).thenReturn(RESPONSE_CODE_ERROR, RESPONSE_CODE_ERROR, RESPONSE_CODE_ERROR);
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        httpResponseValidationSteps.waitForResponseCode(RESPONSE_CODE, DURATION, RETRY_TIMES, stepsToExecute);
        verify(stepsToExecute, atLeast(3)).execute(Optional.empty());
        verify(softAssert).assertEquals(HTTP_RESPONSE_STATUS_CODE, RESPONSE_CODE, RESPONSE_CODE_ERROR);
    }

    private String mockHeaderRetrieval()
    {
        String headerValue = "headerValue";
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(ACCEPT_ENCODING_HEADER_NAME);
        httpResponse.setResponseHeaders(header);
        when(header.getValue()).thenReturn(headerValue);
        when(softAssert.assertTrue(ACCEPT_ENCODING_HEADER_NAME + HEADER_IS_PRESENT, true)).thenReturn(true);
        return headerValue;
    }

    private void mockHttpResponse()
    {
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        when(softAssert.assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, httpResponse)).thenReturn(true);
    }

    private void verifyNoHttpResponse()
    {
        verify(softAssert).assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, null);
        verifyNoMoreInteractions(softAssert);
    }
}

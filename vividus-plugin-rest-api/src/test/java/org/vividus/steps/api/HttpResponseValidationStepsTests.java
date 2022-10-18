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

package org.vividus.steps.api;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
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

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
@SuppressWarnings("checkstyle:MethodCount")
class HttpResponseValidationStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(HttpResponseValidationSteps.class);

    private static final String SET_COOKIES_HEADER_NAME = "Set-Cookies";
    private static final String HEADER_IS_PRESENT = " header is present";
    private static final String RESPONSE_BODY = "testResponse";
    private static final String HTTP_RESPONSE_BODY = "HTTP response body";
    private static final String VARIABLE_NAME = "variableName";
    private static final String NUMBER_RESPONSE_HEADERS_WITH_NAME =
            "The number of the response headers with the name '%s'";
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
    void testGetHeaderAttributes()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(SET_COOKIES_HEADER_NAME);
        httpResponse.setResponseHeaders(header);
        HeaderElement headerElement = mock(HeaderElement.class);
        when(header.getElements()).thenReturn(new HeaderElement[] { headerElement });
        when(softAssert.assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, true)).thenReturn(true);
        String headerAttributeName = "HTTPOnly";
        when(headerElement.getName()).thenReturn(headerAttributeName);
        ExamplesTable attribute = new ExamplesTable("|attribute|\n|HTTPOnly|/|");
        httpResponseValidationSteps.assertHeaderContainsAttributes(SET_COOKIES_HEADER_NAME, attribute);
        verify(softAssert).assertThat(
                eq(format("%s header contains %s attribute", SET_COOKIES_HEADER_NAME, headerAttributeName)),
                eq(Collections.singletonList(headerAttributeName)),
                argThat(matcher -> matcher.toString().equals(Matchers.contains(headerAttributeName).toString())));
        validateDeprecateMessage("Then response header '$httpHeaderName' contains attribute: $attributes",
                "Then response header `$headerName` contains elements:$elements");
    }

    @Test
    void testGetHeaderAttributesHeaderNotFound()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn("Vary");
        httpResponse.setResponseHeaders(header);
        ExamplesTable attribute = new ExamplesTable("|attribute|\n|Secure|/|");
        httpResponseValidationSteps.assertHeaderContainsAttributes(SET_COOKIES_HEADER_NAME, attribute);
        verify(softAssert).assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, false);
    }

    @Test
    void testGetHeaderAttributesNoHttpResponse()
    {
        httpResponseValidationSteps.assertHeaderContainsAttributes(SET_COOKIES_HEADER_NAME, ExamplesTable.empty());
        verifyNoHttpResponse();
    }

    @Test
    void shouldCheckThatHeaderWithNameContainsElements()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(SET_COOKIES_HEADER_NAME);
        httpResponse.setResponseHeaders(header);
        HeaderElement headerElement = mock(HeaderElement.class);
        when(header.getElements()).thenReturn(new HeaderElement[] { headerElement });
        when(softAssert.assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, true)).thenReturn(true);
        String headerAttributeName = "charset";
        when(headerElement.getName()).thenReturn(headerAttributeName);
        ExamplesTable attribute = new ExamplesTable("|element|\n|charset|/|");
        httpResponseValidationSteps.checkHeaderContainsElements(SET_COOKIES_HEADER_NAME, attribute);
        verify(softAssert).assertThat(
                eq(format("%s header contains %s element", SET_COOKIES_HEADER_NAME, headerAttributeName)),
                eq(Collections.singletonList(headerAttributeName)),
                argThat(matcher -> matcher.toString().equals(Matchers.contains(headerAttributeName).toString())));
    }

    @Test
    void shouldNotCheckHeaderElementsIfHeaderWithNameIsNotFound()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn("Accept");
        httpResponse.setResponseHeaders(header);
        ExamplesTable attribute = new ExamplesTable("|element|\n|value|/|");
        httpResponseValidationSteps.checkHeaderContainsElements(SET_COOKIES_HEADER_NAME, attribute);
        verify(softAssert).assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, false);
    }

    @Test
    void shouldNotCheckHeaderElementsIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.checkHeaderContainsElements(SET_COOKIES_HEADER_NAME, ExamplesTable.empty());
        verifyNoHttpResponse();
    }

    @Test
    void testThenTheResponseTimeShouldBeLessThan()
    {
        mockHttpResponse();
        long responseTime = 1000L;
        httpResponse.setResponseTimeInMs(responseTime);
        httpResponseValidationSteps.thenTheResponseTimeShouldBeLessThan(responseTime);
        verify(softAssert).assertThat(eq("The response time is less than response time threshold."),
                eq(responseTime), argThat(matcher -> lessThan(responseTime).toString().equals(matcher.toString())));
        validateDeprecateMessage("Then the response time should be less than '$responseTimeThresholdMs' milliseconds",
                "Then response time is $comparisonRule `$responseTime` milliseconds");
    }

    @Test
    void testThenTheResponseTimeShouldBeLessThanNoHttpResponse()
    {
        httpResponseValidationSteps.thenTheResponseTimeShouldBeLessThan(1000L);
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
    void testThenTheResponseCodeShouldBeEqualTo()
    {
        mockHttpResponse();
        int validCode = 200;
        httpResponse.setStatusCode(validCode);
        httpResponseValidationSteps.assertResponseCode(ComparisonRule.EQUAL_TO, validCode);
        verify(softAssert).assertThat(eq(HTTP_RESPONSE_STATUS_CODE), eq(validCode), argThat(
                matcher -> matcher.toString().equals(ComparisonRule.EQUAL_TO.getComparisonRule(validCode).toString())));
        validateDeprecateMessage("Then the response code is $comparisonRule '$responseCode'",
                "Then response code is $comparisonRule `$responseCode`");
    }

    @Test
    void testThenTheResponseCodeShouldBeEqualToNoHttpResponse()
    {
        httpResponseValidationSteps.assertResponseCode(ComparisonRule.EQUAL_TO, 200);
        verifyNoHttpResponse();
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
    void testDoesResponseBodyEqualToContent()
    {
        mockHttpResponse();
        String body = RESPONSE_BODY;
        httpResponse.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        httpResponseValidationSteps.doesResponseBodyMatch(IS_EQUAL_TO, body);
        verify(softAssert).assertThat(eq(HTTP_RESPONSE_BODY), eq(body),
                argThat(arg -> arg.toString().equals("\"testResponse\"")));
        assertThat(LOGGER.getLoggingEvents(), is(List.of(warn(
                "The step: \"Then the response body $comparisonRule '$content'\" is deprecated and will be removed in"
                + " VIVIDUS 0.6.0. Use ${response} dynamic variable with \"Then `$variable1` is $comparisonRule "
                + "`$variable2`\" step"))));
    }

    @Test
    void testDoesResponseBodyEqualToContentNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseBodyMatch(IS_EQUAL_TO, StringUtils.EMPTY);
        verifyNoHttpResponse();
    }

    @Test
    void testDoesResponseBodyMatchResource()
    {
        mockHttpResponse();
        when(softAssert.assertEquals(ARRAY_SIZE, 6, 6)).thenReturn(true);
        httpResponse.setResponseBody(new byte[] { 123, 98, 111, 100, 121, 125 });
        httpResponseValidationSteps.doesResponseBodyMatchResource(ByteArrayValidationRule.IS_EQUAL_TO,
                RESPONSE_BODY_TXT);
        verify(softAssert).recordPassedAssertion(EQUAL_ARRAYS);
        validateDeprecateMessage("Then the response body $validationRule resource at '$resourcePath'",
                "Then response body $validationRule resource at `$resourcePath`");
    }

    @Test
    void testDoesResponseBodyMatchResourceNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseBodyMatchResource(ByteArrayValidationRule.IS_EQUAL_TO, "body.txt");
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
    void testSaveHeaderValue()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        httpResponseValidationSteps.saveHeaderValue(SET_COOKIES_HEADER_NAME, scopes, VARIABLE_NAME);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, headerValue);
        validateDeprecateMessage(
                "When I save response header '$httpHeaderName' value to $scopes variable '$variableName'",
                "When I save response header `$headerName` value to $scopes variable `$variableName`");
    }

    @Test
    void testSaveHeaderValueNoHttpResponse()
    {
        httpResponseValidationSteps.saveHeaderValue(SET_COOKIES_HEADER_NAME, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verifyNoHttpResponse();
    }

    @Test
    void shouldSaveHeaderValueToVariable()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        httpResponseValidationSteps.saveHeaderValueToVariable(SET_COOKIES_HEADER_NAME, scopes, VARIABLE_NAME);
        verify(variableContext).putVariable(scopes, VARIABLE_NAME, headerValue);
    }

    @Test
    void shouldNotSaveHeaderValueToVariableIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.saveHeaderValueToVariable(SET_COOKIES_HEADER_NAME, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verifyNoHttpResponse();
    }

    @Test
    void testDoesHeaderEqualToValue()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        httpResponseValidationSteps.doesHeaderMatch(SET_COOKIES_HEADER_NAME, IS_EQUAL_TO,
                headerValue);
        verify(softAssert).assertThat(eq(format(HEADER_VALUE, SET_COOKIES_HEADER_NAME)), eq(headerValue),
                argThat(matcher -> matcher.toString().equals(equalTo(headerValue).toString())));
        validateDeprecateMessage("Then the value of the response header '$httpHeaderName' $comparisonRule '$value'",
                "Then value of response header `$headerName` $comparisonRule `$value`");
    }

    @Test
    void testDoesHeaderEqualToValueNoHttpResponse()
    {
        httpResponseValidationSteps.doesHeaderMatch(SET_COOKIES_HEADER_NAME, IS_EQUAL_TO, "value");
        verifyNoHttpResponse();
    }

    @Test
    void shouldValidateHeaderValue()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        httpResponseValidationSteps.validateHeaderValue(SET_COOKIES_HEADER_NAME, IS_EQUAL_TO,
                headerValue);
        verify(softAssert).assertThat(eq(format(HEADER_VALUE, SET_COOKIES_HEADER_NAME)), eq(headerValue),
                argThat(matcher -> matcher.toString().equals(equalTo(headerValue).toString())));
    }

    @Test
    void shouldNotValidateHeaderValueIfHttpCallWasNotPerfor()
    {
        httpResponseValidationSteps.validateHeaderValue(SET_COOKIES_HEADER_NAME, IS_EQUAL_TO, "header value");
        verifyNoHttpResponse();
    }

    @Test
    void testSaveHeaderValueHeaderNotFound()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.saveHeaderValue(SET_COOKIES_HEADER_NAME, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verify(softAssert).assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, false);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldNotSaveHeaderValueIfHeaderByNameIsNotFound()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.saveHeaderValueToVariable(SET_COOKIES_HEADER_NAME, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verify(softAssert).assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, false);
        verifyNoInteractions(variableContext);
    }

    @Test
    void testDoesResponseNotContainBody()
    {
        mockHttpResponse();
        httpResponse.setResponseBody(null);
        httpResponseValidationSteps.doesResponseNotContainBody();
        verify(softAssert).assertNull(RESPONSE_WITH_NO_BODY, null);
        validateDeprecateMessage("Then the response does not contain body", "Then response does not contain body");
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
    void testDoesResponseNotContainBodyNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseNotContainBody();
        verifyNoHttpResponse();
    }

    @Test
    void shouldNotCheckResponseIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.doesResponseContainNoBody();
        verifyNoHttpResponse();
    }

    @Test
    void testResponseNotContainsHeadersWithName()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.isHeaderWithNameFound(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO, 1);
        verify(softAssert).assertThat(eq(String.format(NUMBER_RESPONSE_HEADERS_WITH_NAME,
                SET_COOKIES_HEADER_NAME)), eq(0),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    @Test
    void testResponseContainsHeadersWithName()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(SET_COOKIES_HEADER_NAME);
        httpResponse.setResponseHeaders(header, header);
        httpResponseValidationSteps.isHeaderWithNameFound(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO, 2);
        verify(softAssert).assertThat(eq(String.format(NUMBER_RESPONSE_HEADERS_WITH_NAME,
                SET_COOKIES_HEADER_NAME)), eq(2),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
        validateDeprecateMessage(
                "Then the number of the response headers with the name '$headerName' is $comparisonRule $value",
                "Then number of response headers with name `$headerName` is $comparisonRule $number");
    }

    @Test
    void testResponseContainsHeadersWithNameNoHttpResponse()
    {
        httpResponseValidationSteps.isHeaderWithNameFound(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO, 1);
        verifyNoHttpResponse();
    }

    @Test
    void shouldFailIfResponseDoesntContainDesiredNumberOfHeaders()
    {
        mockHttpResponse();
        httpResponse.setResponseHeaders();
        httpResponseValidationSteps.validateNumberOfResponseHeaders(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO,
                1);
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_RESPONSE_HEADERS, SET_COOKIES_HEADER_NAME)), eq(0),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(1).toString().equals(m.toString())));
    }

    @Test
    void shouldValidateNumberOfHeadersInResponse()
    {
        mockHttpResponse();
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(SET_COOKIES_HEADER_NAME);
        httpResponse.setResponseHeaders(header, header);
        httpResponseValidationSteps.validateNumberOfResponseHeaders(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO,
                2);
        verify(softAssert).assertThat(eq(String.format(NUMBER_OF_RESPONSE_HEADERS, SET_COOKIES_HEADER_NAME)), eq(2),
                argThat(m -> ComparisonRule.EQUAL_TO.getComparisonRule(2).toString().equals(m.toString())));
    }

    @Test
    void shouldNotValidateNumberOfHeadersInResponseIfHttpCallWasNotPerformed()
    {
        httpResponseValidationSteps.validateNumberOfResponseHeaders(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO,
                1);
        verifyNoHttpResponse();
    }

    @Test
    void shouldValidateSecuredConnectionDeprecated()
    {
        boolean secure = true;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);
        connectionDetails.setSecurityProtocol(TLS_V1_3);

        when(httpTestContext.getConnectionDetails()).thenReturn(connectionDetails);
        when(softAssert.assertTrue(CONNECTION_SECURE_ASSERTION, secure)).thenReturn(Boolean.TRUE);
        httpResponseValidationSteps.isConnectionSecured(TLS_V1_3);
        verify(softAssert).assertEquals(SECURITY_PROTOCOL, TLS_V1_3, TLS_V1_3);
        validateDeprecateMessage("Then the connection is secured using $securityProtocol protocol",
                "Then connection is secured using $securityProtocol protocol");
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
        verify(stepsToExecute, times(3)).execute(Optional.empty());
        verify(softAssert).assertEquals(HTTP_RESPONSE_STATUS_CODE, RESPONSE_CODE_ERROR, RESPONSE_CODE);
    }

    private String mockHeaderRetrieval()
    {
        String headerValue = "headerValue";
        Header header = mock(Header.class);
        when(header.getName()).thenReturn(SET_COOKIES_HEADER_NAME);
        httpResponse.setResponseHeaders(header);
        when(header.getValue()).thenReturn(headerValue);
        when(softAssert.assertTrue(SET_COOKIES_HEADER_NAME + HEADER_IS_PRESENT, true)).thenReturn(true);
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

    private void validateDeprecateMessage(String deprecatedStep, String newStep)
    {
        assertThat(LOGGER.getLoggingEvents(),
                is(List.of(warn(
                        "The step \"{}\" is deprecated and will be removed in VIVIDUS 0.6.0. Please use step \"{}\"",
                        deprecatedStep, newStep))));
    }
}

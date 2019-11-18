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

package org.vividus.bdd.steps.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ByteArrayValidationRule;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class HttpResponseValidationStepsTests
{
    private static final String SET_COOKIES_HEADER_NAME = "Set-Cookies";
    private static final String HEADER_IS_PRESENT = " header is present";
    private static final String RESPONSE_BODY = "testResponse";
    private static final String HTTP_RESPONSE_BODY = "HTTP response body";
    private static final String VARIABLE_NAME = "variableName";
    private static final String NUMBER_RESPONSE_HEADERS_WITH_NAME =
            "The number of the response headers with the name '%s'";
    private static final String HTTP_RESPONSE_IS_NOT_NULL = "HTTP response is not null";

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IBddVariableContext bddVariableContext;

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
                eq(SET_COOKIES_HEADER_NAME + " header contains " + headerAttributeName + " attribute"),
                eq(Collections.singletonList(headerAttributeName)),
                argThat(matcher -> matcher.toString().equals(Matchers.contains(headerAttributeName).toString())));
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
        httpResponseValidationSteps.assertHeaderContainsAttributes(SET_COOKIES_HEADER_NAME,
                new ExamplesTable(StringUtils.EMPTY));
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
    }

    @Test
    void testThenTheResponseTimeShouldBeLessThanNoHttpResponse()
    {
        httpResponseValidationSteps.thenTheResponseTimeShouldBeLessThan(1000L);
        verifyNoHttpResponse();
    }

    @Test
    void testDecomressedResponseBodySizeNoHttpResponse()
    {
        httpResponseValidationSteps.doesDecomressedResponseBodySizeConfirmRule(ComparisonRule.LESS_THAN, 10);
        verifyNoHttpResponse();
    }

    @Test
    void testDecomressedResponseBodySizeEquslTo()
    {
        mockHttpResponse();
        String body = RESPONSE_BODY;
        when(softAssert.assertNotNull(HTTP_RESPONSE_IS_NOT_NULL, httpResponse)).thenReturn(true);
        httpResponse.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        httpResponseValidationSteps.doesDecomressedResponseBodySizeConfirmRule(ComparisonRule.EQUAL_TO, 10);
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
        verify(softAssert).assertThat(eq("HTTP response status code"), eq(validCode),
                argThat(matcher -> matcher.toString().equals("a value equal to <" + validCode + ">")));
    }

    @Test
    void testThenTheResponseCodeShouldBeEqualToNoHttpResponse()
    {
        httpResponseValidationSteps.assertResponseCode(ComparisonRule.EQUAL_TO, 200);
        verifyNoHttpResponse();
    }

    @Test
    void testDoesResponseBodyEqualToContent()
    {
        mockHttpResponse();
        String body = RESPONSE_BODY;
        httpResponse.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        httpResponseValidationSteps.doesResponseBodyMatch(StringComparisonRule.IS_EQUAL_TO, body);
        verify(softAssert).assertThat(eq(HTTP_RESPONSE_BODY), eq(body),
                argThat(arg -> arg.toString().equals("\"testResponse\"")));
    }

    @Test
    void testDoesResponseBodyEqualToContentNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseBodyMatch(StringComparisonRule.IS_EQUAL_TO, StringUtils.EMPTY);
        verifyNoHttpResponse();
    }

    @Test
    void testDoesResponseBodyMatchResource()
    {
        mockHttpResponse();
        when(softAssert.assertEquals("Arrays size", 6, 6)).thenReturn(true);
        httpResponse.setResponseBody(new byte[] { 123, 98, 111, 100, 121, 125 });
        httpResponseValidationSteps.doesResponseBodyMatchResource(ByteArrayValidationRule.IS_EQUAL_TO,
                "requestBody.txt");
        verify(softAssert).recordPassedAssertion("Expected and actual arrays are equal");
    }

    @Test
    void testDoesResponseBodyMatchResourceNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseBodyMatchResource(ByteArrayValidationRule.IS_EQUAL_TO, "body.txt");
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
            "{},                application/json",
            "[],                application/json",
            "' {\"key\":1}',    application/json",
            "{',                text/plain",
            "1',                text/plain"
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
        httpResponseValidationSteps.assertContentTypeOfResponseBody(StringComparisonRule.IS_EQUAL_TO, "text/plain");
        verifyNoHttpResponse();
    }

    private void testContentTypeOfResponseBody(String contentType)
    {
        httpResponseValidationSteps.assertContentTypeOfResponseBody(StringComparisonRule.IS_EQUAL_TO, contentType);
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
        verify(bddVariableContext).putVariable(scopes, VARIABLE_NAME, headerValue);
    }

    @Test
    void testSaveHeaderValueNoHttpResponse()
    {
        httpResponseValidationSteps.saveHeaderValue(SET_COOKIES_HEADER_NAME, Set.of(VariableScope.SCENARIO),
                VARIABLE_NAME);
        verifyNoHttpResponse();
    }

    @Test
    void testDoesHeaderEqualToValue()
    {
        mockHttpResponse();
        String headerValue = mockHeaderRetrieval();
        httpResponseValidationSteps.doesHeaderMatch(SET_COOKIES_HEADER_NAME, StringComparisonRule.IS_EQUAL_TO,
                headerValue);
        verify(softAssert).assertThat(eq("'" + SET_COOKIES_HEADER_NAME + "' header value"), eq(headerValue),
                argThat(matcher -> matcher.toString().equals(equalTo(headerValue).toString())));
    }

    @Test
    void testDoesHeaderEqualToValueNoHttpResponse()
    {
        httpResponseValidationSteps.doesHeaderMatch(SET_COOKIES_HEADER_NAME, StringComparisonRule.IS_EQUAL_TO, "value");
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
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void testDoesResponseNotContainBody()
    {
        mockHttpResponse();
        httpResponse.setResponseBody(null);
        httpResponseValidationSteps.doesResponseNotContainBody();
        verify(softAssert).assertNull("The response does not contain body", null);
    }

    @Test
    void testDoesResponseNotContainBodyNoHttpResponse()
    {
        httpResponseValidationSteps.doesResponseNotContainBody();
        verifyNoHttpResponse();
    }

    @Test
    void testSaveResponseBody()
    {
        mockHttpResponse();
        httpResponse.setResponseBody(RESPONSE_BODY.getBytes(StandardCharsets.UTF_8));
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        httpResponseValidationSteps.saveResponseBody(scopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(scopes, VARIABLE_NAME, RESPONSE_BODY);
    }

    @Test
    void testSaveResponseBodyNoHttpResponse()
    {
        httpResponseValidationSteps.saveResponseBody(Set.of(VariableScope.SCENARIO), VARIABLE_NAME);
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
    }

    @Test
    void testResponseContainsHeadersWithNameNoHttpResponse()
    {
        httpResponseValidationSteps.isHeaderWithNameFound(SET_COOKIES_HEADER_NAME, ComparisonRule.EQUAL_TO, 1);
        verifyNoHttpResponse();
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
}

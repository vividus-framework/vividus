/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.steps.integration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.ContextCopyingExecutor;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.Sleeper;
import org.vividus.validator.model.WebPageResourceValidation;

@ExtendWith(MockitoExtension.class)
class ResourceCheckStepsTests
{
    private static final String UNCHECKED = "unchecked";
    private static final String LINK_SELECTOR = "a";
    private static final String FIRST_PAGE_TABLE = "|pages|\n|https://first.page|";
    private static final String SECOND_PAGE_URL = "https://second.page";
    private static final String FIRST_PAGE_URL = "https://first.page";
    private static final String THIRD_PAGE_URL = "https://third.page";
    private static final String N_A = "N/A";
    private static final String RESULTS = "results";
    private static final String MAILTO_ID = "#mailto";
    private static final URI MAILTO_URI = URI.create("mailto:by.kalinin@gmail.com");
    private static final String JS_ID = "#js";
    private static final URI JS_URI = URI.create("javascript:void(0)");
    private static final String FTP_ID = "#ftp";
    private static final URI FTP_URI = URI.create("ftp://all-the-date.here");
    private static final String SHARP_ID = "#sharp";
    private static final URI SHARP_URI = URI.create("#");
    private static final URI FAQ_URI = URI.create("https://vividus.org/faq");
    private static final String ABOUT_ID = "#about";
    private static final String RELATIVE_ID = "#relative";
    private static final String HTTPS_ID = "#https";
    private static final String HTTP_ID = "#http";
    private static final String TEMPLATE_NAME = "resources-validation-result.ftl";
    private static final String REPORT_NAME = "Resource validation results";
    private static final URI VIVIDUS_URI = URI.create("https://vividus.org");
    private static final URI VIVIDUS_ABOUT_URI = URI.create("https://vividus.org/about");
    private static final URI SERENITY_URI = URI.create("http://www.thucydides.info");
    private static final URI VIVIDUS_QUERY_URI_1 = URI.create("https://vividus.org/products?name=pelmeshki");
    private static final String SELECTOR_QUERY_1 = "#query-params-one";
    private static final URI VIVIDUS_QUERY_URI_2 = URI.create("https://vividus.org/products?name=smetanka");
    private static final String SELECTOR_QUERY_2 = "#query-params-two";

    private static final String FIRST_PAGE =
          "<!DOCTYPE html>\n"
        + "<html>\n"
        + "<head>\n"
        +   "<meta charset=\"UTF-8\">\n"
        +   "<script src='https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.4.1.min.js' />"
        + "<title>Title of the document</title>\n"
        + "</head>\n"
        + "<body>\n"
        + "  <a id='https' href='https://vividus.org'>Eeny, meeny, miny, moe</a>\n"
        + "  <a id='relative' href='/faq'>Catch a tiger by the toe.</a>\n"
        + "  <a id='js' href='javascript:void(0)'>If he hollers, let him go,</a>\n"
        + "  <a id='sharp' href='#'>Eeny, meeny, miny, moe.</a>\n"
        + "  <a id='mailto' href='mailto:by.kalinin@gmail.com'>Say hi</a>\n"
        + "  <a id='http' href='http://www.thucydides.info'>Just ignore it</a>\n"
        + "  <a id='ftp' href='ftp://all-the-date.here'>Not supported scheme</a>\n"
        + "  <a id='query-params-one' href='/products?name=pelmeshki'>Dumplings</a>\n"
        + "  <a id='query-params-two' href='/products?name=smetanka'>Sour cream</a>\n"
        + "  <img id='image' src='https://avatars0.githubusercontent.com/u/48793437?s=200&v=4'/>\n"
        + "  <img class='image (gif)' src='https://github.githubassets.com/images/spinners/octocat-spinner-32.gif'/>\n"
        + "  <iframe src='https://selenide.org'/>"
        + "</body>\n</html>";

    private static final String SECOND_PAGE =
            "<!DOCTYPE html>"
          + "<html>"
          + "<head>"
          + "<title>Title of the document</title>"
          + "</head>"
          + "<body>"
          + "  <a id='about' href='https://vividus.org/about'>About</a>"
          + "</body>"
          + "</html>";

    private static final String THIRD_PAGE =
            "<!DOCTYPE html>\r\n"
          + "<html>\r\n"
          + "<head\\r\n"
          + "<title>Title of the document</title>\r\n"
          + "</head>\r\n"
          + "<body>\r\n"
          + "  <a id='link-id' href='https://vividus.org/about'>About</a>\r\n"
          + "  <video id='video-id'>Some video without attributes</a>\r\n"
          + "</body>\r\n"
          + "</html>\r\n";

    @Mock(lenient = true)
    private ResourceValidator resourceValidator;
    @Mock
    private AttachmentPublisher attachmentPublisher;
    @Mock
    private HttpRequestExecutor httpRequestExecutor;
    @Mock
    private HttpTestContext httpTestContext;
    @Mock
    private SoftAssert softAssert;
    @Mock
    private WebApplicationConfiguration webApplicationConfiguration;
    @Mock
    private ContextCopyingExecutor executor;

    @InjectMocks
    private ResourceCheckSteps resourceCheckSteps;

    @Test
    void shouldCheckDesiredResourcesAndPostAttachment() throws InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        resourceCheckSteps.setUriToIgnoreRegex(Optional.empty());
        resourceCheckSteps.init();
        URI imageUri = URI.create("https://avatars0.githubusercontent.com/u/48793437?s=200&v=4");
        URI gifImageUri = URI.create("https://github.githubassets.com/images/spinners/octocat-spinner-32.gif");
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(VIVIDUS_URI);
        resourceCheckSteps.checkResources("a, img", FIRST_PAGE);

        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(11));
            Iterator<WebPageResourceValidation> resourceValidations = validationsToReport.iterator();
            validate(resourceValidations, SERENITY_URI, HTTP_ID, CheckStatus.PASSED, N_A);
            validate(resourceValidations, imageUri, "#image", CheckStatus.PASSED, N_A);
            validate(resourceValidations, gifImageUri, N_A, CheckStatus.PASSED, N_A);
            validate(resourceValidations, VIVIDUS_URI, HTTPS_ID, CheckStatus.PASSED, N_A);
            validate(resourceValidations, FAQ_URI, RELATIVE_ID, CheckStatus.PASSED, N_A);
            validate(resourceValidations, VIVIDUS_QUERY_URI_1, SELECTOR_QUERY_1, CheckStatus.PASSED, N_A);
            validate(resourceValidations, VIVIDUS_QUERY_URI_2, SELECTOR_QUERY_2, CheckStatus.PASSED, N_A);
            validate(resourceValidations, SHARP_URI, SHARP_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, FTP_URI, FTP_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, JS_URI, JS_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, MAILTO_URI, MAILTO_ID, CheckStatus.FILTERED, N_A);
            return true;
        }), eq(REPORT_NAME));
    }

    @Test
    void shouldCheckResourcesFromPages() throws IOException, InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getResponseBodyAsString()).thenReturn(FIRST_PAGE, SECOND_PAGE);
        resourceCheckSteps.setUriToIgnoreRegex(Optional.empty());
        resourceCheckSteps.init();
        ExamplesTable examplesTable =
                new ExamplesTable("|pages|\n|https://first.page|\n|https://second.page|");
        lenient().doAnswer(call ->
        {
            Sleeper.sleep(5, TimeUnit.SECONDS);
            return null;
        }).when(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, SECOND_PAGE_URL, Optional.empty());
        resourceCheckSteps.checkResources(LINK_SELECTOR, examplesTable);
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, SECOND_PAGE_URL, Optional.empty());
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, FIRST_PAGE_URL, Optional.empty());
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(10));
            Iterator<WebPageResourceValidation> resourceValidations = validationsToReport.iterator();
            validate(resourceValidations.next(), SERENITY_URI, HTTP_ID, CheckStatus.PASSED);
            validate(resourceValidations.next(), URI.create(FIRST_PAGE_URL + "/faq"), RELATIVE_ID, CheckStatus.PASSED);
            validate(resourceValidations.next(), URI.create(FIRST_PAGE_URL + "/products?name=pelmeshki"),
                    SELECTOR_QUERY_1, CheckStatus.PASSED);
            validate(resourceValidations.next(), URI.create(FIRST_PAGE_URL + "/products?name=smetanka"),
                    SELECTOR_QUERY_2, CheckStatus.PASSED);
            validate(resourceValidations.next(), VIVIDUS_URI, HTTPS_ID, CheckStatus.PASSED);
            validate(resourceValidations.next(), VIVIDUS_ABOUT_URI, ABOUT_ID, CheckStatus.PASSED);
            validate(resourceValidations.next(), SHARP_URI, SHARP_ID, CheckStatus.FILTERED);
            validate(resourceValidations.next(), FTP_URI, FTP_ID, CheckStatus.FILTERED);
            validate(resourceValidations.next(), JS_URI, JS_ID, CheckStatus.FILTERED);
            validate(resourceValidations.next(), MAILTO_URI, MAILTO_ID, CheckStatus.FILTERED);
            return true;
        }), eq(REPORT_NAME));
    }

    @Test
    void shouldCheckResourcesFromPagesWithEmptyResource() throws IOException, InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getResponseBodyAsString()).thenReturn(THIRD_PAGE);
        resourceCheckSteps.setUriToIgnoreRegex(Optional.empty());
        resourceCheckSteps.init();
        ExamplesTable examplesTable =
                new ExamplesTable("|pages|\n|https://third.page|");
        resourceCheckSteps.checkResources(LINK_SELECTOR + ", video", examplesTable);
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, THIRD_PAGE_URL, Optional.empty());
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(1));
            Iterator<WebPageResourceValidation> resourceValidations = validationsToReport.iterator();
            validate(resourceValidations.next(), VIVIDUS_ABOUT_URI, "#link-id", CheckStatus.PASSED);
            return true;
        }), eq(REPORT_NAME));
        verify(softAssert).recordFailedAssertion("Element by selector #video-id doesn't contain href/src attributes");
    }

    @Test
    void shouldReportBrokenUrlWhenExceptionOccurs() throws IOException, InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        IOException ioException = new IOException();
        doThrow(ioException).when(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, FIRST_PAGE_URL,
                Optional.empty());
        resourceCheckSteps.setUriToIgnoreRegex(Optional.empty());
        resourceCheckSteps.init();
        ExamplesTable examplesTable =
                new ExamplesTable(FIRST_PAGE_TABLE);
        resourceCheckSteps.checkResources(LINK_SELECTOR, examplesTable);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(1));
            WebPageResourceValidation resourceValidation = validationsToReport.iterator().next();
            Assertions.assertAll(
                () -> assertNull(resourceValidation.getUri()),
                () -> assertEquals(N_A, resourceValidation.getCssSelector()),
                () -> assertSame(CheckStatus.BROKEN, resourceValidation.getCheckStatus()),
                () -> assertEquals(FIRST_PAGE_URL, resourceValidation.getPageURL()));
            return true;
        }), eq(REPORT_NAME));
        verifyNoInteractions(httpTestContext);
        verify(softAssert).recordFailedAssertion(
                "Unable to get page with URL: https://first.page", ioException);
    }

    @Test
    void shouldReportBrokenUrlWhenNoBodyReturned() throws IOException, InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpTestContext.getResponse()).thenReturn(httpResponse);
        resourceCheckSteps.setUriToIgnoreRegex(Optional.empty());
        resourceCheckSteps.init();
        ExamplesTable examplesTable =
                new ExamplesTable("|pages|\n|https://second.page|");
        resourceCheckSteps.checkResources(LINK_SELECTOR, examplesTable);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(1));
            WebPageResourceValidation resourceValidation = validationsToReport.iterator().next();
            Assertions.assertAll(
                () -> assertNull(resourceValidation.getUri()),
                () -> assertEquals(N_A, resourceValidation.getCssSelector()),
                () -> assertSame(CheckStatus.BROKEN, resourceValidation.getCheckStatus()),
                () -> assertEquals(SECOND_PAGE_URL, resourceValidation.getPageURL()));
            return true;
        }), eq(REPORT_NAME));
        verify(httpRequestExecutor).executeHttpRequest(HttpMethod.GET, SECOND_PAGE_URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                "Unable to get page with URL: https://second.page; Response is received without body;");
    }

    @Test
    void shouldReportExceptionOccuredInExecutingThread() throws InterruptedException, ExecutionException
    {
        InterruptedException interruptedException = new InterruptedException();
        doNothing().when(executor).execute(argThat(r -> true), argThat(v -> {
            v.uncaughtException(new Thread("Interrupted-0"), interruptedException);
            return true;
        }));
        resourceCheckSteps.checkResources(LINK_SELECTOR, FIRST_PAGE_TABLE);
        verify(softAssert).recordFailedAssertion("Exception occured in thread with name: Interrupted-0",
                interruptedException);
        verifyNoInteractions(httpTestContext, attachmentPublisher, resourceValidator);
    }

    private void mockResourceValidator()
    {
        when(resourceValidator.perform(any(WebPageResourceValidation.class)))
            .thenAnswer(invocation -> {
                WebPageResourceValidation resourceValidation = invocation.getArgument(0);
                resourceValidation.setCheckStatus(CheckStatus.PASSED);
                return resourceValidation;
            });
    }

    @Test
    void shouldFilterResourceByRegExpCheckDesiredResourcesAnPostAttachment()
            throws InterruptedException, ExecutionException
    {
        mockResourceValidator();
        runExecutor();
        resourceCheckSteps.setUriToIgnoreRegex(Optional.of("^((?!https).)*"));
        resourceCheckSteps.init();
        when(webApplicationConfiguration.getMainApplicationPageUrl()).thenReturn(VIVIDUS_URI);
        resourceCheckSteps.checkResources(LINK_SELECTOR, FIRST_PAGE);
        verify(attachmentPublisher).publishAttachment(eq(TEMPLATE_NAME), argThat(m -> {
            @SuppressWarnings(UNCHECKED)
            Set<WebPageResourceValidation> validationsToReport = ((Map<String, Set<WebPageResourceValidation>>) m)
                    .get(RESULTS);
            assertThat(validationsToReport, hasSize(9));
            Iterator<WebPageResourceValidation> resourceValidations = validationsToReport.iterator();
            validate(resourceValidations, VIVIDUS_URI, HTTPS_ID, CheckStatus.PASSED, N_A);
            validate(resourceValidations, FAQ_URI, RELATIVE_ID, CheckStatus.PASSED, N_A);
            validate(resourceValidations, VIVIDUS_QUERY_URI_1, SELECTOR_QUERY_1, CheckStatus.PASSED, N_A);
            validate(resourceValidations, VIVIDUS_QUERY_URI_2, SELECTOR_QUERY_2, CheckStatus.PASSED, N_A);
            validate(resourceValidations, SHARP_URI, SHARP_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, FTP_URI, FTP_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, SERENITY_URI, HTTP_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, JS_URI, JS_ID, CheckStatus.FILTERED, N_A);
            validate(resourceValidations, MAILTO_URI, MAILTO_ID, CheckStatus.FILTERED, N_A);
            return true;
        }), eq(REPORT_NAME));
    }

    private void runExecutor() throws InterruptedException, ExecutionException
    {
        doNothing().when(executor).execute(argThat(r -> {
            r.run();
            return true;
        }), any());
    }

    private void validate(Iterator<WebPageResourceValidation> toValidate, URI uri, String selector,
            CheckStatus checkStatus, String pageUrl)
    {
        WebPageResourceValidation actual = toValidate.next();
        validate(actual, uri, selector, checkStatus);
        assertEquals(pageUrl, actual.getPageURL());
    }

    private void validate(WebPageResourceValidation actual, URI uri, String selector, CheckStatus checkStatus)
    {
        Assertions.assertAll(
            () -> assertEquals(uri, actual.getUri()),
            () -> assertEquals(selector, actual.getCssSelector()),
            () -> assertSame(checkStatus, actual.getCheckStatus()));
    }
}

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

package org.vividus.steps.integration;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Then;
import org.vividus.http.ExpectedRedirect;
import org.vividus.http.HttpRedirectsProvider;
import org.vividus.http.RedirectValidationState;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.UriUtils;

public class RedirectValidationSteps
{
    private static final char SINGLE_QUOTE = '\'';

    private final HttpRedirectsProvider redirectsProvider;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;

    public RedirectValidationSteps(HttpRedirectsProvider redirectsProvider, ISoftAssert softAssert,
            IAttachmentPublisher attachmentPublisher)
    {
        this.redirectsProvider = redirectsProvider;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Check that all URLs redirect to proper pages with correct redirects number
     * <p>Validation fails if either actual final URL or number of redirects do not match the expected values</p>
     * <b>Example:</b>
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td><b>startUrl</b></td>
     * <td><b>endUrl</b></td>
     * <td><b>redirectsNumber</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>https://example.com/redirect</td>
     * <td>https://example.com/get-response</td>
     * <td>1</td>
     * </tr>
     * </tbody>
     * </table>
     * @param expectedRedirects Table of parameters where: <br>
     * <ul>
     * <li><b>startUrl</b> - The URL from which redirection starts.</li>
     * <li><b>endUrl</b> - The expected final URL to redirect to.</li>
     * <li><b>redirectsNumber</b> - The expected number of redirects between startUrl and endUrl (optional)</li>
     * </ul>
     */
    @Then("I validate HTTP redirects: $expectedRedirects")
    public void validateRedirects(List<ExpectedRedirect> expectedRedirects)
    {
        softAssert.runIgnoringTestFailFast(() ->
        {
            List<RedirectValidationState> redirectsResults = expectedRedirects.stream().map(e ->
            {
                RedirectValidationState validationState = new RedirectValidationState(e);
                checkRedirect(validationState, e);
                return validationState;
            }).toList();

            performPostCheckActions(redirectsResults);
        });
    }

    private void performPostCheckActions(List<RedirectValidationState> states)
    {
        attachmentPublisher.publishAttachment("/org/vividus/steps/integration/redirect-validation-test-report.ftl",
                Map.of("results", states), "Redirects validation test report");
    }

    private void checkRedirect(RedirectValidationState validationState, ExpectedRedirect expectedRedirect)
    {
        try
        {
            List<URI> redirects = redirectsProvider.getRedirects(expectedRedirect.getStartUrl());
            initTestResult(validationState, redirects);
            StringBuilder assertionMessageBuilder = new StringBuilder().append(SINGLE_QUOTE)
                    .append(expectedRedirect.getStartUrl());
            if (expectedRedirect.getEndUrl().equals(validationState.getActualEndUrl()))
            {
                assertionMessageBuilder.append("' redirected to '").append(expectedRedirect.getEndUrl())
                        .append(SINGLE_QUOTE);
                softAssert.recordPassedAssertion(assertionMessageBuilder.toString());

                assertRedirectsNumber(validationState);
            }
            else
            {
                assertionMessageBuilder.append("' should redirect to '").append(expectedRedirect.getEndUrl())
                        .append("', ");
                if (validationState.getActualEndUrl() != null)
                {
                    validationState.fail("different redirect URL's");
                    assertionMessageBuilder.append("but actually redirected to: '")
                            .append(validationState.getActualEndUrl()).append(SINGLE_QUOTE);
                    softAssert.recordFailedAssertion(assertionMessageBuilder.toString());
                }
                else if (UriUtils.removeUserInfo(expectedRedirect.getStartUrl()).equals(expectedRedirect.getEndUrl()))
                {
                    validationState.setActualEndUrl(expectedRedirect.getEndUrl());
                    String resultMessage = "no redirection expected";
                    validationState.pass(resultMessage);
                    assertionMessageBuilder.append(resultMessage);
                    softAssert.recordPassedAssertion(assertionMessageBuilder.toString());
                    assertRedirectsNumber(validationState);
                }
                else
                {
                    validationState.setActualEndUrl(expectedRedirect.getStartUrl());
                    validationState.fail("no redirect returned");
                    assertionMessageBuilder.append("but actually no redirect returned");
                    softAssert.recordFailedAssertion(assertionMessageBuilder.toString());
                }
            }
        }
        catch (IOException e)
        {
            validationState.fail(e.getMessage());
            softAssert.recordFailedAssertion(e);
        }
    }

    private void assertRedirectsNumber(RedirectValidationState validationState)
    {
        Integer expectedRedirectsNumber = validationState.getExpectedRedirect().getRedirectsNumber();
        if (null != expectedRedirectsNumber && expectedRedirectsNumber != validationState.getActualRedirectsNumber())
        {
            validationState.fail("different redirects number");
            softAssert.recordFailedAssertion(
                    String.format("Redirects number from '%s' to '%s' is expected to be '%d' but got '%d'",
                            validationState.getExpectedRedirect().getStartUrl(),
                            validationState.getExpectedRedirect().getEndUrl(), expectedRedirectsNumber,
                            validationState.getActualRedirectsNumber()));
        }
    }

    private static void initTestResult(RedirectValidationState validationState, List<URI> redirects)
    {
        if (!redirects.isEmpty())
        {
            validationState.setActualEndUrl(redirects.get(redirects.size() - 1));
            validationState.setActualRedirectsNumber(redirects.size());
            validationState.setRedirects(redirects);
        }
        else
        {
            validationState.setRedirects(new ArrayList<>());
            validationState.setActualRedirectsNumber(0);
        }
    }
}

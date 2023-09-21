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

package org.vividus.http;

import java.net.URI;
import java.util.List;

public class RedirectValidationState
{
    private final ExpectedRedirect expectedRedirect;

    private URI actualEndUrl;
    private int actualRedirectsNumber = -1;
    private List<URI> redirects;

    private String resultMessage = "Passed";
    private boolean passed = true;

    public RedirectValidationState(ExpectedRedirect expectedRedirect)
    {
        this.expectedRedirect = expectedRedirect;
    }

    public void fail(String resultMessage)
    {
        this.resultMessage = "Fail: " + resultMessage;
        passed = false;
    }

    public void pass(String resultMessage)
    {
        this.resultMessage = "Passed: " + resultMessage;
    }

    public ExpectedRedirect getExpectedRedirect()
    {
        return expectedRedirect;
    }

    public URI getActualEndUrl()
    {
        return actualEndUrl;
    }

    public void setActualEndUrl(URI actualEndUrl)
    {
        this.actualEndUrl = actualEndUrl;
    }

    public boolean isPassed()
    {
        return passed;
    }

    public String getResultMessage()
    {
        return resultMessage;
    }

    public int getActualRedirectsNumber()
    {
        return actualRedirectsNumber;
    }

    public void setActualRedirectsNumber(int actualRedirectsNumber)
    {
        this.actualRedirectsNumber = actualRedirectsNumber;
    }

    public List<URI> getRedirects()
    {
        return redirects;
    }

    public void setRedirects(List<URI> redirects)
    {
        this.redirects = redirects;
    }
}

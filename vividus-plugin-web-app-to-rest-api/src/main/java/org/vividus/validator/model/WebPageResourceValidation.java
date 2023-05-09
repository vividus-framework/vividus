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

package org.vividus.validator.model;

import java.net.URI;
import java.util.Comparator;

import org.apache.commons.lang3.tuple.Pair;
import org.vividus.http.validation.model.AbstractResourceValidation;

public class WebPageResourceValidation extends AbstractResourceValidation<WebPageResourceValidation>
{
    private static final String N_A = "N/A";

    private String cssSelector;
    private String pageURL;

    public WebPageResourceValidation()
    {
        this(null, N_A);
    }

    public WebPageResourceValidation(String error)
    {
        this(Pair.of(null, error), N_A);
    }

    public WebPageResourceValidation(Pair<URI, String> uriOrError, String cssSelector)
    {
        this(uriOrError, cssSelector, N_A);
    }

    public WebPageResourceValidation(Pair<URI, String> uriOrError, String cssSelector, String pageURL)
    {
        super(uriOrError);
        this.cssSelector = cssSelector;
        this.pageURL = pageURL;
    }

    public String getCssSelector()
    {
        return cssSelector;
    }

    public void setCssSelector(String cssSelector)
    {
        this.cssSelector = cssSelector;
    }

    public String getPageURL()
    {
        return pageURL;
    }

    public void setPageURL(String pageURL)
    {
        this.pageURL = pageURL;
    }

    public void setError(String error)
    {
        super.setUriOrError(Pair.of(null, error));
    }

    @Override
    public WebPageResourceValidation copy()
    {
        WebPageResourceValidation newValidation = new WebPageResourceValidation();
        newValidation.pageURL = this.pageURL;
        newValidation.cssSelector = this.cssSelector;
        copyParameters(newValidation);
        return newValidation;
    }

    @Override
    public int compareTo(WebPageResourceValidation toCompare)
    {
        int result = super.compareTo(toCompare);
        if (0 == result)
        {
            return Comparator.comparing(WebPageResourceValidation::getPageURL)
                             .thenComparing(WebPageResourceValidation::getCssSelector)
                             .compare(this, toCompare);
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "WebPageResourceValidation [uri=" + getUriOrError() + ", cssSelector=" + cssSelector + ", pageURL="
                + pageURL + ", statusCode=" + getStatusCode() + ", checkStatus=" + getCheckStatus() + "]";
    }
}

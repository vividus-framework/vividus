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

package org.vividus.validator.model;

import java.net.URI;
import java.util.Objects;

public class ResourceValidation implements Comparable<ResourceValidation>
{
    private static final String N_A = "N/A";

    private URI uri;
    private String cssSelector = N_A;
    private String pageURL = N_A;
    private int statusCode = -1;
    private CheckStatus checkStatus;

    public ResourceValidation()
    {
    }

    public ResourceValidation(URI toCheck, String cssSelector, String pageURL)
    {
        this.uri = toCheck;
        this.cssSelector = cssSelector;
        this.pageURL = pageURL;
    }

    public ResourceValidation(URI toCheck, String cssSelector)
    {
        this(toCheck, cssSelector, N_A);
    }

    public URI getUri()
    {
        return uri;
    }

    public void setUri(URI uri)
    {
        this.uri = uri;
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

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public CheckStatus getCheckStatus()
    {
        return checkStatus;
    }

    public void setCheckStatus(CheckStatus checkStatus)
    {
        this.checkStatus = checkStatus;
    }

    public ResourceValidation copy()
    {
        ResourceValidation newValidation = new ResourceValidation();
        newValidation.pageURL = this.pageURL;
        newValidation.statusCode = this.statusCode;
        newValidation.uri = this.uri;
        return newValidation;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(uri);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof ResourceValidation))
        {
            return false;
        }
        ResourceValidation other = (ResourceValidation) obj;
        return Objects.equals(uri.toString(), other.uri.toString());
    }

    @Override
    public int compareTo(ResourceValidation toCompare)
    {
        int result = Integer.compare(this.checkStatus.getWeight(), toCompare.checkStatus.getWeight());
        if (0 == result && this.getUri() != null && toCompare.getUri() != null)
        {
            result = this.getUri().toString().compareTo(toCompare.getUri().toString());
        }
        else if (0 == result)
        {
            result = this.getPageURL().compareTo(getPageURL());
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "ResourceValidation [uri=" + uri + ", cssSelector=" + cssSelector + ", pageURL=" + pageURL
                + ", statusCode=" + statusCode + ", checkStatus=" + checkStatus + "]";
    }
}

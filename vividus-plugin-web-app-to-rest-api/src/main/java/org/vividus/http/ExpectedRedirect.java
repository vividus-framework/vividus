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

import org.jbehave.core.annotations.AsParameters;

@AsParameters
public class ExpectedRedirect
{
    private URI startUrl;
    private URI endUrl;
    private Integer redirectsNumber;

    public URI getStartUrl()
    {
        return startUrl;
    }

    public void setStartUrl(URI startUrl)
    {
        this.startUrl = startUrl;
    }

    public URI getEndUrl()
    {
        return endUrl;
    }

    public void setEndUrl(URI endUrl)
    {
        this.endUrl = endUrl;
    }

    public Integer getRedirectsNumber()
    {
        return redirectsNumber;
    }

    public void setRedirectsNumber(Integer redirectsNumber)
    {
        this.redirectsNumber = redirectsNumber;
    }
}

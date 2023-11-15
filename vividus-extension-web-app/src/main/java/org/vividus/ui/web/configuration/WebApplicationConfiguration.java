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

package org.vividus.ui.web.configuration;

import static org.apache.commons.lang3.Validate.isTrue;

import java.net.URI;

import org.vividus.util.UriUtils;

public class WebApplicationConfiguration
{
    private URI mainApplicationPageUri;
    private String basicAuthUser;
    private final AuthenticationMode authenticationMode;

    public WebApplicationConfiguration(String mainApplicationPageUrl, AuthenticationMode authenticationMode)
    {
        this.authenticationMode = authenticationMode;
        if (mainApplicationPageUrl != null)
        {
            URI uri = UriUtils.createUri(mainApplicationPageUrl);
            this.basicAuthUser = uri.getUserInfo();
            this.mainApplicationPageUri = this.authenticationMode != null
                    ? this.authenticationMode.getUrl(mainApplicationPageUrl, basicAuthUser)
                    : uri;
        }
    }

    public URI getMainApplicationPageUrl()
    {
        isTrue(mainApplicationPageUri != null, "URL of the main application page should be non-blank");
        return mainApplicationPageUri;
    }

    /**
     * @return the main application page URL or null if it's not set
     */
    @SuppressWarnings("SimpleAccessorNameNotation")
    public URI getMainApplicationPageUrlUnsafely()
    {
        return mainApplicationPageUri;
    }

    public AuthenticationMode getAuthenticationMode()
    {
        return authenticationMode;
    }

    public String getBasicAuthUser()
    {
        return basicAuthUser;
    }
}

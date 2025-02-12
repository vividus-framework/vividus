/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.http.client;

import java.util.Map;

public class HttpContextConfig
{
    private String origin;
    private BasicAuthConfig auth;
    private Map<String, String> headers;

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    public BasicAuthConfig getAuth()
    {
        return auth;
    }

    public void setAuth(BasicAuthConfig auth)
    {
        this.auth = auth;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }
}

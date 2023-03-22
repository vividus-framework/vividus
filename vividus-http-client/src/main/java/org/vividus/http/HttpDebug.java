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

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.core5.annotation.Contract;

/**
 * HTTP DEBUG method
 * <p>
 * When the client tries to automatically attach the debugger in an .NET application, the client sends a HTTP
 * request that contains the DEBUG verb. This HTTP request is used to verify that the process of the application is
 * running and to select the correct process to attach.
 */
@Contract
public class HttpDebug extends HttpUriRequestBase
{
    public static final String METHOD = "DEBUG";

    private static final long serialVersionUID = 1L;

    public HttpDebug(final URI uri)
    {
        super(METHOD, uri);
    }

    public HttpDebug(final String uri)
    {
        this(URI.create(uri));
    }

    @Override
    public String getMethod()
    {
        return METHOD;
    }
}

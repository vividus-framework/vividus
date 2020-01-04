/*
 * Copyright 2019-2020 the original author or authors.
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

import org.apache.http.client.methods.HttpRequestBase;

/**
 * HTTP DEBUG method
 * <p>
 * When the client tries to automatically attach the debugger in an .NET application, the client sends a HTTP
 * request that contains the DEBUG verb. This HTTP request is used to verify that the process of the application is
 * running and to select the correct process to attach.
 */
public class HttpDebug extends HttpRequestBase
{
    public static final String METHOD = "DEBUG";

    public HttpDebug()
    {
        // Nothing to do
    }

    public HttpDebug(final URI uri)
    {
        setURI(uri);
    }

    public HttpDebug(final String uri)
    {
        setURI(URI.create(uri));
    }

    @Override
    public String getMethod()
    {
        return METHOD;
    }
}

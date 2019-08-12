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

package org.vividus.http;

import java.net.URI;

import org.apache.http.annotation.Contract;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

@Contract
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase
{
    public HttpDeleteWithBody()
    {
        // Nothing to do
    }

    public HttpDeleteWithBody(final URI uri)
    {
        setURI(uri);
    }

    /**
     * Create HTTP DELETE request wrapper
     * @param uri URI
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpDeleteWithBody(final String uri)
    {
        this(URI.create(uri));
    }

    @Override
    public String getMethod()
    {
        return HttpDelete.METHOD_NAME;
    }
}

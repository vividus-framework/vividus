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

package org.vividus.http.client;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.protocol.HttpContext;

public class EntityForwardingRedirectStrategy extends LaxRedirectStrategy
{
    @Override
    public HttpUriRequest getRedirect(final HttpRequest request, final HttpResponse response, final HttpContext context)
            throws ProtocolException
    {
        final String method = request.getRequestLine().getMethod();
        HttpUriRequest redirect = super.getRedirect(request, response, context);
        if (HttpPost.METHOD_NAME.equalsIgnoreCase(method))
        {
            HttpPost httpPostRequest = new HttpPost(redirect.getURI());
            if (request instanceof HttpEntityEnclosingRequest)
            {
                httpPostRequest.setEntity(((HttpEntityEnclosingRequest) request).getEntity());
            }
            return httpPostRequest;
        }
        return redirect;
    }
}


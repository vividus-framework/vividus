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

import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpResponseInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;

public class SavingConnectionDetailsHttpResponseInterceptor implements HttpResponseInterceptor
{
    private HttpTestContext httpTestContext;

    @Override
    public void process(HttpResponse response, EntityDetails entity, HttpContext context)
    {
        SSLSession sslSession = HttpClientContext.adapt(context).getSSLSession();
        boolean secure = sslSession != null;
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setSecure(secure);
        if (secure)
        {
            connectionDetails.setSecurityProtocol(sslSession.getProtocol());
        }
        httpTestContext.putConnectionDetails(connectionDetails);
    }

    public void setHttpTestContext(HttpTestContext httpTestContext)
    {
        this.httpTestContext = httpTestContext;
    }
}

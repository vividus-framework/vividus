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

package org.vividus.http.handler;

import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

public class IdempotentMethodsRetryHandler extends DefaultHttpRequestRetryHandler
{
    private List<String> idempotentMethodsSendingRequestBody;

    @Override
    protected boolean handleAsIdempotent(final HttpRequest request)
    {
        if (idempotentMethodsSendingRequestBody != null && !super.handleAsIdempotent(request))
        {
            String method = request.getRequestLine().getMethod().toUpperCase();
            return idempotentMethodsSendingRequestBody.contains(method);
        }
        return true;
    }

    public void setIdempotentMethodsSendingRequestBody(List<String> idempotentMethodsSendingRequestBody)
    {
        this.idempotentMethodsSendingRequestBody = idempotentMethodsSendingRequestBody;
    }
}

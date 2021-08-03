/*
 * Copyright 2019-2021 the original author or authors.
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

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;

public class StatusCodeValidatingHandler implements HttpResponseHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCodeValidatingHandler.class);

    private final int minInclusiveStatusCode;
    private final int maxInclusiveStatusCode;
    private final String serviceName;

    @SuppressWarnings("MagicNumber")
    public StatusCodeValidatingHandler(int minInclusiveStatusCode, int maxInclusiveStatusCode, String serviceName)
    {
        isTrue(minInclusiveStatusCode < maxInclusiveStatusCode,
                "Min allowed status code must be less than max status code");
        isTrue(minInclusiveStatusCode >= HttpStatus.SC_CONTINUE,
                "Min status code must be greater than or equal to %d, but got %d", HttpStatus.SC_CONTINUE,
                minInclusiveStatusCode);
        int maxAllowedStatusCode = 599;
        isTrue(maxInclusiveStatusCode <= maxAllowedStatusCode,
                "Min status code must be less than or equal to %d, but got %d", maxAllowedStatusCode,
                maxInclusiveStatusCode);
        this.minInclusiveStatusCode = minInclusiveStatusCode;
        this.maxInclusiveStatusCode = maxInclusiveStatusCode;
        this.serviceName = serviceName;
    }

    @Override
    public void handle(HttpResponse httpResponse) throws IOException
    {
        int status = httpResponse.getStatusCode();
        if (status >= minInclusiveStatusCode && status < maxInclusiveStatusCode)
        {
            return;
        }
        LOGGER.atError().addArgument(serviceName).addArgument(httpResponse).log("{} response: {}");
        throw new IOException(
                String.format("The status code is expected to be between %d and %d inclusively, but got: %d",
                        minInclusiveStatusCode, maxInclusiveStatusCode, status));
    }
}

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

package org.vividus.http;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingStatusCodeHttpResponseInterceptorTests
{
    private static final int STATUS_CODE = 301;

    @Mock private HttpTestContext httpTestContext;
    @Mock private HttpContext context;
    @Mock private HttpResponse response;

    @InjectMocks private SavingStatusCodeHttpResponseInterceptor interceptor;

    @Test
    void shouldSaveStatusCodeOnNewContext()
    {
        when(response.getCode()).thenReturn(STATUS_CODE);
        interceptor.process(response, null, context);
        verify(httpTestContext).recordStatusCodeData(STATUS_CODE, context);
    }
}

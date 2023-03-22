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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;

import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingConnectionDetailsHttpResponseInterceptorTests
{
    @Mock private HttpTestContext httpTestContext;
    @InjectMocks private SavingConnectionDetailsHttpResponseInterceptor interceptor;

    @Test
    void shouldSaveConnectionDetailsForSecuredConnection()
    {
        var protocol = "TLSv1.2";
        SSLSession sslSession = mock();
        when(sslSession.getProtocol()).thenReturn(protocol);
        shouldSaveConnectionDetails(sslSession, true, protocol);
    }

    @Test
    void shouldSaveConnectionDetailsForNonSecuredConnection()
    {
        shouldSaveConnectionDetails(null, false, null);
    }

    private void shouldSaveConnectionDetails(SSLSession sslSession, boolean secure, String protocol)
    {
        HttpClientContext context = mock();
        when(context.getSSLSession()).thenReturn(sslSession);
        interceptor.process(mock(HttpResponse.class), null, context);
        verify(httpTestContext).putConnectionDetails(argThat(connectionDetails -> connectionDetails.isSecure() == secure
                && Objects.equals(protocol, connectionDetails.getSecurityProtocol())));
    }
}

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

package org.vividus.api;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.net.ssl.SSLSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.HttpTestContext;
import org.vividus.http.SavingConnectionDetailsHttpResponseInterceptor;

@ExtendWith(MockitoExtension.class)
class SavingConnectionDetailsHttpResponseInterceptorTests
{
    @Mock
    private HttpTestContext httpTestContext;

    @InjectMocks
    private SavingConnectionDetailsHttpResponseInterceptor interceptor;

    @Test
    void shouldSaveConnectionDetailsForSecuredConnection()
    {
        String protocol = "TLSv1.2";
        SSLSession sslSession = mock(SSLSession.class);
        when(sslSession.getProtocol()).thenReturn(protocol);
        intercept(mockHttpContextWithNonStaledConnection(sslSession));
        verify(httpTestContext).putConnectionDetails(argThat(connectionDetails -> connectionDetails.isSecure()
                && protocol.equals(connectionDetails.getSecurityProtocol())));
    }

    @Test
    void shouldSaveConnectionDetailsForSecuredConnectionAndDoNotCheckStalenessForResponsesWithEntity()
    {
        String protocol = "TLSv1.3";
        SSLSession sslSession = mock(SSLSession.class);
        when(sslSession.getProtocol()).thenReturn(protocol);
        HttpContext context = mock(HttpContext.class);
        ManagedHttpClientConnection connection = mock(ManagedHttpClientConnection.class);
        when(context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).thenReturn(connection);
        when(connection.isOpen()).thenReturn(true);
        when(connection.getSSLSession()).thenReturn(sslSession);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getEntity()).thenReturn(mock(HttpEntity.class));
        interceptor.process(response, context);
        verify(httpTestContext).putConnectionDetails(argThat(connectionDetails -> connectionDetails.isSecure()
                && protocol.equals(connectionDetails.getSecurityProtocol())));
        verify(connection, never()).isStale();
    }

    @Test
    void shouldSaveConnectionDetailsForNonSecuredConnection()
    {
        interceptor.process(mock(HttpResponse.class), mockHttpContextWithNonStaledConnection(null));
        verify(httpTestContext).putConnectionDetails(argThat(
            connectionDetails -> !connectionDetails.isSecure() && connectionDetails.getSecurityProtocol() == null));
    }

    @Test
    void shouldSaveNoConnectionDetailsForStaleConnection()
    {
        HttpContext context = mock(HttpContext.class);
        mockHttpConnection(Boolean.TRUE, context, Boolean.TRUE);
        intercept(context);
        verifyNoInteractions(httpTestContext);
    }

    @Test
    void shouldSaveNoConnectionDetailsIfConnectionClosed()
    {
        HttpContext context = mock(HttpContext.class);
        ManagedHttpClientConnection connection = mock(ManagedHttpClientConnection.class);
        when(connection.isOpen()).thenReturn(Boolean.FALSE);
        when(context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).thenReturn(connection);
        intercept(context);
        verifyNoInteractions(httpTestContext);
        verify(connection, never()).isStale();
    }

    private void intercept(HttpContext context)
    {
        interceptor.process(mock(HttpResponse.class), context);
    }

    private static HttpContext mockHttpContextWithNonStaledConnection(SSLSession sslSession)
    {
        HttpContext context = mock(HttpContext.class);
        ManagedHttpClientConnection connection = mockHttpConnection(Boolean.FALSE, context, Boolean.TRUE);
        when(connection.getSSLSession()).thenReturn(sslSession);
        return context;
    }

    private static ManagedHttpClientConnection mockHttpConnection(Boolean stale, HttpContext context, Boolean closed)
    {
        ManagedHttpClientConnection connection = mock(ManagedHttpClientConnection.class);
        when(connection.isStale()).thenReturn(stale);
        when(context.getAttribute(HttpCoreContext.HTTP_CONNECTION)).thenReturn(connection);
        when(connection.isOpen()).thenReturn(closed);
        return connection;
    }
}

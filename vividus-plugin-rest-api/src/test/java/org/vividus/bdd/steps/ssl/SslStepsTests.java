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

package org.vividus.bdd.steps.ssl;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.CollectionComparisonRule;
import org.vividus.http.client.SslContextFactory;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class SslStepsTests
{
    private static final String HOST = "example.com";
    private static final String TLS_V_1_3 = "TLSv1.3";
    private static final String TLS_V_1_2 = "TLSv1.2";

    @Mock private ISoftAssert softAssert;
    @Mock private SslContextFactory sslContextFactory;
    @InjectMocks private SslSteps sslSteps;

    @Test
    void testCheckSupportedSecureProtocols() throws IOException, GeneralSecurityException
    {
        SSLContext sslContext = mock(SSLContext.class);
        SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);
        SSLSocket socket = mock(SSLSocket.class);

        when(sslContextFactory.getDefaultSslContext()).thenReturn(sslContext);
        when(sslContext.getSocketFactory()).thenReturn(sslSocketFactory);
        when(sslSocketFactory.createSocket(HOST, 443)).thenReturn(socket);
        when(socket.getEnabledProtocols()).thenReturn(new String[] { TLS_V_1_3, TLS_V_1_2 });

        sslSteps.checkSupportedSecureProtocols(HOST, CollectionComparisonRule.ARE_EQUAL_TO,
                new String[] { TLS_V_1_2, TLS_V_1_3 });

        verify(softAssert).assertThat(eq("Enabled secure protocols"), eq(new String[] { TLS_V_1_3, TLS_V_1_2 }),
            argThat(arg -> "[\"TLSv1.2\", \"TLSv1.3\"] in any order".equals(arg.toString())));
    }
}

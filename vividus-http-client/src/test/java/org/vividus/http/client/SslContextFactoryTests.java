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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.keystore.IKeyStoreFactory;

@ExtendWith(MockitoExtension.class)
class SslContextFactoryTests
{
    @Mock
    private IKeyStoreFactory keyStoreFactory;

    @InjectMocks
    private SslContextFactory sslContextFactory;

    @Test
    void testGetTrustingAllSslContext()
    {
        String protocol = SSLConnectionSocketFactory.SSL;
        SSLContext actualContext = sslContextFactory.getTrustingAllSslContext(protocol);
        assertEquals(protocol, actualContext.getProtocol());
    }

    @Test
    void testGetSslContext() throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null);
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));
        assertTrue(sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, false).isPresent());
    }

    @Test
    void testGetSslContextEmpty()
    {
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.empty());
        assertFalse(sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, false).isPresent());
    }

    @Test
    void testGetTrustAllSslContextEmpty()
    {
        SSLContext sslContext = mock(SSLContext.class);
        SslContextFactory spy = spy(sslContextFactory);
        doReturn(sslContext).when(spy).getTrustingAllSslContext(SSLConnectionSocketFactory.SSL);
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.empty());
        assertEquals(Optional.of(sslContext), spy.getSslContext(SSLConnectionSocketFactory.SSL, true));
    }

    @Test
    void testGetSslContextException()
    {
        KeyStore keyStore = mock(KeyStore.class);
        when(keyStoreFactory.getKeyStore()).thenReturn(Optional.of(keyStore));
        Exception exception = assertThrows(IllegalStateException.class,
            () -> sslContextFactory.getSslContext(SSLConnectionSocketFactory.SSL, true));
        assertEquals("Unable to use trusting all SSL context and custom SSL context together, please disable SSL"
                + " certificate check or loading of client certificates", exception.getMessage());
    }
}

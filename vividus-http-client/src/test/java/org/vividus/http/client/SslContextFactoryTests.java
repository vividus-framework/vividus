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

package org.vividus.http.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SslContextFactoryTests
{
    private static final String TLS = "TLS";

    private final SslContextFactory sslContextFactory = new SslContextFactory();

    @Test
    void shouldReturnDefaultSslContext() throws GeneralSecurityException
    {
        assertEquals(SSLContext.getDefault(), sslContextFactory.getDefaultSslContext());
    }

    @Test
    void shouldReturnTrustingAllSslContext() throws GeneralSecurityException
    {
        SSLContext actualContext = sslContextFactory.getTrustingAllSslContext();
        assertEquals(TLS, actualContext.getProtocol());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "pass")
    void shouldReturnSslContextWithoutUsingKeyPassword(String privateKeyPassword)
            throws GeneralSecurityException, IOException
    {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null);
        SSLContext actualContext = sslContextFactory.getSslContext(keyStore, privateKeyPassword);
        assertEquals(TLS, actualContext.getProtocol());
    }
}

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

import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;

public class SslContextFactory
{
    public SSLContext getDefaultSslContext() throws GeneralSecurityException
    {
        return SSLContext.getDefault();
    }

    public SSLContext getTrustingAllSslContext() throws GeneralSecurityException
    {
        return createBuilder().loadTrustMaterial(TrustAllStrategy.INSTANCE).build();
    }

    public SSLContext getSslContext(KeyStore keyStore, String privateKeyPassword) throws GeneralSecurityException
    {
        char[] privatePasswordKeyChars = privateKeyPassword != null ? privateKeyPassword.toCharArray() : null;
        return createBuilder().loadKeyMaterial(keyStore, privatePasswordKeyChars).build();
    }

    private static SSLContextBuilder createBuilder()
    {
        return SSLContextBuilder.create();
    }
}

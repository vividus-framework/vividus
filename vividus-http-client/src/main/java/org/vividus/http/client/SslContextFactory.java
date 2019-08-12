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

package org.vividus.http.client;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Optional;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.vividus.http.keystore.IKeyStoreFactory;

public class SslContextFactory implements ISslContextFactory
{
    private IKeyStoreFactory keyStoreFactory;
    private String privateKeyPassword;

    @Override
    public SSLContext getTrustingAllSslContext(String protocol)
    {
        try
        {
            return createBuilder(protocol)
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();
        }
        catch (GeneralSecurityException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Optional<SSLContext> getSslContext(String protocol, boolean trustAll)
    {
        Optional<KeyStore> keyStore = keyStoreFactory.getKeyStore();
        if (trustAll)
        {
            if (keyStore.isPresent())
            {
                throw new IllegalStateException(
                        "Unable to use trusting all SSL context and custom SSL context together, "
                                + "please disable SSL certificate check or loading of client certificates");
            }
            return Optional.of(getTrustingAllSslContext(protocol));
        }
        if (keyStore.isPresent())
        {
            return keyStore.map(k ->
            {
                try
                {
                    char[] privatePasswordKeyChars = privateKeyPassword != null ? privateKeyPassword.toCharArray()
                            : null;
                    return createBuilder(protocol).loadKeyMaterial(k, privatePasswordKeyChars).build();
                }
                catch (GeneralSecurityException e)
                {
                    throw new IllegalStateException(e);
                }
            });
        }
        return Optional.empty();
    }

    private static SSLContextBuilder createBuilder(String protocol)
    {
        return SSLContextBuilder.create().setProtocol(protocol);
    }

    public void setKeyStoreFactory(IKeyStoreFactory keyStoreFactory)
    {
        this.keyStoreFactory = keyStoreFactory;
    }

    public void setPrivateKeyPassword(String privateKeyPassword)
    {
        this.privateKeyPassword = privateKeyPassword;
    }
}

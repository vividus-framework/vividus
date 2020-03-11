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

package org.vividus.http.keystore;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Optional;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.vividus.util.ResourceUtils;

public class KeyStoreFactory implements IKeyStoreFactory
{
    private final KeyStoreOptions keyStoreOptions;

    private final LazyInitializer<Optional<KeyStore>> cachedKeyStore = new LazyInitializer<>()
    {
        @Override
        protected Optional<KeyStore> initialize()
        {
            try
            {
                String keyStorePath = keyStoreOptions.getPath();
                if (keyStorePath != null)
                {
                    String keyStorePassword = keyStoreOptions.getPassword();
                    if (keyStorePassword == null)
                    {
                        throw new IllegalStateException(String.format("Key store password for %s %s must not be null",
                                keyStoreOptions.getType(), keyStorePath));
                    }
                    KeyStore keyStore = KeyStore.getInstance(keyStoreOptions.getType());
                    try (InputStream inputStream = ResourceUtils.findResource(getClass(), keyStorePath).openStream())
                    {
                        keyStore.load(inputStream, keyStorePassword.toCharArray());
                    }
                    return Optional.of(keyStore);
                }
                return Optional.empty();
            }
            catch (GeneralSecurityException | IOException e)
            {
                throw new IllegalStateException(e);
            }
        }
    };

    public KeyStoreFactory(KeyStoreOptions keyStoreOptions)
    {
        this.keyStoreOptions = keyStoreOptions;
    }

    @Override
    public Optional<KeyStore> getKeyStore()
    {
        try
        {
            return cachedKeyStore.get();
        }
        catch (ConcurrentException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public KeyStoreOptions getKeyStoreOptions()
    {
        return keyStoreOptions;
    }
}

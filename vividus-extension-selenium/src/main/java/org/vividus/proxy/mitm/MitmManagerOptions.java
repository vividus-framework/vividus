/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.proxy.mitm;

import org.vividus.http.keystore.KeyStoreOptions;
import org.vividus.proxy.model.MitmManagerType;

public class MitmManagerOptions
{
    private final MitmManagerType mitmManagerType;
    private final String alias;
    private final boolean trustAllServers;
    private final KeyStoreOptions keyStoreOptions;

    public MitmManagerOptions(MitmManagerType mitmManagerType, String alias, boolean trustAllServers,
            KeyStoreOptions keyStoreOptions)
    {
        this.mitmManagerType = mitmManagerType;
        this.alias = alias;
        this.trustAllServers = trustAllServers;
        this.keyStoreOptions = keyStoreOptions;
    }

    public MitmManagerType getMitmManagerType()
    {
        return mitmManagerType;
    }

    public String getAlias()
    {
        return alias;
    }

    public boolean isTrustAllServers()
    {
        return trustAllServers;
    }

    public KeyStoreOptions getKeyStoreOptions()
    {
        return keyStoreOptions;
    }
}

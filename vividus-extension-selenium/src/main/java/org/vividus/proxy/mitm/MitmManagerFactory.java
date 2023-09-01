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

package org.vividus.proxy.mitm;

import org.apache.commons.lang3.Validate;
import org.littleshoot.proxy.MitmManager;
import org.vividus.http.keystore.KeyStoreOptions;

public class MitmManagerFactory implements IMitmManagerFactory
{
    @Override
    public MitmManager createMitmManager(MitmManagerOptions options)
    {
        KeyStoreOptions keyStoreOptions = options.keyStoreOptions();
        checkNotNull(keyStoreOptions.path(), "key store path");
        checkNotNull(keyStoreOptions.type(), "key store type");
        checkNotNull(keyStoreOptions.password(), "key store password");
        checkNotNull(options.alias(), "alias");

        return options.mitmManagerType().buildMitmManager(options, keyStoreOptions);
    }

    private void checkNotNull(Object value, String parameter)
    {
        Validate.isTrue(value != null, parameter + " parameter must be set");
    }
}

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

package org.vividus.proxy.model;

import java.io.File;

import com.browserup.bup.mitm.KeyStoreFileCertificateSource;
import com.browserup.bup.mitm.manager.ImpersonatingMitmManager;

import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.extras.SelfSignedSslEngineSource;
import org.vividus.http.keystore.KeyStoreOptions;
import org.vividus.proxy.mitm.MitmManagerOptions;
import org.vividus.util.ResourceUtils;

public enum MitmManagerType
{
    IMPERSONATED
    {
        @Override
        public MitmManager buildMitmManager(MitmManagerOptions options, KeyStoreOptions keyStoreOptions)
        {
            File keyStore = ResourceUtils.loadFile(getClass(), keyStoreOptions.getPath());
            KeyStoreFileCertificateSource certificateSource = new KeyStoreFileCertificateSource(
                    keyStoreOptions.getType(), keyStore, options.getAlias(), keyStoreOptions.getPassword());

            return ImpersonatingMitmManager
                    .builder()
                    .rootCertificateSource(certificateSource)
                    .trustAllServers(options.isTrustAllServers())
                    .build();
        }
    },
    SELF_SIGNED
    {
        @Override
        public MitmManager buildMitmManager(MitmManagerOptions options, KeyStoreOptions keyStoreOptions)
        {
            SelfSignedSslEngineSource sslEngineSource = new SelfSignedSslEngineSource(
                    keyStoreOptions.getPath(),
                    options.isTrustAllServers(),
                    true,
                    options.getAlias(),
                    keyStoreOptions.getPassword());

            return new SelfSignedMitmManager(sslEngineSource);
        }
    };

    public abstract MitmManager buildMitmManager(MitmManagerOptions options, KeyStoreOptions keyStoreOptions);
}

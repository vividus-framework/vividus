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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.vividus.proxy.model.MitmManagerType.IMPERSONATED;
import static org.vividus.proxy.model.MitmManagerType.SELF_SIGNED;

import java.nio.file.Path;

import com.browserup.bup.mitm.manager.ImpersonatingMitmManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.littleshoot.proxy.MitmManager;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.vividus.http.keystore.KeyStoreOptions;

class MitmManagerFactoryTests
{
    private static final String KEYSTORE_PATH = "bundle.p12";
    private static final String ALIAS = "alias";
    private static final String PASSWORD = "password";

    private final IMitmManagerFactory factory = new MitmManagerFactory();

    @Test
    void testCreateImpersonatingMitmManager()
    {
        MitmManagerOptions options = new MitmManagerOptions(IMPERSONATED, ALIAS, true,
                new KeyStoreOptions(KEYSTORE_PATH, PASSWORD, "PKCS12"));
        MitmManager mitmManager = factory.createMitmManager(options);
        assertThat(mitmManager, instanceOf(ImpersonatingMitmManager.class));
    }

    @Test
    void testCreateSelfSignedMitmManager(@TempDir Path tempDir)
    {
        MitmManagerOptions options = new MitmManagerOptions(SELF_SIGNED, ALIAS, false,
                new KeyStoreOptions(tempDir.resolve(KEYSTORE_PATH).toString(), PASSWORD, "JKS"));
        MitmManager mitmManager = factory.createMitmManager(options);
        assertThat(mitmManager, instanceOf(SelfSignedMitmManager.class));
    }

    @Test
    void testCreateMitmManagerWithInvalidParameters()
    {
        MitmManagerOptions options = new MitmManagerOptions(IMPERSONATED, null, true,
                new KeyStoreOptions(null, null, null));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> factory.createMitmManager(options));
        assertEquals("key store path parameter must be set", exception.getMessage());
    }
}

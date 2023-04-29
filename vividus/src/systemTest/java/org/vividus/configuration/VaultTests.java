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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.vault.VaultContainer;

@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
@EnabledOnOs(OS.LINUX)
@Testcontainers
class VaultTests
{
    private static final String CONFIGURATION_SUITES = "configuration.suites";

    private static final String VAULT_URI = "vault.uri";
    private static final String VAULT_TOKEN = "vividus-root-token";

    private static final String NEVER_SHOW_IT = "never-show-it";
    private static final String NEVER_NEVER_SHOW_IT = "never-never-show-it";

    @Container
    private final VaultContainer<?> vaultContainer = new VaultContainer<>("hashicorp/vault:1.13")
            .withVaultToken(VAULT_TOKEN)
            .withSecretInVault("secret/vividus/test",
                    "top_secret=" + NEVER_SHOW_IT,
                    "one_more_secret=" + NEVER_NEVER_SHOW_IT
            );

    @BeforeEach
    void beforeEach()
    {
        resetBeanFactory();
    }

    @AfterEach
    void afterEach()
    {
        resetBeanFactory();
    }

    @Test
    void shouldNotAttemptToAccessVaultIfNoVaultPropertiesAreSet()
    {
        System.setProperty(CONFIGURATION_SUITES, "");
        System.setProperty(VAULT_URI, vaultContainer.getHttpHostAddress());
        BeanFactory.open();
    }

    @Test
    void shouldResolvePropertyValueStoredInVault() throws IOException
    {
        System.setProperty(CONFIGURATION_SUITES, "valid");
        System.setProperty(VAULT_URI, vaultContainer.getHttpHostAddress());
        BeanFactory.open();
        var properties = ConfigurationResolver.getInstance().getProperties();
        assertEquals(NEVER_SHOW_IT, properties.getProperty("property-stored-in-vault"));
        assertEquals(NEVER_NEVER_SHOW_IT, properties.getProperty("one-more-property-stored-in-vault"));
    }

    @Test
    void shouldFailToResolvePropertyValueStoredInVault()
    {
        System.setProperty(CONFIGURATION_SUITES, "broken");
        System.setProperty(VAULT_URI, vaultContainer.getHttpHostAddress());
        var exception = assertThrows(IllegalArgumentException.class, BeanFactory::open);
        assertEquals("Unable to find secret at path 'secret/vividus/test/missing_secret' in Vault",
                exception.getMessage());
    }

    private void resetBeanFactory()
    {
        System.clearProperty(CONFIGURATION_SUITES);
        BeanFactory.reset();
        ConfigurationResolver.reset();
    }
}

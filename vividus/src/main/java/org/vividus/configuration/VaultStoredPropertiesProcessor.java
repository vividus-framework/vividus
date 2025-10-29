/*
 * Copyright 2019-2025 the original author or authors.
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

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.vault.client.RestTemplateCustomizer;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.config.EnvironmentVaultConfiguration;
import org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

public class VaultStoredPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VaultStoredPropertiesProcessor.class);
    private static final Pattern SECRET_PATTERN = Pattern.compile("(?<engine>[^/]+)/(?<path>.+)/(?<key>[^/]+)$");
    private static final String VAULT_PROCESSOR_ENABLED_PROPERTY = "secrets-manager.vault.enabled";

    private Properties properties;
    private VaultTemplate vaultTemplate;
    private AnnotationConfigApplicationContext context;

    public VaultStoredPropertiesProcessor()
    {
        super("(?<!AZURE_KEY_)(?:VAULT|HASHI_CORP_VAULT)");
    }

    @Override
    public boolean isEnabled(Properties properties)
    {
        return Boolean.parseBoolean(properties.getProperty(VAULT_PROCESSOR_ENABLED_PROPERTY));
    }

    @Override
    public Properties processProperties(Properties properties)
    {
        this.properties = properties;
        return super.processProperties(properties);
    }

    @Override
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        Matcher matcher = SECRET_PATTERN.matcher(partOfPropertyValueToProcess);
        Validate.isTrue(matcher.matches(),
                "Full secret path must follow pattern 'engine/path_with_separators/key', but '%s' was found in "
                        + "property '%s'",
                partOfPropertyValueToProcess, propertyName);
        String engine = matcher.group("engine");
        String path = matcher.group("path");
        VaultResponse response = getVaultTemplate().opsForKeyValue(engine, KeyValueBackend.KV_2).get(path);
        return Optional.ofNullable(response)
                .map(VaultResponse::getData)
                .map(data -> data.get(matcher.group("key")))
                .map(String.class::cast)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Unable to find secret at path '%s' in Vault", partOfPropertyValueToProcess)
                ));
    }

    @Override
    public String processProperty(String propertyName, String propertyValue)
    {
        if (propertyValue.matches(".*(?<!AZURE_KEY_|HASHI_CORP_)VAULT\\((.*?)\\).*"))
        {
            LOGGER.warn("`VAULT(...)` placeholder is deprecated and will be removed in VIVIDUS 0.7.0, "
                    + "please use `HASHI_CORP_VAULT(...)` placeholder instead.");
        }
        return super.processProperty(propertyName, propertyValue);
    }

    @Override
    public void close() throws IOException
    {
        if (context != null)
        {
            context.close();
        }
    }

    private VaultTemplate getVaultTemplate()
    {
        if (vaultTemplate == null)
        {
            context = new AnnotationConfigApplicationContext();
            PropertiesPropertySource propertySource = new PropertiesPropertySource("properties", properties);
            context.getEnvironment().getPropertySources().addFirst(propertySource);

            Optional.ofNullable(properties.getProperty("vault.namespace")).ifPresent(namespace ->
                    context.registerBean(RestTemplateCustomizer.class, () -> restTemplate ->
                            restTemplate.getInterceptors().add(VaultClients.createNamespaceInterceptor(namespace))
                    )
            );
            context.register(EnvironmentVaultConfiguration.class);
            context.refresh();

            vaultTemplate = context.getBean(VaultTemplate.class);
        }
        return vaultTemplate;
    }
}

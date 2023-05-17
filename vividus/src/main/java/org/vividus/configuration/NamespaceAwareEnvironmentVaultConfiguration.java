package org.vividus.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.client.RestTemplateCustomizer;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.config.EnvironmentVaultConfiguration;

@Configuration
public class NamespaceAwareEnvironmentVaultConfiguration extends EnvironmentVaultConfiguration
{
    @Value("${vault.namespace}")
    private String namespace;

    @Bean
    public RestTemplateCustomizer addingNamespaceHeaderRestTemplateCustomizer() {
        return (restTemplate) -> restTemplate.getInterceptors().add(VaultClients.createNamespaceInterceptor(namespace));
    }
}
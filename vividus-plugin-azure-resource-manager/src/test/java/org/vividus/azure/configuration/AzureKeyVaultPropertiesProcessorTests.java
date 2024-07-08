/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.azure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

@ExtendWith(MockitoExtension.class)
public class AzureKeyVaultPropertiesProcessorTests
{
    private static final String KEY_VAULT_PROPERTY_NAME = "azure.key-vault.url";
    private static final String KEY_VAULT_NAME = "testkeyvault";
    private static final String KEY_VAULT_VALUE = "admin_12345";
    private static final String PROPERTY_NAME = "property-name";
    private static final String PROPERTY_VALUE = "variables-userPassword";

    @Test
    void shouldFailToProcessPropertiesWithoutKeyVaultName()
    {
        var properties = new Properties();
        properties.setProperty(KEY_VAULT_PROPERTY_NAME, "");
        var processor = new AzureKeyVaultPropertiesProcessor();
        processor.processProperties(properties);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processor.processValue(PROPERTY_NAME, PROPERTY_VALUE));
        assertEquals(
                String.format("Provide value into property `%s`", KEY_VAULT_PROPERTY_NAME), exception.getMessage());
    }

    @Test
    void shouldProcessPropertiesWithKeyVaultNameWithoutClient()
    {
        var properties = new Properties();
        properties.put(KEY_VAULT_PROPERTY_NAME, KEY_VAULT_NAME);

        MutablePropertySources propertySources = mock();
        try (var processor = new AzureKeyVaultPropertiesProcessor();
             var annotationConfigApplicationContextConstruction = mockConstruction(
                     AnnotationConfigApplicationContext.class,
                     (mock, context) -> {
                         ConfigurableEnvironment configurableEnvironment = mock();
                         when(mock.getEnvironment()).thenReturn(configurableEnvironment);

                         when(configurableEnvironment.getPropertySources()).thenReturn(propertySources);

                         SecretClient secretClient = mock();
                         when(mock.getBean(SecretClient.class)).thenReturn(secretClient);

                         KeyVaultSecret keyVaultSecret = mock();
                         when(secretClient.getSecret(PROPERTY_VALUE)).thenReturn(keyVaultSecret);
                         when(keyVaultSecret.getValue()).thenReturn(KEY_VAULT_VALUE);
                     }))
        {
            assertEquals(KEY_VAULT_VALUE, processor.processValue(PROPERTY_NAME, PROPERTY_VALUE));

            assertThat(annotationConfigApplicationContextConstruction.constructed(), hasSize(1));
            var context = annotationConfigApplicationContextConstruction.constructed().get(0);

            var ordered = inOrder(context, propertySources);
            ordered.verify(propertySources).addFirst(
                    argThat(source -> KEY_VAULT_NAME.equals(source.getProperty(KEY_VAULT_PROPERTY_NAME)))
            );
            ordered.verify(context).refresh();
            ordered.verify(context).getBean(SecretClient.class);
        }
    }
}

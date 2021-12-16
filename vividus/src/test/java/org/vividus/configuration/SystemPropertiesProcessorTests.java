/*
 * Copyright 2019-2021 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemPropertiesProcessorTests
{
    @Mock
    private PropertiesDecryptor propertiesDecryptor;

    @InjectMocks
    private SystemPropertiesProcessor systemPropertiesProcessor;

    @Test
    void shouldProcessSystemPropertyWithDecryption()
    {
        String systemPropertyKey = "system.SYSTEM_PROPERTY";
        String systemPropertyValue = "value1";
        String propertyKey = "key";
        String value2 = "value2";
        Properties properties = new Properties();
        properties.setProperty(systemPropertyKey, systemPropertyValue);
        properties.setProperty(propertyKey, value2);
        when(propertiesDecryptor.decrypt(systemPropertyValue)).thenReturn(systemPropertyValue);
        Properties systemPropertiesExtracted = systemPropertiesProcessor.process(properties);
        verify(propertiesDecryptor).decrypt(systemPropertyValue);
        assertEquals(systemPropertyValue, System.getProperty(systemPropertyKey.substring(7)));
        assertNull(systemPropertiesExtracted.getProperty(systemPropertyKey));
        assertEquals(value2, systemPropertiesExtracted.getProperty(propertyKey));
    }
}

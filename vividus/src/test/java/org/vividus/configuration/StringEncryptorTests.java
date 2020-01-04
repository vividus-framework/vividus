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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StandardPBEStringEncryptor.class)
public class StringEncryptorTests
{
    private static final String PROPERTY_KEY_FIRST = "key first";
    private static final String PROPERTY_KEY_SECOND = "key second";
    private static final String PROPERTY_VALUE_FIRST = "value first";
    private static final String PROPERTY_VALUE_SECOND = "value second";
    private static final String PROPERTY_VALUE_SECOND_ENCRYPTED = "ENC(value second)";

    @Test
    public void testDecryptProperties()
    {
        StandardPBEStringEncryptor standardPBEStringEncryptor = mock(StandardPBEStringEncryptor.class);
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_KEY_FIRST, PROPERTY_VALUE_FIRST);
        properties.setProperty(PROPERTY_KEY_SECOND, PROPERTY_VALUE_SECOND_ENCRYPTED);
        when(standardPBEStringEncryptor.decrypt(PROPERTY_VALUE_SECOND)).thenReturn(PROPERTY_VALUE_SECOND);
        Properties propertiesDecrypted = StringEncryptor.decryptProperties(standardPBEStringEncryptor, properties);
        assertEquals(properties.getProperty(PROPERTY_KEY_FIRST), propertiesDecrypted.getProperty(PROPERTY_KEY_FIRST));
        assertEquals(PROPERTY_VALUE_SECOND, propertiesDecrypted.getProperty(PROPERTY_KEY_SECOND));
    }
}

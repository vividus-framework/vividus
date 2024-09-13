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

package org.vividus.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Properties;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.encryption.DecryptionFailedException;
import org.vividus.encryption.Decryptor;
import org.vividus.encryption.NoDecryptionPasswordException;

class EncryptedPropertiesProcessorTests
{
    private static final String ENCRYPTOR_PASSWORD_PROPERTY = "system.vividus.encryptor.password";
    private static final String PASSWORD = "I test VIVIDUS encryption features";

    private static final String PROPERTY_KEY = "key";
    private static final String ENCRYPTED_VALUE = "KixcztydWWB6e3EYz4tzZg==";
    private static final String ENCRYPTED_VALUE_PLACEHOLDER = "ENC(" + ENCRYPTED_VALUE + ")";
    private static final String DECRYPTED_VALUE = "hello";

    @ParameterizedTest
    @CsvSource({
            ENCRYPTED_VALUE_PLACEHOLDER + ", " + DECRYPTED_VALUE,
            "username=open password=" + ENCRYPTED_VALUE_PLACEHOLDER + " secret-key=" + ENCRYPTED_VALUE_PLACEHOLDER
                    + ", username=open password=" + DECRYPTED_VALUE + " secret-key=" + DECRYPTED_VALUE
    })
    void shouldDecryptProperties(String encryptedValue, String decryptedValue)
    {
        var properties = new Properties();
        properties.setProperty(ENCRYPTOR_PASSWORD_PROPERTY, PASSWORD);
        properties.setProperty(PROPERTY_KEY, encryptedValue);
        var propertiesDecrypted = new EncryptedPropertiesProcessor().processProperties(properties);
        assertEquals(decryptedValue, propertiesDecrypted.getProperty(PROPERTY_KEY));
    }

    static List<Object> propertiesToDoNotDecrypt()
    {
        return List.of(
                "any",
                new Object()
        );
    }

    @ParameterizedTest
    @MethodSource("propertiesToDoNotDecrypt")
    void shouldKeepNonStringPropertiesAsIs(Object propertyValue)
    {
        try (var stringDecryptorMockedConstruction = mockConstruction(Decryptor.class))
        {
            var properties = new Properties();
            properties.put(PROPERTY_KEY, propertyValue);
            var decryptedProperties = new EncryptedPropertiesProcessor().processProperties(properties);
            assertEquals(propertyValue, decryptedProperties.get(PROPERTY_KEY));
            assertThat(stringDecryptorMockedConstruction.constructed(), hasSize(1));
            verifyNoInteractions(stringDecryptorMockedConstruction.constructed().get(0));
        }
    }

    @ParameterizedTest
    @CsvSource({
            ENCRYPTED_VALUE + ", invalid password",
            "invalidvalue, " + PASSWORD
    })
    void shouldThrowExceptionOnInvalidValues(String value, String password)
    {
        var properties = new Properties();
        properties.setProperty(PROPERTY_KEY, String.format("ENC(%s)", value));
        properties.setProperty(ENCRYPTOR_PASSWORD_PROPERTY, password);
        var processor = new EncryptedPropertiesProcessor();
        var exception = assertThrows(DecryptionFailedException.class, () -> processor.processProperties(properties));
        assertEquals("Unable to decrypt the value '" + value + "' from the property with the name 'key'",
                exception.getMessage());
        var cause = exception.getCause();
        assertEquals(EncryptionOperationNotPossibleException.class, cause.getClass());
        assertNull(cause.getMessage());
    }

    @Test
    void shouldFailToProcessEncryptedPropertiesWithoutEncryptorPassword()
    {
        var properties = new Properties();
        properties.setProperty(PROPERTY_KEY, ENCRYPTED_VALUE_PLACEHOLDER);
        var processor = new EncryptedPropertiesProcessor();
        var exception = assertThrows(DecryptionFailedException.class, () -> processor.processProperties(properties));
        assertEquals("Encrypted properties are found, but no password for decryption is provided",
                exception.getMessage());
        var cause = exception.getCause();
        assertEquals(NoDecryptionPasswordException.class, cause.getClass());
        assertEquals("No password for decryption is provided", cause.getMessage());
    }
}

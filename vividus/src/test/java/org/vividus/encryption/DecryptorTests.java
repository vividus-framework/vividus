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

package org.vividus.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.junitpioneer.jupiter.SetSystemProperty;

class DecryptorTests
{
    private static final String PASSWORD = "I test VIVIDUS Decryptor";
    private static final String ENCRYPTED_VALUE = "jt8KvrtlsTJ4NYyKp5zpww==";
    private static final String DECRYPTED_VALUE = "hello";

    @Test
    @SetEnvironmentVariable(key = "VIVIDUS_ENCRYPTOR_PASSWORD", value = PASSWORD)
    @SetSystemProperty(key = "vividus.encryptor.password", value = "invalid_pass")
    void shouldDecryptProperties()
    {
        var actual = new Decryptor(new Properties()).decrypt(ENCRYPTED_VALUE);
        assertEquals(DECRYPTED_VALUE, actual);
    }

    @ParameterizedTest
    @CsvSource({
            ENCRYPTED_VALUE + ", invalid password",
            "invalidvalue, " + PASSWORD
    })
    void shouldThrowExceptionOnInvalidValues(String value, String password)
    {
        var properties = new Properties();
        properties.setProperty("system.vividus.encryptor.password", password);
        var decryptor = new Decryptor(properties);
        assertThrows(EncryptionOperationNotPossibleException.class, () -> decryptor.decrypt(value));
    }

    @Test
    void shouldFailToProcessEncryptedPropertiesWithoutEncryptorPassword()
    {
        var properties = new Properties();
        var decryptor = new Decryptor(properties);
        var exception = assertThrows(NoDecryptionPasswordException.class, () -> decryptor.decrypt(ENCRYPTED_VALUE));
        assertEquals("No password for decryption is provided", exception.getMessage());
    }
}

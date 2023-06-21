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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Properties;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class EncryptedAbstractPropertiesProcessorTests
{
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final String PASSWORD = "I test VIVIDUS encryption features";
    private static final String PROPERTY_KEY = "key";
    private static final String ENCRYPTED_VALUE = "KixcztydWWB6e3EYz4tzZg==";
    private static final String ENCRYPTED_VALUE_PLACEHOLDER = "ENC(" + ENCRYPTED_VALUE + ")";
    private static final String DECRYPTED_VALUE = "hello";
    private static final String PROHIBITED_DEFAULT_PASSWORD_MESSAGE = "Usage of default VIVIDUS password for "
            + "encryption of sensitive data is prohibited. The default VIVIDUS encryption password will be removed in"
            + " VIVIDUS 0.6.0";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(EncryptedPropertiesProcessor.class);

    private StandardPBEStringEncryptor createEncryptor()
    {
        return createEncryptor(PASSWORD);
    }

    private StandardPBEStringEncryptor createEncryptor(String password)
    {
        var standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setAlgorithm(ALGORITHM);
        standardPBEStringEncryptor.setPassword(password);
        return standardPBEStringEncryptor;
    }

    @ParameterizedTest
    @CsvSource({
            ENCRYPTED_VALUE_PLACEHOLDER + ", " + DECRYPTED_VALUE,
            "username=open password=" + ENCRYPTED_VALUE_PLACEHOLDER + " secret-key=" + ENCRYPTED_VALUE_PLACEHOLDER
                    + ", username=open password=" + DECRYPTED_VALUE + " secret-key=" + DECRYPTED_VALUE
    })
    void shouldDecryptProperties(String encryptedValue, String decryptedValue)
    {
        var properties = new Properties();
        properties.setProperty(PROPERTY_KEY, encryptedValue);
        var propertiesDecrypted = new EncryptedPropertiesProcessor(createEncryptor()).processProperties(properties);
        assertEquals(decryptedValue, propertiesDecrypted.getProperty(PROPERTY_KEY));
        assertThat(logger.getLoggingEvents(), is(empty()));
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
        var standardPBEStringEncryptor = mock(StandardPBEStringEncryptor.class);

        var properties = new Properties();
        properties.put(PROPERTY_KEY, propertyValue);
        var decryptedProperties = new EncryptedPropertiesProcessor(standardPBEStringEncryptor).processProperties(
                properties);
        assertEquals(propertyValue, decryptedProperties.get(PROPERTY_KEY));
        verifyNoInteractions(standardPBEStringEncryptor);
        assertThat(logger.getLoggingEvents(), is(empty()));
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
        var decryptor = new EncryptedPropertiesProcessor(createEncryptor(password));
        var exception = assertThrows(DecryptionFailedException.class, () -> decryptor.processProperties(properties));
        assertEquals("Unable to decrypt the value '" + value + "' from the property with the name 'key'",
                exception.getMessage());
        var cause = exception.getCause();
        assertEquals(EncryptionOperationNotPossibleException.class, cause.getClass());
        assertNull(cause.getMessage());
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldCreateDefaultEncryptor()
    {
        var properties = new Properties();
        properties.setProperty(PROPERTY_KEY, "ENC(tWkyTEdA8p0UbcV0UA5GXw==)");
        var propertiesDecrypted = new EncryptedPropertiesProcessor(properties).processProperties(properties);
        assertEquals("Ķekava", propertiesDecrypted.getProperty(PROPERTY_KEY));
        assertThat(logger.getLoggingEvents(), is(List.of(warn(PROHIBITED_DEFAULT_PASSWORD_MESSAGE))));
    }
}

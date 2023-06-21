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

import java.util.Optional;
import java.util.Properties;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncryptedPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedPropertiesProcessor.class);

    private static final String ENC = "ENC";
    private Properties properties;
    private StringEncryptor stringEncryptor;

    EncryptedPropertiesProcessor(StringEncryptor stringEncryptor)
    {
        super(ENC);
        this.stringEncryptor = stringEncryptor;
    }

    EncryptedPropertiesProcessor(Properties properties)
    {
        super(ENC);
        this.properties = properties;
    }

    @Override
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        try
        {
            return getStringEncryptor().decrypt(partOfPropertyValueToProcess);
        }
        catch (EncryptionOperationNotPossibleException e)
        {
            String errorMessage = String.format("Unable to decrypt the value '%s' from the property with the name '%s'",
                    partOfPropertyValueToProcess, propertyName);
            throw new DecryptionFailedException(errorMessage, e);
        }
    }

    @SuppressWarnings("checkstyle:TodoComment")
    private StringEncryptor getStringEncryptor()
    {
        if (stringEncryptor == null)
        {
            // TODO: change the precedence in 0.6.0: env var should have the highest priority, then system properties
            String password = Optional.ofNullable(System.getProperty("vividus.encryptor.password"))
                    .or(() -> Optional.ofNullable(properties.getProperty("system.vividus.encryptor.password")))
                    .or(() -> Optional.ofNullable(System.getenv("VIVIDUS_ENCRYPTOR_PASSWORD")))
                    .orElseGet(() -> {
                        LOGGER.warn(
                                "Usage of default VIVIDUS password for encryption of sensitive data is prohibited. "
                                        + "The default VIVIDUS encryption password will be removed in VIVIDUS 0.6.0");
                        return "82=thuMUH@";
                    });

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setAlgorithm("PBEWithMD5AndDES");
            encryptor.setPassword(password);

            stringEncryptor = encryptor;
        }
        return stringEncryptor;
    }
}

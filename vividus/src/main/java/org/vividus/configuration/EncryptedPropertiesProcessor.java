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

import java.util.Properties;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.vividus.encryption.DecryptionFailedException;
import org.vividus.encryption.Decryptor;
import org.vividus.encryption.NoDecryptionPasswordException;

public class EncryptedPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final String ENC = "ENC";
    private Decryptor decryptor;

    public EncryptedPropertiesProcessor()
    {
        super(ENC);
    }

    @Override
    public Properties processProperties(Properties properties)
    {
        this.decryptor = new Decryptor(properties);
        return super.processProperties(properties);
    }

    @Override
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        try
        {
            return decryptor.decrypt(partOfPropertyValueToProcess);
        }
        catch (NoDecryptionPasswordException e)
        {
            throw new DecryptionFailedException(
                    "Encrypted properties are found, but no password for decryption is provided", e);
        }
        catch (EncryptionOperationNotPossibleException e)
        {
            String errorMessage = String.format("Unable to decrypt the value '%s' from the property with the name '%s'",
                    partOfPropertyValueToProcess, propertyName);
            throw new DecryptionFailedException(errorMessage, e);
        }
    }
}

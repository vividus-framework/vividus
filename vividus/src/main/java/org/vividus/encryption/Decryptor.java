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

package org.vividus.encryption;

import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

public class Decryptor
{
    private final LazyInitializer<StringEncryptor> encryptorProvider;

    public Decryptor(Properties properties)
    {
        this.encryptorProvider = LazyInitializer.<StringEncryptor>builder().setInitializer(() -> {
            String password = Optional.ofNullable(System.getenv("VIVIDUS_ENCRYPTOR_PASSWORD"))
                    .or(() -> Optional.ofNullable(System.getProperty("vividus.encryptor.password")))
                    .or(() -> Optional.ofNullable(properties.getProperty("system.vividus.encryptor.password")))
                    .orElseThrow(() -> new NoDecryptionPasswordException("No password for decryption is provided"));

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setAlgorithm("PBEWithMD5AndDES");
            encryptor.setPassword(password);
            return encryptor;
        }).get();
    }

    /**
     * Decrypts an encrypted string.
     *
     * @param encrypted the encrypted string to be decrypted
     * @return the result of decryption
     * @throws NoDecryptionPasswordException           if no password for decryption is provided
     * @throws EncryptionOperationNotPossibleException if decryption is failed
     */
    public String decrypt(String encrypted)
    {
        try
        {
            return encryptorProvider.get().decrypt(encrypted);
        }
        catch (ConcurrentException e)
        {
            throw new IllegalStateException(e);
        }
    }
}

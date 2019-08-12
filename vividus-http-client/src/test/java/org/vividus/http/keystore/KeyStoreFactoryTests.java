/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.http.keystore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KeyStoreFactoryTests
{
    private static final String PASSWORD = "password";
    private static final String JKS_PATH = "/org/vividus/http/keystore/client.jks";
    private static final String CA = "CA";
    private static final String JKS = "JKS";

    private final KeyStoreFactory keyStoreFactory = new KeyStoreFactory();

    @BeforeEach
    void before()
    {
        keyStoreFactory.setKeyStoreType(JKS);
    }

    @Test
    void testGetKeyStore()
    {
        keyStoreFactory.setKeyStorePath(JKS_PATH);
        keyStoreFactory.setKeyStorePassword(PASSWORD);
        assertEquals(keyStoreFactory.getKeyStore(), keyStoreFactory.getKeyStore());
    }

    @Test
    void testGetCachedKeyStore() throws GeneralSecurityException
    {
        keyStoreFactory.setKeyStorePath(JKS_PATH);
        keyStoreFactory.setKeyStorePassword(PASSWORD);

        Optional<KeyStore> optionalkeyStore = keyStoreFactory.getKeyStore();

        assertTrue(optionalkeyStore.isPresent());
        KeyStore keyStore = optionalkeyStore.get();
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate("alias");
        String subjectFormat = "EMAILADDRESS=at@testers.com, CN=AT, OU=Automation Team %s, O=Test "
                + "Automation, L=Minsk, ST=BLR, C=BY";
        String actualSubject = certificate.getSubjectDN().getName();
        String actualIssuer = certificate.getIssuerDN().getName();
        assertEquals(String.format(subjectFormat, "CERT"), actualSubject);
        assertEquals(String.format(subjectFormat, CA), actualIssuer);
    }

    @Test
    void testGetKeyStoreException()
    {
        keyStoreFactory.setKeyStorePath(JKS_PATH);
        keyStoreFactory.setKeyStorePassword(null);
        Exception exception = assertThrows(IllegalStateException.class, keyStoreFactory::getKeyStore);
        assertEquals(String.format("Key store password for %s %s must not be null", JKS, JKS_PATH),
                exception.getMessage());
    }

    @Test
    void testGetKeyStoreEmptyPath()
    {
        keyStoreFactory.setKeyStorePath(null);
        Optional<KeyStore> optionalkeyStore = keyStoreFactory.getKeyStore();
        assertFalse(optionalkeyStore.isPresent());
    }
}

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

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;

public final class PropertiesDecryptor
{
    private static final Pattern ENCRYPTED_PROPERTY_PATTERN = Pattern.compile("ENC\\((.+?)\\)");

    private PropertiesDecryptor()
    {
    }

    public static Properties decryptProperties(StringEncryptor stringEncryptor, Properties properties)
    {
        for (Map.Entry<Object, Object> entry : properties.entrySet())
        {
            Object value = entry.getValue();
            if (value instanceof String)
            {
                String decryptedValue = (String) value;
                Matcher matcher = ENCRYPTED_PROPERTY_PATTERN.matcher(decryptedValue);
                while (matcher.find())
                {
                    String encryptedPartOfValue = matcher.group(1);
                    String decryptedPartOfValue = stringEncryptor.decrypt(encryptedPartOfValue);
                    decryptedValue = decryptedValue.replace("ENC(" + encryptedPartOfValue + ")", decryptedPartOfValue);
                }
                entry.setValue(decryptedValue);
            }
        }
        return properties;
    }
}

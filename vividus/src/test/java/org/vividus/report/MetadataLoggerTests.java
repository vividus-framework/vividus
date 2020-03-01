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

package org.vividus.report;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Properties;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(TestLoggerFactoryExtension.class)
public class MetadataLoggerTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MetadataLogger.class);
    private static final String VALUE = "value";
    private static final String FORMAT = "{}={}";

    @ParameterizedTest
    @ValueSource(strings = {
            "secure.access-key",
            "secure.admin-password",
            "secure.api-key",
            "secure.access-token",
            "secure.secret-phrase"
    })
    void testLogPropertiesSecurely(String key)
    {
        MetadataLogger.logPropertiesSecurely(createProperties(key, VALUE));
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info(FORMAT, key, "****"))));
    }

    @Test
    void testLogPropertiesSecurelyPlainProperties()
    {
        String key = "simple.ignore-failure";
        MetadataLogger.logPropertiesSecurely(createProperties(key, VALUE));
        assertThat(LOGGER.getLoggingEvents(), is(List.of(info(FORMAT, key, VALUE))));
    }

    private static Properties createProperties(String key, String value)
    {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }
}

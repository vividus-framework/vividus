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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.junit.jupiter.api.Test;

class PropertiesProcessorTests
{
    private static final String PROCESSOR_ENABLED_PROPERTY = "secrets-manager.enabled";
    private static final String KEY = "key";
    private static final String TRUE = "true";

    @Test
    void shouldNotProcessPropertiesWhenProcessorDisabled()
    {
        var value = "value";
        var processor = new TestProcessor();
        Properties properties = new Properties();
        properties.put(KEY, value);
        assertEquals(value, processor.processProperties(properties).get(KEY));
    }

    @Test
    void shouldProcessStringPropertyWhenProcessorEnabled()
    {
        var processor = new TestProcessor();
        Properties properties = new Properties();
        properties.put(PROCESSOR_ENABLED_PROPERTY, TRUE);
        properties.put(KEY, "SECRET_MANAGER(value)");
        assertEquals("VALUE", processor.processProperties(properties).get(KEY));
    }

    @Test
    void shouldSkipProcessingOfPropertyWhenProcessorEnabled()
    {
        var value = 123;
        var processor = new TestProcessor();
        Properties properties = new Properties();
        properties.put(PROCESSOR_ENABLED_PROPERTY, TRUE);
        properties.put(KEY, value);
        assertEquals(value, processor.processProperties(properties).get(KEY));
    }

    static class TestProcessor extends AbstractPropertiesProcessor
    {
        protected TestProcessor()
        {
            super("SECRET_MANAGER");
        }

        @Override
        protected boolean isEnabled(Properties properties)
        {
            return Boolean.parseBoolean(properties.getProperty(PROCESSOR_ENABLED_PROPERTY));
        }

        @Override
        protected String processValue(String propertyName, String partOfPropertyValueToProcess)
        {
            return partOfPropertyValueToProcess.toUpperCase();
        }
    }
}

/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.exporter.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.vividus.util.property.PropertyParser;

@ExtendWith(MockitoExtension.class)
class VividusExporterCommonConfigurationTests
{
    private static final String VALUE_1 = "value 1";
    private static final String PROPERTY_1 = "property 1";
    private static final String PROPERTY_2 = "property 2";
    private static final String VALUE_2 = "value 2";

    private final VividusExporterCommonConfiguration commonConfiguration = new VividusExporterCommonConfiguration();

    @Test
    void testVerifyPropertyParser()
    {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(PROPERTY_1, VALUE_1);
        environment.setProperty(PROPERTY_2, VALUE_2);

        PropertyParser propertyParser = commonConfiguration.propertyParser(environment);

        assertEquals(propertyParser.getPropertyValue(PROPERTY_1), VALUE_1);
        assertEquals(propertyParser.getPropertyValue(PROPERTY_2), VALUE_2);
    }
}

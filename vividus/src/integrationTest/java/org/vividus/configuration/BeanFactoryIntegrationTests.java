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

package org.vividus.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.BeanIsAbstractException;

class BeanFactoryIntegrationTests
{
    private static final String CONFIGURATION_PROFILE = "configuration.profile";
    private static final String ENVIRONMENTS_PROPERTY = "configuration.environments";

    private static final String BASIC_ENV = "basicenv";
    private static final String ADDITIONAL_ENV = "additionalenv/props";

    private static final String PROPERTY_VALUE_FROM_SUITE = "value-from-suite";

    @BeforeEach
    void beforeEach()
    {
        resetBeanFactory();
    }

    @AfterEach
    void afterEach()
    {
        resetBeanFactory();
    }

    @ParameterizedTest(name = "{0} profile")
    @ValueSource(strings = {
            "",
            "web/desktop/chrome",
            "web/phone/iphone",
            "web/tablet/ipad",
            "web/desktop/chrome/mobile_emulation/phone",
            "web/desktop/chrome/mobile_emulation/tablet",
            "web/desktop/chrome/mobile_emulation/responsive"
    })
    void testBeanFactory(String profile)
    {
        System.setProperty(CONFIGURATION_PROFILE, profile);
        System.setProperty(ENVIRONMENTS_PROPERTY, "integrationtest");
        BeanFactory.open();
        for (String beanName : BeanFactory.getBeanDefinitionNames())
        {
            try
            {
                BeanFactory.getBean(beanName).hashCode();
            }
            catch (@SuppressWarnings("unused") BeanIsAbstractException e)
            {
                // ignored
            }
        }
    }

    @Test
    void testConfigurationResolverMultipleEnvironments() throws IOException
    {
        System.setProperty(ENVIRONMENTS_PROPERTY, BASIC_ENV + "," + ADDITIONAL_ENV);
        BeanFactory.open();
        assertProperties("additionalenv-props-property-value", "additionalenv-property-value");
    }

    @Test
    void testConfigurationResolverEnvironmentsIsNotSet()
    {
        Exception exception = assertThrows(IllegalStateException.class, BeanFactory::open);
        assertEquals("environments is not set", exception.getMessage());
    }

    @Test
    void testConfigurationResolverSuiteOverridesEnvironments() throws Exception
    {
        System.setProperty(ENVIRONMENTS_PROPERTY, BASIC_ENV);
        BeanFactory.open();
        assertSuiteProperty("env-suite-overridable-property", PROPERTY_VALUE_FROM_SUITE);
    }

    @Test
    void testConfigurationResolverSuiteOverridesProfile() throws Exception
    {
        System.setProperty(ENVIRONMENTS_PROPERTY, BASIC_ENV);
        BeanFactory.open();
        assertSuiteProperty("profile-suite-overridable-property", PROPERTY_VALUE_FROM_SUITE);
    }

    @Test
    void testConfigurationResolverDeeperSuiteOverridesUpperSuite() throws Exception
    {
        System.setProperty(ENVIRONMENTS_PROPERTY, BASIC_ENV);
        System.setProperty("configuration.suite", "integration");
        BeanFactory.open();
        assertSuiteProperty("deeper-suite-level-overridable-property", "value-from-deeper-level");
    }

    private void assertSuiteProperty(String key, String expectedValue) throws IOException
    {
        Properties properties = ConfigurationResolver.getInstance().getProperties();
        assertEquals(expectedValue, properties.getProperty(key));
    }

    private void assertProperties(String additionalValueFirst, String additionalValueSecond) throws IOException
    {
        Properties properties = ConfigurationResolver.getInstance().getProperties();
        assertEquals("basicenv-property-value", properties.getProperty("basicenv-property"));
        assertEquals("root-property-value", properties.getProperty("root-property"));
        assertEquals("basicenv-property-overriden-root-value", properties.getProperty("basicenv-property-overriden"));
        assertEquals(additionalValueFirst, properties.getProperty("additionalenv-props-property"));
        assertEquals(additionalValueSecond, properties.getProperty("additionalenv-property"));
        assertEquals("override-property-value", properties.getProperty("override-property"));
    }

    private void resetBeanFactory()
    {
        Stream.of(CONFIGURATION_PROFILE, ENVIRONMENTS_PROPERTY).forEach(System::clearProperty);
        BeanFactory.reset();
        ConfigurationResolver.reset();
    }
}

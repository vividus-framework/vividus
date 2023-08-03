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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith(MockitoExtension.class)
class ConfigurationResolverTests
{
    private static final String DESKTOP_CHROME = "desktop/chrome";
    private static final String CONFIGURATION = "configuration";
    private static final String PROPERTY_1 = "property1";
    private static final String PROPERTY_2 = "property2";
    private static final String PROPERTY_3 = "property3";
    private static final String PROPERTY_4 = "property4";
    private static final String PROPERTY_5 = "property5";
    private static final String PROPERTY_6 = "property6";
    private static final String PROPERTY_7 = "property7";
    private static final String VIVIDUS_UTIL_PROPERTY_1 = "vividus.util.property1";
    private static final String VIVIDUS_UTIL_PROPERTY_2 = "vividus.util.property2";
    private static final String OVERRIDING = "overriding";
    private static final String DEFAULT = "default";
    private static final String HTTP_CLIENT_PROPERTY_1 = "http.client.property1";
    private static final String HTTP_CLIENT_PROPERTY_2 = "http.client.property2";
    private static final String CONFIGURATION_PROFILES = "configuration.profiles";
    private static final String CONFIGURATION_ENVIRONMENTS = "configuration.environments";
    private static final String CONFIGURATION_SUITES = "configuration.suites";
    private static final String DEFAULTS = "defaults";
    private static final String ENCRYPTED = "encrypted";
    private static final String ROOT = "root";
    private static final String ENVIRONMENTS = "environments";
    private static final String SUITES = "suites";
    private static final String UAT = "uat";
    private static final String ACTIVE_PROFILES = "desktop/chrome,nosecurity";
    private static final String HTTP_CLIENT_DEFAULT = "org/vividus/http/client";
    private static final String UTIL_DEFAULT = "org/vividus/util";
    private static final String PROFILE = "profile";
    private static final String ENVIRONMENT = "environment";
    private static final String SUITE = "suite";
    private static final String DEPRECATED = "deprecated";
    private static final String EMPTY_STRING = "";
    private static final String NOSECURITY = "nosecurity";
    private static final String PROPERTY_8 = "property8";
    private static final String CONFIGURATION_SET_PROPERTY_KEY = "configuration-set.active";
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String SET = "set";
    private static final String CONFIGURATION_PROPERTIES = "configuration.properties";

    @Mock private ResourcePatternResolver resourcePatternResolver;

    @Test
    void shouldLoadProperties() throws IOException
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
             var newDeprecatedPropertiesHandler = mockConstruction(DeprecatedPropertiesHandler.class,
                 withSettings().useConstructor(new Properties(), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX));
             var newPropertiesLoaders = mockConstruction(PropertiesLoader.class,
                 withSettings().useConstructor(resourcePatternResolver), (mock, context) -> {
                     when(mock.loadConfigurationProperties()).thenReturn(toProperties(
                         Map.of(CONFIGURATION_PROFILES, ACTIVE_PROFILES,
                                 CONFIGURATION_ENVIRONMENTS, UAT,
                                 CONFIGURATION_SUITES, "smoke",
                                 PROPERTY_1, CONFIGURATION,
                                 PROPERTY_2, CONFIGURATION,
                                 PROPERTY_3, CONFIGURATION,
                                 PROPERTY_4, CONFIGURATION,
                                 PROPERTY_5, CONFIGURATION,
                                 PROPERTY_6, CONFIGURATION,
                                 VIVIDUS_UTIL_PROPERTY_2, CONFIGURATION)));

                     when(mock.loadOverridingProperties()).thenReturn(toProperties(
                         Map.of(CONFIGURATION_SUITES, EMPTY_STRING,
                                 PROPERTY_1, OVERRIDING,
                                 HTTP_CLIENT_PROPERTY_1, OVERRIDING)));

                     when(mock.loadDefaultProperties(HTTP_CLIENT_DEFAULT)).thenReturn(toProperties(Map.of(
                             HTTP_CLIENT_PROPERTY_1, DEFAULT,
                             HTTP_CLIENT_PROPERTY_2, DEFAULT
                     )));

                     when(mock.loadDefaultProperties(UTIL_DEFAULT)).thenReturn(toProperties(Map.of(
                             VIVIDUS_UTIL_PROPERTY_1, DEFAULT,
                             VIVIDUS_UTIL_PROPERTY_2, DEFAULT
                     )));

                     when(mock.loadFromResourceTreeRecursively(true, DEFAULTS)).thenReturn(toProperties(
                         Map.of(PROPERTY_2, DEFAULTS,
                                PROPERTY_3, DEFAULTS,
                                PROPERTY_8, DEFAULTS)));

                     when(mock.loadFromResourceTreeRecursively(eq(true), any(Predicate.class),
                         eq(EMPTY_STRING))).thenReturn(toProperties(
                             Map.of(PROPERTY_3, ROOT,
                                    ENCRYPTED, "ENC(0owj4MADNEoWpkeE22n5bFfkjEfaA4Tv)")));

                     when(mock.loadFromResourceTreeRecursively(true, PROFILE, NOSECURITY))
                         .thenReturn(toProperties(Map.of(PROPERTY_4, NOSECURITY,
                                                         PROPERTY_5, NOSECURITY,
                                                         PROPERTY_6, NOSECURITY,
                                                         PROPERTY_7, NOSECURITY)));

                     when(mock.loadFromResourceTreeRecursively(true, PROFILE, DESKTOP_CHROME))
                                  .thenReturn(toProperties(Map.of(PROPERTY_5, DESKTOP_CHROME,
                                                                  PROPERTY_6, DESKTOP_CHROME,
                                                                  PROPERTY_7, DESKTOP_CHROME)));

                     when(mock.loadFromResourceTreeRecursively(true, ENVIRONMENT, UAT))
                         .thenReturn(toProperties(Map.of(PROPERTY_5, ENVIRONMENTS,
                                                         PROPERTY_6, ENVIRONMENTS)));

                     when(mock.loadFromResourceTreeRecursively(false, SUITE, EMPTY_STRING))
                             .thenReturn(toProperties(Map.of(PROPERTY_6, SUITES)));

                     when(mock.loadFromResourceTreeRecursively(false, DEPRECATED)).thenReturn(new Properties());
                 }))
        {
            beanFactory.when(BeanFactory::getResourcePatternResolver).thenReturn(resourcePatternResolver);
            ConfigurationResolver.getInstance();
            var propertiesLoader = verifySingeConstruction(newPropertiesLoaders);
            var deprecatedPropertiesHandler = verifySingeConstruction(newDeprecatedPropertiesHandler);
            var ordered = Mockito.inOrder(propertiesLoader, deprecatedPropertiesHandler);
            ordered.verify(propertiesLoader).loadConfigurationProperties();
            ordered.verify(propertiesLoader).loadOverridingProperties();
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(false, DEPRECATED);
            ordered.verify(propertiesLoader).prohibitConfigurationProperties();
            ordered.verify(propertiesLoader).loadDefaultProperties(HTTP_CLIENT_DEFAULT);
            ordered.verify(propertiesLoader).loadDefaultProperties(UTIL_DEFAULT);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, DEFAULTS);
            var resource = mock(Resource.class);
            when(resource.getFilename()).thenReturn(CONFIGURATION_PROPERTIES);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(eq(true),
                argThat((ArgumentMatcher<Predicate<Resource>>) argument -> !argument.test(resource)), eq(EMPTY_STRING));
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, PROFILE, NOSECURITY);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, PROFILE, DESKTOP_CHROME);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, ENVIRONMENT, UAT);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(false, SUITE, EMPTY_STRING);
            ordered.verify(deprecatedPropertiesHandler).replaceDeprecated(any(Properties.class));
            ordered.verify(deprecatedPropertiesHandler).replaceDeprecated(any(Properties.class), any(Properties.class));
            ordered.verify(deprecatedPropertiesHandler, atLeast(13)).warnIfDeprecated(any(String.class),
                    any(Object.class));
            ordered.verify(deprecatedPropertiesHandler).removeDeprecated(any(Properties.class));
            verifyNoMoreInteractions(deprecatedPropertiesHandler, propertiesLoader);
            var configurationResolver = ConfigurationResolver.getInstance();
            var properties = configurationResolver.getProperties();
            assertAll(
                () -> assertEquals("Поехали!",      properties.getProperty(ENCRYPTED)),
                () -> assertEquals(OVERRIDING,      properties.getProperty(PROPERTY_1)),
                () -> assertEquals(OVERRIDING,      properties.getProperty(HTTP_CLIENT_PROPERTY_1)),
                () -> assertEquals(DEFAULT,         properties.getProperty(HTTP_CLIENT_PROPERTY_2)),
                () -> assertEquals(DEFAULT,         properties.getProperty(VIVIDUS_UTIL_PROPERTY_1)),
                () -> assertEquals(CONFIGURATION,   properties.getProperty(VIVIDUS_UTIL_PROPERTY_2)),
                () -> assertEquals(CONFIGURATION,   properties.getProperty(PROPERTY_2)),
                () -> assertEquals(ROOT,            properties.getProperty(PROPERTY_3)),
                () -> assertEquals(NOSECURITY,      properties.getProperty(PROPERTY_4)),
                () -> assertEquals(ENVIRONMENTS,    properties.getProperty(PROPERTY_5)),
                () -> assertEquals(SUITES,          properties.getProperty(PROPERTY_6)),
                () -> assertEquals(DESKTOP_CHROME,  properties.getProperty(PROPERTY_7)),
                () -> assertEquals(DEFAULTS,        properties.getProperty(PROPERTY_8)),
                () -> assertEquals(EMPTY_STRING,    properties.getProperty(CONFIGURATION_SUITES)),
                () -> assertEquals(ACTIVE_PROFILES, properties.getProperty(CONFIGURATION_PROFILES)),
                () -> assertEquals(UAT,             properties.getProperty(CONFIGURATION_ENVIRONMENTS)));
            ConfigurationResolver.reset();
            var ise = assertThrows(IllegalStateException.class, configurationResolver::getProperties);
            assertEquals("ConfigurationResolver has not been initialized after the reset", ise.getMessage());
        }
    }

    @Test
    void shouldUseConfigurationSet() throws IOException
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
                var newDeprecatedPropertiesHandler = mockConstruction(DeprecatedPropertiesHandler.class,
                        withSettings().useConstructor(new Properties(), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX));
                var newPropertiesLoaders = mockConstruction(PropertiesLoader.class,
                        withSettings().useConstructor(resourcePatternResolver), (mock, context) -> {
                            var emptyProperties = toProperties(Map.of());
                            when(mock.loadConfigurationProperties()).thenReturn(toProperties(
                                    Map.of(CONFIGURATION_PROFILES, ACTIVE_PROFILES,
                                            CONFIGURATION_ENVIRONMENTS, UAT,
                                            CONFIGURATION_SUITES, "regression")));

                            when(mock.loadOverridingProperties()).thenReturn(toProperties(
                                    Map.of(CONFIGURATION_SET_PROPERTY_KEY, "test",
                                            "configuration-set.test.profiles", SET,
                                            "configuration-set.test.suites", SET,
                                            "configuration-set.test.environments", SET)));

                            when(mock.loadDefaultProperties(HTTP_CLIENT_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadDefaultProperties(UTIL_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, DEFAULTS)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(eq(true), any(Predicate.class),
                                    eq(EMPTY_STRING))).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, PROFILE, SET))
                                    .thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, ENVIRONMENT, SET))
                                    .thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, SUITE, SET))
                                    .thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(false, DEPRECATED)).thenReturn(new Properties());
                        }))
        {
            beanFactory.when(BeanFactory::getResourcePatternResolver).thenReturn(resourcePatternResolver);
            ConfigurationResolver.getInstance();
            var propertiesLoader = verifySingeConstruction(newPropertiesLoaders);
            var deprecatedPropertiesHandler = verifySingeConstruction(newDeprecatedPropertiesHandler);
            var ordered = Mockito.inOrder(propertiesLoader, deprecatedPropertiesHandler);
            ordered.verify(propertiesLoader).loadConfigurationProperties();
            ordered.verify(propertiesLoader).loadOverridingProperties();
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(false, DEPRECATED);
            ordered.verify(propertiesLoader).prohibitConfigurationProperties();
            ordered.verify(propertiesLoader).loadDefaultProperties(HTTP_CLIENT_DEFAULT);
            ordered.verify(propertiesLoader).loadDefaultProperties(UTIL_DEFAULT);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, DEFAULTS);
            var resource = mock(Resource.class);
            when(resource.getFilename()).thenReturn(CONFIGURATION_PROPERTIES);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(eq(true),
                argThat((ArgumentMatcher<Predicate<Resource>>) argument -> !argument.test(resource)), eq(EMPTY_STRING));
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, PROFILE, SET);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, ENVIRONMENT, SET);
            ordered.verify(propertiesLoader).loadFromResourceTreeRecursively(true, SUITE, SET);
        }
    }

    @Test
    void shouldThrowAnExceptionInCaseOfEmptyConfigurationSet()
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
                var newDeprecatedPropertiesHandler = mockConstruction(DeprecatedPropertiesHandler.class,
                        withSettings().useConstructor(new Properties(), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX));
                var newPropertiesLoaders = mockConstruction(PropertiesLoader.class,
                        withSettings().useConstructor(resourcePatternResolver), (mock, context) -> {
                            var emptyProperties = toProperties(Map.of());
                            when(mock.loadConfigurationProperties()).thenReturn(toProperties(
                                    Map.of(CONFIGURATION_SET_PROPERTY_KEY, "")));

                            when(mock.loadOverridingProperties()).thenReturn(emptyProperties);

                            when(mock.loadDefaultProperties(HTTP_CLIENT_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadDefaultProperties(UTIL_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, DEFAULTS)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(eq(true), any(Predicate.class),
                                    eq(EMPTY_STRING))).thenReturn(emptyProperties);
                        }))
        {
            beanFactory.when(BeanFactory::getResourcePatternResolver).thenReturn(resourcePatternResolver);
            var ise = assertThrows(IllegalStateException.class, ConfigurationResolver::getInstance);
            assertEquals("Property 'configuration-set.active' must be not blank.", ise.getMessage());
        }
    }

    @Test
    void shouldThrowAnExceptionIfConfigurationSetNotDefined()
    {
        try (var beanFactory = mockStatic(BeanFactory.class);
                var newDeprecatedPropertiesHandler = mockConstruction(DeprecatedPropertiesHandler.class,
                        withSettings().useConstructor(new Properties(), PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX));
                var newPropertiesLoaders = mockConstruction(PropertiesLoader.class,
                        withSettings().useConstructor(resourcePatternResolver), (mock, context) -> {
                            var emptyProperties = toProperties(Map.of());
                            when(mock.loadConfigurationProperties()).thenReturn(toProperties(
                                    Map.of(CONFIGURATION_SET_PROPERTY_KEY, "active")));

                            when(mock.loadOverridingProperties()).thenReturn(emptyProperties);

                            when(mock.loadDefaultProperties(HTTP_CLIENT_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadDefaultProperties(UTIL_DEFAULT)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(true, DEFAULTS)).thenReturn(emptyProperties);

                            when(mock.loadFromResourceTreeRecursively(eq(true), any(Predicate.class),
                                    eq(EMPTY_STRING))).thenReturn(emptyProperties);
                        }))
        {
            beanFactory.when(BeanFactory::getResourcePatternResolver).thenReturn(resourcePatternResolver);
            var ise = assertThrows(IllegalStateException.class, ConfigurationResolver::getInstance);
            assertEquals("The 'configuration-set.active.profiles' property is not set", ise.getMessage());
        }
    }

    private <T> T verifySingeConstruction(MockedConstruction<T> toVerify)
    {
        var beans = toVerify.constructed();
        assertThat(beans, hasSize(1));
        return beans.get(0);
    }

    private Properties toProperties(Map<String, String> map)
    {
        var properties = new Properties();
        properties.putAll(map);
        return properties;
    }
}

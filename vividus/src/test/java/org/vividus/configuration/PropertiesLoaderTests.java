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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class PropertiesLoaderTests
{
    private static final String CONFIG_LOCATION = "classpath*:/properties/configuration.properties";
    private static final String OVERRIDING_LOCATION = "classpath:/overriding.properties";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String LOADING_PROPERTIES_FROM = "Loading properties from /{}";
    private static final String PROPS_PATTERN = "{}=={}";
    private static final String ROOT_PROPERTIES = "classpath*:/properties/profiles/*.properties";
    private static final String PROFILES = "profiles";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(PropertiesLoader.class);

    @Mock private ResourcePatternResolver resourcePatternResolver;

    @InjectMocks private PropertiesLoader propertiesLoader;

    @Test
    void shouldLoadProperties() throws IOException
    {
        var resource = mock(Resource.class);
        mockResources(CONFIG_LOCATION, resource);
        var properties = new Properties();
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
            PropertiesFactoryBean.class, (mock, context) -> when(mock.getObject()).thenReturn(properties)))
        {
            assertSame(properties, propertiesLoader.loadConfigurationProperties());
            validatePropertiesLoading(mockedConstruction, resource);
        }
    }

    private void validatePropertiesLoading(MockedConstruction<PropertiesFactoryBean> mockedConstruction,
            Resource resource)
    {
        List<PropertiesFactoryBean> beans = mockedConstruction.constructed();
        assertThat(beans, hasSize(1));
        PropertiesFactoryBean propertiesFactoryBean = beans.get(0);
        validateResourceLoading(resource, propertiesFactoryBean);
    }

    private void validateResourceLoading(Resource resource, PropertiesFactoryBean propertiesFactoryBean)
    {
        verify(propertiesFactoryBean).setFileEncoding(StandardCharsets.UTF_8.name());
        verify(propertiesFactoryBean).setSingleton(false);
        verify(propertiesFactoryBean).setLocation(resource);
    }

    @Test
    void shouldReturnEmptyPropertiesIfNoConfigurationResourcesFound() throws IOException
    {
        mockResources(CONFIG_LOCATION);
        assertTrue(propertiesLoader.loadConfigurationProperties().isEmpty());
    }

    @Test
    void shouldThrowAnExceptionInCaseOfMultipleConfigurationFiles() throws IOException
    {
        var resource = mock(Resource.class);
        mockResources(CONFIG_LOCATION, resource, resource);
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, propertiesLoader::loadConfigurationProperties);
        assertEquals("Exactly one resource is expected: classpath*:/properties/configuration.properties, but found: 2",
            exception.getMessage());
    }

    @Test
    void shouldLoadOverridingProperties() throws IOException
    {
        var resource = mock(Resource.class);
        when(resourcePatternResolver.getResource(OVERRIDING_LOCATION)).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        var properties = new Properties();
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
            PropertiesFactoryBean.class, (mock, context) -> when(mock.getObject()).thenReturn(properties)))
        {
            assertSame(properties, propertiesLoader.loadOverridingProperties());
            validatePropertiesLoading(mockedConstruction, resource);
        }
    }

    @Test
    void shouldReturnEmptyOverridingProperties() throws IOException
    {
        var resource = mock(Resource.class);
        when(resourcePatternResolver.getResource(OVERRIDING_LOCATION)).thenReturn(resource);
        Properties properties = propertiesLoader.loadOverridingProperties();
        assertTrue(properties.isEmpty());
    }

    @Test
    void shouldCollectDefaultProperties() throws IOException
    {
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(true);
        mockResources("classpath*:org/vividus/util/defaults.properties", resource);
        var properties = new Properties();
        properties.put(KEY, VALUE);
        propertiesLoader.prohibitConfigurationProperties();
        var resourcePath = "org/vividus/util";
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
            PropertiesFactoryBean.class, (mock, context) -> when(mock.getObject()).thenReturn(properties)))
        {
            assertEquals(properties, propertiesLoader.loadDefaultProperties(resourcePath));
            validatePropertiesLoading(mockedConstruction, resource);
        }
        assertEquals(List.of(LoggingEvent.info(LOADING_PROPERTIES_FROM, resourcePath),
                LoggingEvent.debug(PROPS_PATTERN, KEY, VALUE)), LOGGER.getLoggingEvents());
        verifyNoMoreInteractions(resourcePatternResolver);
    }

    @Test
    void shouldCollectPropertiesRecursively() throws IOException
    {
        var web = mock(Resource.class);
        when(web.exists()).thenReturn(true);
        var chrome = mock(Resource.class);
        when(chrome.exists()).thenReturn(true);
        mockResources(ROOT_PROPERTIES);
        mockResources("classpath*:/properties/profiles/web/*.properties", web);
        mockResources("classpath*:/properties/profiles/web/desktop/*.properties");
        mockResources("classpath*:/properties/profiles/web/desktop/chrome/*.properties", chrome);

        AtomicInteger index = new AtomicInteger();
        var pfb = "pfb";
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
                PropertiesFactoryBean.class, (mock, context) -> {
                    var properties = new Properties();
                    properties.put(pfb, index.getAndIncrement());
                    when(mock.getObject()).thenReturn(properties);
                }))
        {
            var expected = new Properties();
            expected.put(pfb, 1);
            assertEquals(expected, propertiesLoader.loadFromResourceTreeRecursively(false, PROFILES,
                "web/desktop/chrome"));
            List<PropertiesFactoryBean> beans = mockedConstruction.constructed();
            assertThat(beans, hasSize(2));
            validateResourceLoading(web, beans.get(0));
            validateResourceLoading(chrome, beans.get(1));
        }
        assertEquals(List.of(LoggingEvent.info(LOADING_PROPERTIES_FROM, "profiles/web/desktop/chrome"),
                LoggingEvent.debug(PROPS_PATTERN, pfb, 1)), LOGGER.getLoggingEvents());
        verifyNoMoreInteractions(resourcePatternResolver);
    }

    @Test
    void shouldCollectPropertiesRecursivelyAndFilterResources() throws IOException
    {
        var resource1 = mock(Resource.class);
        when(resource1.exists()).thenReturn(true);
        var resource2 = mock(Resource.class);
        when(resource1.exists()).thenReturn(true);
        mockResources(ROOT_PROPERTIES, resource1, resource2);
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
                PropertiesFactoryBean.class))
        {
            assertEquals(new Properties(), propertiesLoader.loadFromResourceTreeRecursively(false,
                r -> false, PROFILES, ""));
            List<PropertiesFactoryBean> beans = mockedConstruction.constructed();
            assertThat(beans, hasSize(0));
        }
        assertEquals(List.of(LoggingEvent.info(LOADING_PROPERTIES_FROM, "profiles/")), LOGGER.getLoggingEvents());
        verifyNoMoreInteractions(resourcePatternResolver);
    }

    @Test
    void shouldFailIfPropertiesNotFoundAndTheyWereExpected() throws IOException
    {
        mockResources("classpath*:/properties/environments/*.properties");
        mockResources("classpath*:/properties/environments/release/*.properties");
        mockResources("classpath*:/properties/environments/release/2.0/*.properties");

        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> propertiesLoader.loadFromResourceTreeRecursively(true,
                "environments", "release/2.0"));
        assertEquals("No files with properties were found at location with pattern:"
                    + " classpath*:/properties/environments/release/2.0/*.properties", exception.getMessage());
        assertEquals(List.of(), LOGGER.getLoggingEvents());
        verifyNoMoreInteractions(resourcePatternResolver);
    }

    @Test
    void shouldFailIfConfigurationPropertiesLoadedFromInvalidLocation() throws IOException
    {
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(true);
        when(resource.toString()).thenReturn("default.properties");
        mockResources("classpath*:org/defaults.properties", resource);
        var properties = new Properties();
        properties.put("configuration-set.active", "qa-prod-no_ui");
        properties.put("configuration.suites", "qa,preprod,prod");
        properties.put("configuration.profiles", "web,mobile,no_ui");
        propertiesLoader.prohibitConfigurationProperties();
        var location = "org";
        try (MockedConstruction<PropertiesFactoryBean> mockedConstruction = mockConstruction(
                PropertiesFactoryBean.class, (mock, context) -> {
                    when(mock.getObject()).thenReturn(properties);
                }))
        {
            var ise = assertThrows(IllegalStateException.class,
                () -> propertiesLoader.loadDefaultProperties(location));
            assertEquals(
                  "The configuration.* and configuration-set.* properties can be set using:"
                + " System properties, overriding.properties file or configuration.properties file."
                + " But found: [configuration-set.active, configuration.suites, configuration.profiles];"
                + " In resource: default.properties", ise.getMessage());
            validatePropertiesLoading(mockedConstruction, resource);
        }
        assertEquals(List.of(LoggingEvent.info(LOADING_PROPERTIES_FROM, location)), LOGGER.getLoggingEvents());
        verifyNoMoreInteractions(resourcePatternResolver);
    }

    private void mockResources(String path, Resource... resources) throws IOException
    {
        when(resourcePatternResolver.getResources(path)).thenReturn(resources);
    }
}

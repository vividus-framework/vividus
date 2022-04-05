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

package org.vividus.issue;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.vividus.softassert.issue.KnownIssueType;
import org.vividus.util.ResourceUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class KnownIssueProviderTests
{
    private static final String URL = "http://examples.com";
    private static final String MAIN_PAGE_URL = "mainPageUrl";
    private static final String FILENAME = "known-issues.json";
    private static final String LOCATION_PATTERN = CLASSPATH_ALL_URL_PREFIX + FILENAME;
    private static final String PROPERTY_PREFIX = "known-issue-provider.additional-parameters-%s";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(KnownIssueProvider.class);

    @Mock private ApplicationContext applicationContext;
    @Mock private IPropertyParser propertyParser;
    @InjectMocks private final KnownIssueProvider knownIssueProvider = new KnownIssueProvider();

    private static VividusKnownIssueIdentifier getKnownIssueIdentifier(KnownIssueType type, String assertionPattern)
    {
        VividusKnownIssueIdentifier knownIssueIdentifier = new VividusKnownIssueIdentifier();
        knownIssueIdentifier.setType(type);
        if (assertionPattern != null)
        {
            knownIssueIdentifier.setAssertionPattern(assertionPattern);
        }
        knownIssueIdentifier.setStoryPattern("Story.*");
        knownIssueIdentifier.setScenarioPattern("Scenario 1.*");
        knownIssueIdentifier.setStepPattern("Given.*");
        knownIssueIdentifier.setVariablePatterns(Map.of("var-1", "value-1-.*"));
        return knownIssueIdentifier;
    }

    private void initKnownIssueProvider(KnownIssueType type, String assertionPattern)
    {
        VividusKnownIssueIdentifier knownIssueIdentifier = getKnownIssueIdentifier(type, assertionPattern);
        Map<String, VividusKnownIssueIdentifier> knownIssueIdentifiers = new HashMap<>();
        knownIssueIdentifiers.put("KEY-1", knownIssueIdentifier);
        knownIssueProvider.setKnownIssueIdentifiers(knownIssueIdentifiers);
        knownIssueProvider.init();
    }

    @Test
    void testExceptionOnKnownIssueIdentifierWithMissingFields()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> initKnownIssueProvider(null, null));
        assertEquals("[Field \"type\" of known issue with key KEY-1 is null, Field \"assertionPattern\" of "
                + "known issue with key KEY-1 is null]", exception.getMessage());
    }

    @Test
    void testNoExceptionOnKnownIssueIdentifierWithoutMissingFields()
    {
        initKnownIssueProvider(KnownIssueType.EXTERNAL, "Assertion.*");
    }

    @Test
    void testInit() throws IOException
    {
        knownIssueProvider.setFileName(FILENAME);
        knownIssueProvider.setKnownIssueIdentifiers(new HashMap<>());
        try (var knownIssues = IOUtils.toInputStream(
                ResourceUtils.loadResource(KnownIssueProviderTests.class, FILENAME), StandardCharsets.UTF_8))
        {
            var resource = mock(Resource.class);
            when(resource.getDescription()).thenReturn(FILENAME);
            when(resource.getInputStream()).thenReturn(knownIssues);
            when(applicationContext.getResources(CLASSPATH_ALL_URL_PREFIX + FILENAME)).thenReturn(
                    new Resource[] { resource });
            when(propertyParser.getPropertyValue(PROPERTY_PREFIX, MAIN_PAGE_URL)).thenReturn(MAIN_PAGE_URL);
            when(propertyParser.getPropertyValue(MAIN_PAGE_URL)).thenReturn(URL);

            knownIssueProvider.init();

            assertEquals(1, knownIssueProvider.getKnownIssueIdentifiers().size());
        }
    }

    @Test
    void testInitNoResource() throws IOException
    {
        knownIssueProvider.setFileName(FILENAME);
        knownIssueProvider.setKnownIssueIdentifiers(new HashMap<>());
        when(applicationContext.getResources(CLASSPATH_ALL_URL_PREFIX + FILENAME)).thenReturn(new Resource[] {});

        knownIssueProvider.init();

        verifyNoInteractions(propertyParser);
        assertThat(logger.getLoggingEvents(), is(List.of(warn(
                "Known issue functionality is not available. No resource is found by location pattern: {}",
                LOCATION_PATTERN))));
    }

    @ParameterizedTest
    @ValueSource(strings = MAIN_PAGE_URL)
    @NullSource
    void testInitNoPropertyByQualifier(String propertyValue) throws IOException
    {
        knownIssueProvider.setFileName(FILENAME);
        knownIssueProvider.setKnownIssueIdentifiers(new HashMap<>());
        try (InputStream knownIssues = IOUtils.toInputStream(
                ResourceUtils.loadResource(KnownIssueProviderTests.class, FILENAME), StandardCharsets.UTF_8))
        {
            Resource resource = mock(Resource.class);
            when(resource.getDescription()).thenReturn(FILENAME);
            when(resource.getInputStream()).thenReturn(knownIssues);
            when(applicationContext.getResources(CLASSPATH_ALL_URL_PREFIX + FILENAME)).thenReturn(
                    new Resource[] { resource });
            when(propertyParser.getPropertyValue(PROPERTY_PREFIX, MAIN_PAGE_URL)).thenReturn(MAIN_PAGE_URL);
            when(propertyParser.getPropertyValue(MAIN_PAGE_URL)).thenReturn(propertyValue);

            knownIssueProvider.init();

            assertEquals(0, knownIssueProvider.getKnownIssueIdentifiers().size());
            var events = logger.getLoggingEvents();
            assertEquals(2, events.size());
            assertEquals(debug("Loading known issue identifiers from {}", FILENAME), events.get(0));
            var info = events.get(1);
            assertEquals("Issue with key {} filtered out by additional pattern '{}'. Actual property value is '{}'",
                    info.getMessage());
            assertEquals("ISSUE-123", info.getArguments().get(0));
            assertEquals(".*examples.com", info.getArguments().get(1).toString());
        }
    }

    @Test
    void testInitIOException() throws IOException
    {
        knownIssueProvider.setFileName(FILENAME);
        knownIssueProvider.setKnownIssueIdentifiers(new HashMap<>());
        IOException ioe = new IOException();
        when(applicationContext.getResources(CLASSPATH_ALL_URL_PREFIX + FILENAME))
            .thenThrow(ioe);

        knownIssueProvider.init();

        verifyNoInteractions(propertyParser);
        assertThat(logger.getLoggingEvents(), is(List.of(error(ioe, "Unable to load known issue identifiers"))));
    }
}

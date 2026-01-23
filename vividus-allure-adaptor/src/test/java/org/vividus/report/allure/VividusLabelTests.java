/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.report.allure;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.SetSystemProperty;

import io.qameta.allure.SeverityLevel;
import io.qameta.allure.model.Link;

@ExtendWith(TestLoggerFactoryExtension.class)
class VividusLabelTests
{
    private static final String ISSUE_ID = "issueId";
    private static final String PRIORITY = "priority";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(VividusLabel.class);

    private Meta createMeta(String metaKey, String metaValue)
    {
        return createMeta(Map.of(metaKey, metaValue));
    }

    private Meta createMeta(Map<String, String> map)
    {
        var properties = new Properties();
        properties.putAll(map);
        return new Meta(properties);
    }

    @Test
    void shouldExtractMetaValues()
    {
        var storyMeta = createMeta(ISSUE_ID, "VVD-1;VVD-2");
        var scenarioMeta = createMeta(ISSUE_ID, "VVD-3; VVD-4");
        var metaValues = VividusLabel.ISSUE_ID.extractMetaValues(storyMeta, scenarioMeta);
        assertThat(metaValues, contains(issue("VVD-1"), issue("VVD-2"), issue("VVD-3"), issue("VVD-4")));
    }

    @Test
    void shouldExtractMetaValuesWithDifferentSuffixes()
    {
        var issueIdDev = "issueId.dev";
        var storyMeta = createMeta(issueIdDev, "VVD-6;VVD-7");
        var issueIdProd = "issueId.prod";
        var scenarioMeta = createMeta(issueIdProd, "VVD-8; VVD-9");
        var metaValues = VividusLabel.ISSUE_ID.extractMetaValues(storyMeta, scenarioMeta);
        assertThat(metaValues, contains(entry(issueIdDev, "VVD-6"), entry(issueIdDev, "VVD-7"),
                entry(issueIdProd, "VVD-8"), entry(issueIdProd, "VVD-9")));
    }

    private Entry<String, String> issue(String key)
    {
        return entry(ISSUE_ID, key);
    }

    @Test
    void shouldExtractSeverity()
    {
        var metaKey = "severity";
        var storyMeta = createMeta(metaKey, "1");
        var scenarioMeta = createMeta(metaKey, "2");
        var metaValues = VividusLabel.SEVERITY.extractMetaValues(storyMeta, scenarioMeta);
        assertEquals(metaValues, Set.of(entry(metaKey, "critical")));
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                warn("Meta @severity is deprecated and will be removed in VIVIDUS 0.7.0. Please use @priority meta "
                     + "instead"))));
    }

    @Test
    void shouldExtractPriority()
    {
        var storyMeta = createMeta(PRIORITY, "3");
        var scenarioMeta = createMeta(PRIORITY, "4");
        var metaValues = VividusLabel.PRIORITY.extractMetaValues(storyMeta, scenarioMeta);
        assertEquals(metaValues, Set.of(entry(PRIORITY, "minor")));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "10", "-1", "medium" })
    void shouldWarnIfExtractedPriorityIsInvalid(String invalidValue)
    {
        var scenarioMeta = createMeta(PRIORITY, invalidValue);
        var metaValues = VividusLabel.PRIORITY.extractMetaValues(Meta.EMPTY, scenarioMeta);
        assertThat(metaValues, is(empty()));
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(warn("The {} meta value must be a number in a range from 1 to {}, but got {}", PRIORITY,
                        SeverityLevel.values().length, invalidValue))));
    }

    @ParameterizedTest
    @CsvSource({
            "TEST_CASE_ID,   testCaseId",
            "ISSUE_ID,       issueId",
            "REQUIREMENT_ID, requirementId",
            "SEVERITY,       severity",
            "PRIORITY,       severity",
            "EPIC,           epic",
            "FEATURE,        feature"
    })
    void shouldCreateLabel(VividusLabel vividusLabel, String expectedName)
    {
        var value = "value";
        var label = vividusLabel.createLabel(entry(expectedName, value));
        assertAll(
            () -> assertEquals(expectedName, label.getName()),
            () -> assertEquals(value, label.getValue())
        );
    }

    @ParameterizedTest
    @CsvSource({
            "TEST_CASE_ID,   tms",
            "ISSUE_ID,       issue",
            "REQUIREMENT_ID, requirement"
    })
    @SetSystemProperty(key = "allure.link.tms.pattern", value = "https://tms/{}")
    @SetSystemProperty(key = "allure.link.issue.pattern", value = "https://issue/{}")
    @SetSystemProperty(key = "allure.link.requirement.pattern", value = "https://requirement/{}")
    void shouldCreateLink(VividusLabel vividusLabel, String type)
    {
        var identifier = "VVD-5";
        var actualLink = vividusLabel.createLink(entry(type, identifier));
        var expectedLink = new Link().setName(identifier).setType(type).setUrl("https://" + type + "/" + identifier);
        assertEquals(Optional.of(expectedLink), actualLink);
    }

    @Test
    @SetSystemProperty(key = "allure.link.issue.test.pattern", value = "https://vividus.dev/{}")
    void shouldCreateLinkUsingSuffix()
    {
        var identifier = "VVD-0";
        var actualLink = VividusLabel.ISSUE_ID.createLink(entry("issueId.test", identifier));
        var expectedLink = new Link().setName(identifier).setType("issue").setUrl("https://vividus.dev/" + identifier);
        assertEquals(Optional.of(expectedLink), actualLink);
    }

    @ParameterizedTest
    @EnumSource(names = {"SEVERITY", "PRIORITY", "EPIC", "FEATURE"}, mode = Mode.INCLUDE)
    void shouldNotCreateLink(VividusLabel vividusLabel)
    {
        assertEquals(Optional.empty(), vividusLabel.createLink(entry("type", "any")));
    }
}

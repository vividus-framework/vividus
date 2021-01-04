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

package org.vividus.bdd.report.allure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;

class VividusLabelTests
{
    private static final String HTTPS = "https://";

    private static final Map<String, String> LINK_TYPES = Stream.of("tms", "issue", "requirement")
            .collect(Collectors.toMap(type -> "allure.link." + type + ".pattern", type -> HTTPS + type + "/{}"));

    private static final String SEVERITY = "severity";

    @BeforeAll
    static void beforeAll()
    {
        LINK_TYPES.forEach(System::setProperty);
    }

    @AfterAll
    static void afterAll()
    {
        LINK_TYPES.keySet().forEach(System::clearProperty);
    }

    private Meta createMeta(String metaKey, String metaValue)
    {
        return createMeta(Map.of(metaKey, metaValue));
    }

    private Meta createMeta(Map<String, String> map)
    {
        Properties properties = new Properties();
        properties.putAll(map);
        return new Meta(properties);
    }

    @Test
    void shouldExtractMetaValues()
    {
        String metaKey = "issueId";
        Meta storyMeta = createMeta(metaKey, "VVD-1;VVD-2");
        Meta scenarioMeta = createMeta(metaKey, "VVD-3; VVD-4");
        Set<String> metaValues = VividusLabel.ISSUE_ID.extractMetaValues(storyMeta, scenarioMeta);
        assertThat(metaValues, contains("VVD-1", "VVD-2", "VVD-3", "VVD-4"));
    }

    @Test
    void shouldExtractSeverity()
    {
        String metaKey = SEVERITY;
        Meta storyMeta = createMeta(metaKey, "1");
        Meta scenarioMeta = createMeta(metaKey, "2");
        Set<String> metaValues = VividusLabel.SEVERITY.extractMetaValues(storyMeta, scenarioMeta);
        assertEquals(Set.of("critical"), metaValues);
    }

    @ParameterizedTest
    @CsvSource({
            "TEST_CASE_ID,   testCaseId",
            "ISSUE_ID,       issueId",
            "REQUIREMENT_ID, requirementId",
            "SEVERITY,       severity",
            "EPIC,           epic",
            "FEATURE,        feature"
    })
    void shouldCreateLabel(VividusLabel vividusLabel, String expectedName)
    {
        String value = "value";
        Label label = vividusLabel.createLabel(value);
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
    void shouldCreateLink(VividusLabel vividusLabel, String type)
    {
        String identifier = "VVD-5";
        Optional<Link> optionalLink = vividusLabel.createLink(identifier);
        assertTrue(optionalLink.isPresent());
        Link link = optionalLink.get();
        assertAll(
            () -> assertEquals(identifier, link.getName()),
            () -> assertEquals(type, link.getType()),
            () -> assertEquals(HTTPS + type + "/" + identifier, link.getUrl())
        );
    }

    @ParameterizedTest
    @EnumSource(names = {"SEVERITY", "EPIC", "FEATURE"}, mode = Mode.INCLUDE)
    void shouldNotCreateLink(VividusLabel vividusLabel)
    {
        assertEquals(Optional.empty(), vividusLabel.createLink("any"));
    }
}

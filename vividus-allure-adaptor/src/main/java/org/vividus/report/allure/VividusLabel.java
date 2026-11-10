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

import static java.util.Map.entry;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.model.MetaWrapper;

import io.qameta.allure.SeverityLevel;
import io.qameta.allure.entity.LabelName;
import io.qameta.allure.model.Label;
import io.qameta.allure.model.Link;
import io.qameta.allure.util.ResultsUtils;

public enum VividusLabel
{
    TEST_CASE_ID("testCaseId", Optional.of(ResultsUtils.TMS_LINK_TYPE)),
    ISSUE_ID("issueId", Optional.of(ResultsUtils.ISSUE_LINK_TYPE)),
    REQUIREMENT_ID("requirementId", Optional.of("requirement")),
    @Deprecated(since = "0.6.0", forRemoval = true)
    SEVERITY(LabelName.SEVERITY.value(), Optional.empty())
    {
        @Override
        public Set<Entry<String, String>> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
        {
            return extractSeverityMetaValues(getMetaName(), scenarioMeta);
        }
    },
    PRIORITY("priority", Optional.empty())
    {
        @Override
        public Set<Entry<String, String>> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
        {
            return extractSeverityMetaValues(getMetaName(), scenarioMeta);
        }

        @Override
        public Label createLabel(Entry<String, String> identifier)
        {
            return ResultsUtils.createSeverityLabel(identifier.getValue());
        }
    },
    EPIC(LabelName.EPIC.value(), Optional.empty()),
    FEATURE(LabelName.FEATURE.value(), Optional.empty());

    private static final Logger LOGGER = LoggerFactory.getLogger(VividusLabel.class);

    private static final String DOT = ".";

    private final String metaName;
    private final Optional<String> linkType;

    VividusLabel(String metaName, Optional<String> linkType)
    {
        this.metaName = metaName;
        this.linkType = linkType;
    }

    String getMetaName()
    {
        return metaName;
    }

    public Set<Entry<String, String>> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
    {
        return Stream.of(storyMeta, scenarioMeta)
                .map(MetaWrapper::new)
                .map(meta -> meta.getPropertiesByKey(k -> k.startsWith(metaName)).entrySet())
                .flatMap(Collection::stream)
                .flatMap(e -> MetaWrapper.parsePropertyValues(e.getValue()).stream().map(m -> entry(e.getKey(), m)))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @SuppressWarnings("NoNullForCollectionReturn")
    private static Set<Entry<String, String>> extractSeverityMetaValues(String metaName, Meta scenarioMeta)
    {
        return scenarioMeta.getOptionalProperty(metaName)
                .map(metaValue -> {
                    if (LabelName.SEVERITY.value().equals(metaName))
                    {
                        LOGGER.warn(
                                "Meta @severity is deprecated and will be removed in VIVIDUS 0.7.0. Please use "
                                + "@priority meta instead");
                    }

                    int levelsQty = SeverityLevel.values().length;
                    String severityMatcher = "[1-%d]{1}".formatted(levelsQty);
                    if (!metaValue.matches(severityMatcher))
                    {
                        LOGGER.warn("The {} meta value must be a number in a range from 1 to {}, but got {}",
                                metaName, levelsQty, metaValue);
                        return null;
                    }

                    return Integer.parseInt(metaValue);
                })
                .map(severity -> SeverityLevel.values()[severity - 1])
                .map(SeverityLevel::value)
                .map(s -> entry(metaName, s))
                .map(Set::of)
                .orElseGet(Set::of);
    }

    public Optional<Link> createLink(Entry<String, String> identifier)
    {
        return linkType.map(type -> ResultsUtils.createLink(identifier.getValue(),
                null, null, buildLinkType(identifier, type)).setType(type));
    }

    private String buildLinkType(Entry<String, String> identifier, String type)
    {
        String suffix = StringUtils.substringAfter(identifier.getKey(), DOT);
        return suffix.isEmpty() ? type : type + DOT + suffix;
    }

    public Label createLabel(Entry<String, String> identifier)
    {
        return ResultsUtils.createLabel(metaName, identifier.getValue());
    }
}

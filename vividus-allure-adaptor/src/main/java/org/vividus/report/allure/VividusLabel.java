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
    SEVERITY(LabelName.SEVERITY.value(), Optional.empty())
    {
        @Override
        public Set<Entry<String, String>> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
        {
            return scenarioMeta.getOptionalProperty(getMetaName())
                    .map(Integer::parseInt)
                    .map(severity -> SeverityLevel.values()[severity - 1])
                    .map(SeverityLevel::value)
                    .map(s -> entry(getMetaName(), s))
                    .map(Set::of)
                    .orElseGet(Set::of);
        }
    },
    EPIC(LabelName.EPIC.value(), Optional.empty()),
    FEATURE(LabelName.FEATURE.value(), Optional.empty());

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

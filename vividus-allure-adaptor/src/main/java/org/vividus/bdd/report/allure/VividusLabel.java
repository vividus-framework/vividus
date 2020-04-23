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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.model.Meta;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.model.MetaWrapper;

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
        public Set<String> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
        {
            String deprecatedMetaKey = "testTier";
            MetaWrapper metaWrapper = new MetaWrapper(scenarioMeta);
            Optional<String> deprecatedMeta = metaWrapper.getOptionalPropertyValue(deprecatedMetaKey);
            Optional<String> actualMeta = metaWrapper.getOptionalPropertyValue(getMetaName());

            if (deprecatedMeta.isPresent())
            {
                String message;
                if (actualMeta.isPresent())
                {
                    message = "Both deprecated '{}' and new '{}' meta are present, new meta is used, "
                            + "please, remove usage of the deprecated meta";
                }
                else
                {
                    message = "Deprecated meta found: '{}'. Use '{}' instead";
                    actualMeta = deprecatedMeta;
                }
                LoggerFactory.getLogger(VividusLabel.class)
                        .atWarn()
                        .addArgument(deprecatedMetaKey)
                        .addArgument(this::getMetaName)
                        .log(message);
            }

            return actualMeta
                    .map(Integer::parseInt)
                    .map(severity -> SeverityLevel.values()[severity - 1])
                    .map(SeverityLevel::value)
                    .map(Set::of)
                    .orElseGet(Set::of);
        }
    },
    EPIC(LabelName.EPIC.value(), Optional.empty()),
    FEATURE(LabelName.FEATURE.value(), Optional.empty());

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

    public Set<String> extractMetaValues(Meta storyMeta, Meta scenarioMeta)
    {
        return Stream.of(storyMeta, scenarioMeta)
                .map(MetaWrapper::new)
                .map(meta -> meta.getPropertyValues(metaName))
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Optional<Link> createLink(String identifier)
    {
        return linkType.map(type -> ResultsUtils.createLink(identifier, null, null, type));
    }

    public Label createLabel(String value)
    {
        return ResultsUtils.createLabel(metaName, value);
    }
}

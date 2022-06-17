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

package org.vividus.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;

public class ExamplesTableToApplitoolsVisualChecksConverter
    extends AbstractParameterConverter<ExamplesTable, List<ApplitoolsVisualCheck>>
{
    private final ApplitoolsVisualCheckFactory visualCheckFactory;

    public ExamplesTableToApplitoolsVisualChecksConverter(ApplitoolsVisualCheckFactory visualCheckFactory)
    {
        this.visualCheckFactory = visualCheckFactory;
    }

    @Override
    public List<ApplitoolsVisualCheck> convertValue(ExamplesTable toConvert, Type type)
    {
        return toConvert.getRowsAs(ApplitoolsVisualCheck.class)
                        .stream()
                        .peek(o -> {
                            checkNotNull(o.getBatchName(), "batchName");
                            checkNotNull(o.getBaselineName(), "baselineName");
                            checkNotNull(o.getAction(), "action");
                        })
                        .map(visualCheckFactory::unite)
                        .collect(Collectors.toList());
    }

    private <T> void checkNotNull(T toCheck, String fieldName)
    {
        Validate.isTrue(toCheck != null, fieldName + " should be set");
    }
}

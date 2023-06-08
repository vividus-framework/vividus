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

package org.vividus.converter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.steps.Parameters;
import org.vividus.accessibility.factory.AxeOptionsFactory;
import org.vividus.accessibility.model.axe.AxeCheckOptions;
import org.vividus.accessibility.model.axe.EnableableProperty;
import org.vividus.ui.action.ISearchActions;

public class ParametersToAxeCheckOptionsConverter extends AbstractAccessibilityCheckOptionsConverter<AxeCheckOptions>
{
    private final AxeOptionsFactory axeOptionsFactory;

    public ParametersToAxeCheckOptionsConverter(ISearchActions searchActions, AxeOptionsFactory axeOptionsFactory)
    {
        super(searchActions);
        this.axeOptionsFactory = axeOptionsFactory;
    }

    @Override
    public AxeCheckOptions convertValue(Parameters row, Type type)
    {
        String standard = getStandard(row, String.class);
        List<String> violationsToCheck = getViolationsToCheck(row);

        Validate.isTrue(standard != null || violationsToCheck != null,
                "Either 'standard' or 'violationsToCheck' must be set");

        Validate.isTrue(standard == null || violationsToCheck == null,
                "Either 'standard' or 'violationsToCheck' is allowed");

        List<String> violationsToIgnore = getViolationsToIgnore(row);
        if (violationsToCheck != null && !violationsToIgnore.isEmpty())
        {
            List<String> commonViolations = new ArrayList<>(violationsToCheck);
            commonViolations.retainAll(violationsToIgnore);
            Validate.isTrue(commonViolations.isEmpty(),
                "The following violations are specified as both to ignore and to check: %s",
                String.join(", ", commonViolations));
        }

        AxeCheckOptions checkOptions = new AxeCheckOptions();
        checkOptions.setRunOnly(axeOptionsFactory.createOptions(Pair.of(standard, violationsToCheck)));

        Map<String, EnableableProperty> ignores = violationsToIgnore.stream()
                .collect(Collectors.toMap(Function.identity(), v -> new EnableableProperty(false)));
        checkOptions.setRules(ignores);

        configureElements(row, checkOptions);
        return checkOptions;
    }
}

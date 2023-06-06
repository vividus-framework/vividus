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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.accessibility.model.htmlcs.AccessibilityStandard;
import org.vividus.accessibility.model.htmlcs.HtmlCsCheckOptions;
import org.vividus.accessibility.model.htmlcs.ViolationLevel;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.context.UiContext;

public class ParametersToAccessibilityCheckOptionsConverter
        extends AbstractAccessibilityCheckOptionsConverter<HtmlCsCheckOptions>
{
    private static final String LEVEL = "level";

    private static final String STANDARD = "standard";

    private final UiContext uiContext;

    public ParametersToAccessibilityCheckOptionsConverter(UiContext uiContext, ISearchActions searchActions)
    {
        super(searchActions);
        this.uiContext = uiContext;
    }

    @Override
    public HtmlCsCheckOptions convertValue(Parameters row, Type type)
    {
        AccessibilityStandard standardParameter = getStandard(row, AccessibilityStandard.class);
        checkNotNull(standardParameter, STANDARD);
        ViolationLevel level = row.valueAs(LEVEL, ViolationLevel.class, null);
        checkNotNull(level, LEVEL);

        List<String> ignore = new ArrayList<>(getViolationsToIgnore(row));
        Stream.of(ViolationLevel.values())
              .filter(l -> l.getCode() > level.getCode())
              .map(ViolationLevel::toString)
              .map(String::toLowerCase)
              .forEach(ignore::add);
        HtmlCsCheckOptions options = new HtmlCsCheckOptions(standardParameter);
        SearchContext context = uiContext.getSearchContext();
        if (context instanceof WebElement)
        {
            options.setRootElement(Optional.of((WebElement) context));
        }
        options.setIgnore(ignore);
        options.setInclude(getViolationsToCheck(row));
        configureElements(row, options);
        options.setLevel(level);
        return options;
    }

    private <T> void checkNotNull(T toCheck, String fieldName)
    {
        Validate.isTrue(toCheck != null, fieldName + " should be set");
    }
}

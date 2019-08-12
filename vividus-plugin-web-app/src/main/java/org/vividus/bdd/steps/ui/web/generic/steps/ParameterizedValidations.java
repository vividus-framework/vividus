/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.generic.steps;

import static org.vividus.ui.validation.matcher.WebElementMatchers.describingElementNumber;
import static org.vividus.ui.validation.matcher.WebElementMatchers.elementNumber;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;
import org.vividus.ui.web.action.ICssSelectorFactory;

public class ParameterizedValidations implements IParameterizedValidations
{
    private static final String ELEMENTS_MESSAGE = "The number of %s with specified parameters";

    @Inject private IDescriptiveSoftAssert descriptiveSoftAssert;
    @Inject private ICssSelectorFactory cssSelectorFactory;

    @Override
    public void assertNumber(String elementName, ComparisonRule comparisonRule, int number, List<WebElement> elements)
    {
        String description = String.format(ELEMENTS_MESSAGE, elementName);
        descriptiveSoftAssert.assertThat(description, description, elements,
                elementNumber(comparisonRule.getComparisonRule(number)));
    }

    @Override
    public void assertNumberWithLocationReporting(String elementName, ComparisonRule comparisonRule, int number,
            List<WebElement> elements)
    {
        String description = String.format(ELEMENTS_MESSAGE, elementName);
        Function<List<? extends WebElement>, String> elementsDescriptionProvider = e -> " CSS selectors:\n"
                + cssSelectorFactory.getCssSelectors(elements).collect(Collectors.joining("\n"));
        descriptiveSoftAssert.assertThat(description, elements,
                describingElementNumber(comparisonRule.getComparisonRule(number), elementsDescriptionProvider));
    }
}

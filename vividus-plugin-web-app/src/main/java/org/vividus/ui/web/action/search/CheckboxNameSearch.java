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

package org.vividus.ui.web.action.search;

import static org.vividus.ui.web.util.LocatorUtil.getXPathLocator;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.element.Checkbox;

public class CheckboxNameSearch extends AbstractElementSearchAction implements IElementSearchAction
{
    private static final String CHECKBOX_LOCATOR = "input[@type='checkbox']";
    private static final String PRECEDING_SIBLING_CHECKBOX_LOCATOR = "preceding-sibling::" + CHECKBOX_LOCATOR;
    private static final String CHECKBOX_LABEL_FORMAT = ".//label[text()='%s'"
            + " and (preceding-sibling::input or following-sibling::input or child::input)]";
    private static final String CHECKBOX_LABEL_DEEP = "label[preceding-sibling::input or following-sibling::input"
            + " or child::input]";

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        List<WebElement> checkboxLabels = searchCheckboxLabels(searchContext, parameters);
        return checkboxLabels.isEmpty() ? findElements(searchContext, getXPathLocator(CHECKBOX_LOCATOR), parameters)
                .stream()
                .filter(c -> parameters.getValue().equals(getWebElementActions().getElementText(c)))
                .map(Checkbox::new)
                .collect(Collectors.toList()) : checkboxLabels;
    }

    private List<WebElement> searchCheckboxLabels(SearchContext searchContext, SearchParameters parameters)
    {
        String checkBoxName = parameters.getValue();
        SearchParameters nonDisplayedParameters = new SearchParameters(parameters.getValue(), Visibility.ALL,
                parameters.isWaitForElement());

        List<WebElement> checkboxLabels = findElements(searchContext,
                getXPathLocator(String.format(CHECKBOX_LABEL_FORMAT, checkBoxName)), parameters);
        List<WebElement> matchedCheckboxLabels = searchCheckboxByLabels(searchContext, nonDisplayedParameters,
                checkboxLabels);
        if (matchedCheckboxLabels.isEmpty())
        {
            checkboxLabels = findElements(searchContext, getXPathLocator(CHECKBOX_LABEL_DEEP), parameters).stream()
                    .filter(e -> getWebElementActions().getElementText(e).contains(checkBoxName))
                    .collect(Collectors.toList());
            return searchCheckboxByLabels(searchContext, nonDisplayedParameters, checkboxLabels);
        }
        return matchedCheckboxLabels;
    }

    private List<WebElement> searchCheckboxByLabels(SearchContext searchContext, SearchParameters parameters,
            List<WebElement> labelElements)
    {
        for (WebElement label : labelElements)
        {
            List<WebElement> checkboxes;
            String checkBoxId = label.getAttribute("for");
            if (checkBoxId != null)
            {
                checkboxes = findElements(searchContext,
                        getXPathLocator(".//input[@type='checkbox' and @id=%s]", checkBoxId), parameters);
            }
            else
            {
                checkboxes = label.findElements(getXPathLocator(PRECEDING_SIBLING_CHECKBOX_LOCATOR));
                if (checkboxes.isEmpty())
                {
                    continue;
                }
            }
            return checkboxes.stream().map(e -> new Checkbox(e, label)).collect(Collectors.toList());
        }
        return List.of();
    }
}

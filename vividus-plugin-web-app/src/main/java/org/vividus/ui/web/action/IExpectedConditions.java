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

package org.vividus.ui.web.action;

import java.util.List;

import org.openqa.selenium.WebElement;

public interface IExpectedConditions<T>
{
    /**
     * An expectation for checking that is at least one element present
     * within the search context
     * @param searchCriteria used to find elements
     * @return the list of WebElements once they are located
     */
    IExpectedSearchContextCondition<List<WebElement>> presenceOfAllElementsLocatedBy(T searchCriteria);

    /**
     * An expectation for checking if the given text is present in the found element.
     * @param searchCriteria used to find elements
     * @param text to be present in the found element
     * @return true once the first element located with searchAttributes contains the given text.
     * Null if StaleElementReferenceException caught.
     */
    IExpectedSearchContextCondition<Boolean> textToBePresentInElementLocated(T searchCriteria,
            String text);

    /**
     * An expectation for checking that all found elements present within the search context are
     * visible. Visibility means that elements are not
     * only displayed but also have a height and width that is greater than 0.
     * @param searchCriteria used to find elements
     * @return the list of WebElements once they are located.
     * Null if StaleElementReferenceException caught.
     */
    IExpectedSearchContextCondition<List<WebElement>> visibilityOfAllElementsLocatedBy(T searchCriteria);

    /**
     * An expectation for checking that an element is present on the DOM of a page
     * and visible. Visibility means that the element is not only displayed but
     * also has a height and width that is greater than 0.
     * @param searchCriteria used to find elements
     * @return the WebElement once it is located and visible.
     * Null if StaleElementReferenceException caught.
     */
    IExpectedSearchContextCondition<WebElement> visibilityOfElement(T searchCriteria);

    /**
     * An expectation for checking an element is visible and enabled such that you
     * can click it.
     * @param searchCriteria used to find elements
     * @return the WebElement once it is located and clickable (visible and enabled).
     * Null if StaleElementReferenceException caught.
     */
    IExpectedSearchContextCondition<WebElement> elementToBeClickable(T searchCriteria);

    /**
     * An expectation for checking an element selection state
     * @param searchCriteria used to find elements
     * @param selected state to verify
     * @return true if element state matches verifiable state false otherwise
     * Null if StaleElementReferenceException caught.
     */
    IExpectedSearchContextCondition<Boolean> elementSelectionStateToBe(T searchCriteria,
            boolean selected);

    /**
     * An expectation for checking an element to be invisible
     * @param searchCriteria used to find elements
     * @return true if element is not displayed or present but invisible or
     * no longer visible.False otherwise.
     */
    IExpectedSearchContextCondition<Boolean> invisibilityOfElement(T searchCriteria);

    /**
     * An expectation with the logical opposite condition of the given condition.
     * Note that if the Condition your are inverting throws an exception that is
     * caught by the Ignored Exceptions, the inversion will not take place and lead
     * to confusing results.
     * @param condition Given condition
     * @return Output value with the logical opposite based on an input value.
     */
    IExpectedSearchContextCondition<Boolean> not(IExpectedSearchContextCondition<?> condition);

    /**
     * An expectation for checking drop-down's multi-select support.
     * This is done by checking the value of the "multiple" attribute.
     * @param searchCriteria used to find elements
     * @param multiSelect given condition
     * @return Output value with the logical opposite based on an input value.
     */
    IExpectedSearchContextCondition<Boolean> isMultiSelectDropDown(T searchCriteria, boolean multiSelect);
}

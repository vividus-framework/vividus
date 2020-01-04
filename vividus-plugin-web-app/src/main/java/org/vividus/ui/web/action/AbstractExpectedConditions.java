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

package org.vividus.ui.web.action;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AbstractExpectedConditions<T> implements IExpectedConditions<T>
{
    protected abstract List<WebElement> findElements(SearchContext searchContext, T searchCriteria);

    protected abstract WebElement findElement(SearchContext searchContext, T searchCriteria);

    protected abstract String toStringParameters(T searchCriteria);

    /**
     * An expectation for checking that is at least one element present
     * within the search context
     * @param searchCriteria used to find elements
     * @return the list of WebElements once they are located
     */
    @Override
    @SuppressWarnings("checkstyle:nonullforcollectionreturn")
    public IExpectedSearchContextCondition<List<WebElement>> presenceOfAllElementsLocatedBy(final T searchCriteria)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            public List<WebElement> apply(SearchContext searchContext)
            {
                List<WebElement> elements = findElements(searchContext, searchCriteria);
                return !elements.isEmpty() ? elements : null;
            }

            @Override
            public String toString()
            {
                return "presence of any elements " + toStringParameters(searchCriteria);
            }
        };
    }

    /**
     * An expectation for checking if the given text is present in the found element.
     * @param searchCriteria used to find elements
     * @param text to be present in the found element
     * @return true once the first element located with searchAttributes contains the given text.
     * Null if StaleElementReferenceException caught.
     */
    @Override
    public IExpectedSearchContextCondition<Boolean> textToBePresentInElementLocated(final T searchCriteria,
            final String text)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            @SuppressWarnings("checkstyle:returnnullinsteadofboolean")
            @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
            public Boolean apply(SearchContext searchContext)
            {
                try
                {
                    WebElement element = findElement(searchContext, searchCriteria);
                    return element.getText().contains(text);
                }
                catch (StaleElementReferenceException e)
                {
                    return null;
                }
            }

            @Override
            public String toString()
            {
                return String.format("text ('%s') to be present in element %s", text,
                        toStringParameters(searchCriteria));
            }
        };
    }

    /**
     * An expectation for checking that all found elements present within the search context are
     * visible. Visibility means that elements are not
     * only displayed but also have a height and width that is greater than 0.
     * @param searchCriteria used to find elements
     * @return the list of WebElements once they are located
     */
    @Override
    @SuppressWarnings("checkstyle:nonullforcollectionreturn")
    public IExpectedSearchContextCondition<List<WebElement>> visibilityOfAllElementsLocatedBy(final T searchCriteria)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            public List<WebElement> apply(SearchContext searchContext)
            {
                List<WebElement> elements = findElements(searchContext, searchCriteria);
                for (Iterator<WebElement> iterator = elements.iterator(); iterator.hasNext();)
                {
                    try
                    {
                        if (!iterator.next().isDisplayed())
                        {
                            return null;
                        }
                    }
                    catch (StaleElementReferenceException e)
                    {
                        iterator.remove();
                    }
                }
                return !elements.isEmpty() ? elements : null;
            }

            @Override
            public String toString()
            {
                return "visibility of all elements " + toStringParameters(searchCriteria);
            }
        };
    }

    /**
     * An expectation with the logical opposite condition of the given condition.
     * Note that if the Condition your are inverting throws an exception that is
     * caught by the Ignored Exceptions, the inversion will not take place and lead
     * to confusing results.
     * @param condition Given condition
     * @return Output value with the logical opposite based on an input value.
     */
    @Override
    public IExpectedSearchContextCondition<Boolean> not(final IExpectedSearchContextCondition<?> condition)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            public Boolean apply(SearchContext searchContext)
            {
                Object result = condition.apply(searchContext);
                return result == null || result.equals(Boolean.FALSE);
            }

            @Override
            public String toString()
            {
                return "condition to not be valid: " + condition;
            }
        };
    }

    /**
     * An expectation for checking that an element is present on the DOM of a page
     * and visible. Visibility means that the element is not only displayed but
     * also has a height and width that is greater than 0.
     * @param searchCriteria used to find elements
     * @return the WebElement once it is located and visible. Null if StaleElementReferenceException caught.
     */
    @Override
    public IExpectedSearchContextCondition<WebElement> visibilityOfElement(final T searchCriteria)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            public WebElement apply(SearchContext searchContext)
            {
                try
                {
                    WebElement element = findElement(searchContext, searchCriteria);
                    return element.isDisplayed() ? element : null;
                }
                catch (StaleElementReferenceException | NoSuchElementException e)
                {
                    return null;
                }
            }

            @Override
            public String toString()
            {
                return "visibility of element " + toStringParameters(searchCriteria);
            }
        };
    }

    /**
     * An expectation for checking an element is visible and enabled such that you
     * can click it.
     * @param searchCriteria used to find elements
     * @return the WebElement once it is located and clickable (visible and enabled).
     * Null if StaleElementReferenceException caught.
     */
    @Override
    public IExpectedSearchContextCondition<WebElement> elementToBeClickable(final T searchCriteria)
    {
        return new IExpectedSearchContextCondition<>()
        {
            private final IExpectedSearchContextCondition<WebElement> visibilityOfElementLocated = visibilityOfElement(
                    searchCriteria);

            @Override
            public WebElement apply(SearchContext searchContext)
            {
                WebElement element = visibilityOfElementLocated.apply(searchContext);
                try
                {
                    return element != null && element.isEnabled() ? element : null;
                }
                catch (StaleElementReferenceException e)
                {
                    return null;
                }
            }

            @Override
            public String toString()
            {
                return "element to be clickable: " + toStringParameters(searchCriteria);
            }
        };
    }

    /**
     * An expectation for checking an element selection state
     * @param searchCriteria used to find elements
     * @param selected state to verify
     * @return true if element state matches verifiable state false otherwise.
     * Null if StaleElementReferenceException caught.
     */
    @Override
    public IExpectedSearchContextCondition<Boolean> elementSelectionStateToBe(final T searchCriteria,
            final boolean selected)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            @SuppressWarnings("checkstyle:returnnullinsteadofboolean")
            @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
            public Boolean apply(SearchContext searchContext)
            {
                try
                {
                    WebElement element = findElement(searchContext, searchCriteria);
                    return element.isSelected() == selected;
                }
                catch (StaleElementReferenceException e)
                {
                    return null;
                }
            }

            @Override
            public String toString()
            {
                return String.format("element %s to%s be selected", toStringParameters(searchCriteria),
                        selected ? "" : " not");
            }
        };
    }

    /**
     * An expectation for checking an element to be invisible
     * @param searchCriteria used to find elements
     * @return true if element is not displayed or present but invisible or
     * no longer visible.False otherwise.
     */
    @Override
    public IExpectedSearchContextCondition<Boolean> invisibilityOfElement(final T searchCriteria)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            public Boolean apply(SearchContext searchContext)
            {
                try
                {
                    WebElement webElement = findElement(searchContext, searchCriteria);
                    return !webElement.isDisplayed();
                }
                catch (NoSuchElementException e)
                {
                    // Returns true because the element is not present in DOM. The
                    // try block checks if the element is present but is invisible.
                    return Boolean.TRUE;
                }
                catch (StaleElementReferenceException e)
                {
                    // Returns true because stale element reference implies that element
                    // is no longer visible.
                    return Boolean.TRUE;
                }
            }

            @Override
            public String toString()
            {
                return "element to no longer be visible: " + toStringParameters(searchCriteria);
            }
        };
    }

    /**
     * An expectation for checking drop-down's multi-select support.
     * This is done by checking the value of the "multiple" attribute.
     * @param searchCriteria used to find elements
     * @param multiSelect given condition
     * @return Output value with the logical opposite based on an input value.
     */
    @Override
    public IExpectedSearchContextCondition<Boolean> isMultiSelectDropDown(final T searchCriteria, boolean multiSelect)
    {
        return new IExpectedSearchContextCondition<>()
        {
            @Override
            @SuppressWarnings("checkstyle:returnnullinsteadofboolean")
            @SuppressFBWarnings("NP_BOOLEAN_RETURN_NULL")
            public Boolean apply(SearchContext searchContext)
            {
                try
                {
                    WebElement element = findElement(searchContext, searchCriteria);
                    return ExpectedConditions.isMultiSelectDropDown(element, multiSelect).apply(null);
                }
                catch (NoSuchElementException e)
                {
                    return null;
                }
            }

            @Override
            public String toString()
            {
                return String.format("An element found by %s is %s select", searchCriteria,
                        ExpectedConditions.multiSelectToString(multiSelect));
            }
        };
    }
}

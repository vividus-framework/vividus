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

package org.vividus.ui.web;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IExpectedSearchContextCondition;

public enum State implements IState, ISearchContextExpectedConditionGetter
{
    ENABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.elementToBeClickable(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.elementToBeClickable(searchCriteria);
        }
    },
    DISABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.not(ENABLED.getExpectedCondition(element));
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.not(ENABLED.getExpectedCondition(expectedConditions, searchCriteria));
        }
    },
    SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.elementSelectionStateToBe(element, true);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.elementSelectionStateToBe(searchCriteria, true);
        }
    },
    NOT_SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.elementSelectionStateToBe(element, false);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.elementSelectionStateToBe(searchCriteria, false);
        }
    },
    VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.visibilityOf(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.visibilityOfElement(searchCriteria);
        }
    },
    NOT_VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.invisibilityOf(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.invisibilityOfElement(searchCriteria);
        }
    }
}

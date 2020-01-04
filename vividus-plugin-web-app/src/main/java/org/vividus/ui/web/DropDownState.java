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
import org.vividus.ui.web.action.ExpectedConditions;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IExpectedSearchContextCondition;

public enum DropDownState implements IState
{
    ENABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.ENABLED.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.ENABLED.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    DISABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.DISABLED.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.DISABLED.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.SELECTED.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.SELECTED.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    NOT_SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.NOT_SELECTED.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.NOT_SELECTED.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.VISIBLE.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.VISIBLE.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    NOT_VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.NOT_VISIBLE.getExpectedCondition(element);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return State.NOT_VISIBLE.getExpectedCondition(expectedConditions, searchCriteria);
        }
    },
    MULTI_SELECT
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.isMultiSelectDropDown(element, true);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.isMultiSelectDropDown(searchCriteria, true);
        }
    },
    SINGLE_SELECT
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.isMultiSelectDropDown(element, false);
        }

        @Override
        public <T> IExpectedSearchContextCondition<?> getExpectedCondition(IExpectedConditions<T> expectedConditions,
                T searchCriteria)
        {
            return expectedConditions.isMultiSelectDropDown(searchCriteria, false);
        }
    }
}

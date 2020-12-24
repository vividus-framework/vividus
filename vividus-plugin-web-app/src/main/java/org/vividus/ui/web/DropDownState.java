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
import org.vividus.ui.IState;
import org.vividus.ui.State;
import org.vividus.ui.web.action.ExpectedConditions;

public enum DropDownState implements IState
{
    ENABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.ENABLED.getExpectedCondition(element);
        }
    },
    DISABLED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.DISABLED.getExpectedCondition(element);
        }
    },
    SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.SELECTED.getExpectedCondition(element);
        }
    },
    NOT_SELECTED
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.NOT_SELECTED.getExpectedCondition(element);
        }
    },
    VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.VISIBLE.getExpectedCondition(element);
        }
    },
    NOT_VISIBLE
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return State.NOT_VISIBLE.getExpectedCondition(element);
        }
    },
    MULTI_SELECT
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.expectMultiSelectDropDown(element, true);
        }
    },
    SINGLE_SELECT
    {
        @Override
        public ExpectedCondition<?> getExpectedCondition(WebElement element)
        {
            return ExpectedConditions.expectMultiSelectDropDown(element, false);
        }
    }
}

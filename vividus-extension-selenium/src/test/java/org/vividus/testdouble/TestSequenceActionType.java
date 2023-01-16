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

package org.vividus.testdouble;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.ui.action.SequenceActionType;

public enum TestSequenceActionType implements SequenceActionType<Actions>
{
    ENTER_TEXT(false)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (Consumer<String>) actions::sendKeys);
        }

        @Override
        public Type getArgumentType()
        {
            return String.class;
        }
    },
    RELEASE(true)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            actions.release();
        }

        @Override
        public Type getArgumentType()
        {
            return null;
        }
    },
    DOUBLE_CLICK(false)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::doubleClick, actions::doubleClick);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    CLICK(true)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::click, actions::click);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    };

    private final boolean nullable;

    TestSequenceActionType(boolean nullable)
    {
        this.nullable = nullable;
    }

    @Override
    public void addAction(Actions actions, Object argument)
    {
        // Do nothing
    }

    @Override
    public boolean isNullable()
    {
        return nullable;
    }
}

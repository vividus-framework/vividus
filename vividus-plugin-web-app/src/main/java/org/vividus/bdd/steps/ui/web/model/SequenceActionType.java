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

package org.vividus.bdd.steps.ui.web.model;

import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public enum SequenceActionType
{
    DOUBLE_CLICK(WebElement.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::doubleClick);
        }
    },
    CLICK_AND_HOLD(WebElement.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::clickAndHold);
        }
    },
    MOVE_BY_OFFSET(Point.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (Point arg) -> actions.moveByOffset(arg.getX(), arg.getY()));
        }
    },
    RELEASE(WebElement.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::release);
        }
    },
    ENTER_TEXT(String.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (String arg) -> actions.sendKeys(arg));
        }
    },
    CLICK(WebElement.class)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::click);
        }
    };

    private final Class<?> argumentType;

    SequenceActionType(Class<?> argumentType)
    {
        this.argumentType = argumentType;
    }

    public Class<?> getArgumentType()
    {
        return argumentType;
    }

    public abstract void addAction(Actions actions, Object argument);

    <T, U extends WebElement> void performOnWebElement(T argument, Consumer<U> argumentConsumer)
    {
        perform(argument, argumentConsumer);
    }

    @SuppressWarnings("unchecked")
    <T, U> void perform(T argument, Consumer<U> argumentConsumer)
    {
        Validate.isInstanceOf(getArgumentType(), argument, "Argument for %s action must be of type %s", name(),
                getArgumentType());
        argumentConsumer.accept((U) argument);
    }
}

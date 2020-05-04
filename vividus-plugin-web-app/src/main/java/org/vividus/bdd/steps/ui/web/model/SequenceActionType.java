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

package org.vividus.bdd.steps.ui.web.model;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.KeysUtils;

public enum SequenceActionType
{
    DOUBLE_CLICK
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::doubleClick);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    CLICK_AND_HOLD
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::clickAndHold);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    MOVE_TO
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::moveToElement);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    MOVE_BY_OFFSET
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (Point arg) -> actions.moveByOffset(arg.getX(), arg.getY()));
        }

        @Override
        public Type getArgumentType()
        {
            return Point.class;
        }
    },
    RELEASE
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::release);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    ENTER_TEXT
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (String arg) -> actions.sendKeys(arg));
        }

        @Override
        public Type getArgumentType()
        {
            return String.class;
        }
    },
    PRESS_KEYS
    {
        private final Type argumentType = TypeUtils.parameterize(List.class, String.class);

        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (List<String> arg) -> actions.sendKeys(KeysUtils.keysToCharSequenceArray(arg)));
        }

        @Override
        public Type getArgumentType()
        {
            return argumentType;
        }
    },
    CLICK
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::click);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    };

    public abstract void addAction(Actions actions, Object argument);

    public abstract Type getArgumentType();

    <T, U extends WebElement> void performOnWebElement(T argument, Consumer<U> argumentConsumer)
    {
        perform(argument, argumentConsumer);
    }

    @SuppressWarnings("unchecked")
    <T, U> void perform(T argument, Consumer<U> argumentConsumer)
    {
        Validate.isTrue(TypeUtils.isAssignable(argument.getClass(), getArgumentType()),
                "Argument for %s action must be of type %s", name(), getArgumentType());
        argumentConsumer.accept((U) argument);
    }
}

/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.ui.web.model;

import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.KeysUtils;
import org.vividus.steps.ui.model.SequenceActionType;

public enum WebSequenceActionType implements SequenceActionType<Actions>
{
    DOUBLE_CLICK(true)
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
    CLICK_AND_HOLD(true)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::clickAndHold, actions::clickAndHold);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    MOVE_TO(false)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::moveToElement, () -> { });
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    MOVE_BY_OFFSET(false)
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
    RELEASE(true)
    {
        @Override
        public void addAction(Actions actions, Object argument)
        {
            performOnWebElement(argument, actions::release, actions::release);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
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
    PRESS_KEYS(false)
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
    KEY_DOWN(false)
    {
        private final Type argumentType = TypeUtils.parameterize(List.class, String.class);

        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (List<String> args) -> buildKeysActions(args, actions::keyDown));
        }

        @Override
        public Type getArgumentType()
        {
            return argumentType;
        }
    },
    KEY_UP(false)
    {
        private final Type argumentType = TypeUtils.parameterize(List.class, String.class);

        @Override
        public void addAction(Actions actions, Object argument)
        {
            perform(argument, (List<String> args) -> buildKeysActions(args, actions::keyUp));
        }

        @Override
        public Type getArgumentType()
        {
            return argumentType;
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

    WebSequenceActionType(boolean nullable)
    {
        this.nullable = nullable;
    }

    @Override
    public boolean isNullable()
    {
        return nullable;
    }

    <T, U extends WebElement> void performOnWebElement(T argument, Consumer<U> argumentConsumer, Runnable emptyRunner)
    {
        perform(argument, argumentConsumer, emptyRunner);
    }

    <T, U> void perform(T argument, Consumer<U> argumentConsumer)
    {
        perform(argument, argumentConsumer, () -> { });
    }

    @SuppressWarnings("unchecked")
    <T, U> void perform(T argument, Consumer<U> argumentConsumer, Runnable emptyRunner)
    {
        if (argument == null)
        {
            emptyRunner.run();
        }
        else
        {
            isTrue(TypeUtils.isAssignable(argument.getClass(), getArgumentType()),
                    "Argument for %s action must be of type %s", name(), getArgumentType());
            argumentConsumer.accept((U) argument);
        }
    }

    private static void buildKeysActions(List<String> keys, Consumer<CharSequence> actionBuilder)
    {
        notEmpty(keys, "At least one key should be provided");
        keys.stream()
            .peek(key -> isTrue(EnumUtils.isValidEnum(Keys.class, key), "The '%s' is not allowed as a key", key))
            .map(Keys::valueOf)
            .forEach(actionBuilder);
    }
}

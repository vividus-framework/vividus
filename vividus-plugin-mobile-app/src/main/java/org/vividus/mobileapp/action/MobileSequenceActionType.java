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

package org.vividus.mobileapp.action;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Consumer;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.SequenceActionType;

public enum MobileSequenceActionType implements SequenceActionType<TouchGestures>
{
    TAP_AND_HOLD(true)
    {
        @Override
        public void addAction(TouchGestures actions, Object argument)
        {
            performOnWebElement(argument, actions::tapAndHold, actions::tapAndHold);
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    TAP(true)
    {
        @Override
        public void addAction(TouchGestures actions, Object argument)
        {
            performOnWebElement(argument, actions::tap, actions::tap);
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
        public void addAction(TouchGestures actions, Object argument)
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
        public void addAction(TouchGestures actions, Object argument)
        {
            perform(argument, (Point arg) -> actions.moveByOffset(arg.getX(), arg.getY()));
        }

        @Override
        public Type getArgumentType()
        {
            return Point.class;
        }
    },
    WAIT(false)
    {
        @Override
        public void addAction(TouchGestures actions, Object argument)
        {
            perform(argument, (Consumer<Duration>) actions::pause);
        }

        @Override
        public Type getArgumentType()
        {
            return Duration.class;
        }
    },
    RELEASE(true)
    {
        @Override
        public void addAction(TouchGestures actions, Object argument)
        {
            perform(argument, arg -> actions.release(), actions::release);
        }

        @Override
        public Type getArgumentType()
        {
            return null;
        }
    };

    private final boolean nullable;

    MobileSequenceActionType(boolean nullable)
    {
        this.nullable = nullable;
    }

    @Override
    public boolean isNullable()
    {
        return nullable;
    }
}

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

package org.vividus.mobileapp.action;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.PointOption.point;
import static org.apache.commons.lang3.Validate.isTrue;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.model.SequenceActionType;

import io.appium.java_client.touch.offset.PointOption;

public enum MobileSequenceActionType implements SequenceActionType<PositionCachingTouchAction>
{
    PRESS(false)
    {
        @Override
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, (WebElement e) -> action.press(getCenter(e)));
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    PRESS_BY_COORDS(false)
    {
        @Override
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, (Point arg) -> action.press(point(arg)));
        }

        @Override
        public Type getArgumentType()
        {
            return Point.class;
        }
    },
    MOVE_TO(false)
    {
        @Override
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, (WebElement e) -> action.moveTo(getCenter(e)));
        }

        @Override
        public Type getArgumentType()
        {
            return WebElement.class;
        }
    },
    WAIT(false)
    {
        @Override
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, (Duration d) -> action.waitAction(waitOptions(d)));
        }

        @Override
        public Type getArgumentType()
        {
            return Duration.class;
        }
    },
    MOVE_BY_OFFSET(false)
    {
        @Override
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, (Point arg) ->
            {
                RetrievablePointOption cachedPosition = action.getPosition();
                if (cachedPosition != null)
                {
                    Point coordinates = cachedPosition.getCoordinates();
                    arg = coordinates.moveBy(arg.getX(), arg.getY());
                }
                action.moveTo(point(arg));
            });
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
        public void addAction(PositionCachingTouchAction action, Object argument)
        {
            perform(argument, arg -> action.release());
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

    @SuppressWarnings("unchecked")
    <T, U> void perform(T argument, Consumer<U> argumentConsumer)
    {
        if (getArgumentType() != null)
        {
            isTrue(TypeUtils.isAssignable(argument.getClass(), getArgumentType()),
                    "Argument for %s action must be of type %s", name(), getArgumentType());
        }
        argumentConsumer.accept((U) argument);
    }

    private static PointOption<?> getCenter(WebElement element)
    {
        Rectangle rectangle = element.getRect();
        return new RetrievablePointOption().withCoordinates(
                rectangle.getX() + rectangle.getWidth() / 2, rectangle.getY() + rectangle.getHeight() / 2);
    }
}

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

import java.time.Duration;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.AtomicActionFactory;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public final class MobileAtomicActionFactories
{
    private MobileAtomicActionFactories()
    {
    }

    @SuppressWarnings("PMD.ShortClassName")
    public static class Tap extends AtomicActionFactory<TouchGestures, WebElement>
    {
        public Tap()
        {
            super("TAP", WebElement.class, TouchGestures::tap, TouchGestures::tap);
        }
    }

    public static class TapAndHold extends AtomicActionFactory<TouchGestures, WebElement>
    {
        public TapAndHold()
        {
            super("TAP_AND_HOLD", WebElement.class, TouchGestures::tapAndHold, TouchGestures::tapAndHold);
        }
    }

    public static class MoveTo extends AtomicActionFactory<TouchGestures, WebElement>
    {
        public MoveTo()
        {
            super("MOVE_TO", WebElement.class, TouchGestures::moveToElement);
        }
    }

    public static class MoveByOffset extends AtomicActionFactory<TouchGestures, Point>
    {
        public MoveByOffset()
        {
            super("MOVE_BY_OFFSET", Point.class,
                    (TouchGestures gestures, Point arg) -> gestures.moveByOffset(arg.getX(), arg.getY()));
        }
    }

    public static class Wait extends AtomicActionFactory<TouchGestures, Duration>
    {
        public Wait()
        {
            super("WAIT", Duration.class, TouchGestures::pause);
        }
    }

    public static class Release extends AtomicActionFactory<TouchGestures, Void>
    {
        public Release()
        {
            super("RELEASE", null, (TouchGestures gestures, Void arg) -> gestures.release(), TouchGestures::release);
        }
    }
}

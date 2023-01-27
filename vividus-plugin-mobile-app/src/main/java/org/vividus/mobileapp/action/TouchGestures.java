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

import static org.openqa.selenium.interactions.PointerInput.MouseButton.LEFT;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Interaction;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;

public class TouchGestures extends Actions
{
    private static final int MOVE_DURATION_MS = 200;

    private final PointerInput touchPointer = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    private boolean pointerMoved;

    public TouchGestures(WebDriver driver)
    {
        super(driver);
    }

    public TouchGestures tapAndHold(WebElement target)
    {
        return moveToElement(target).tapAndHold();
    }

    public TouchGestures tapAndHold()
    {
        return tick(touchPointer.createPointerDown(LEFT.asArg()));
    }

    public TouchGestures tap(WebElement target)
    {
        return moveToElement(target).tap();
    }

    public TouchGestures tap()
    {
        return tapAndHold().release();
    }

    @Override
    public TouchGestures release()
    {
        return tick(touchPointer.createPointerUp(LEFT.asArg()));
    }

    @Override
    public TouchGestures moveToElement(WebElement target)
    {
        if (!pointerMoved)
        {
            pointerMoved = true;
        }
        return tick(touchPointer.createPointerMove(Duration.ofMillis(MOVE_DURATION_MS),
                PointerInput.Origin.fromElement(target), 0, 0));
    }

    @Override
    public TouchGestures moveByOffset(int xOffset, int yOffset)
    {
        PointerInput.Origin origin;
        if (!pointerMoved)
        {
            origin = PointerInput.Origin.viewport();
            pointerMoved = true;
        }
        else
        {
            origin = PointerInput.Origin.pointer();
        }
        return tick(touchPointer.createPointerMove(Duration.ofMillis(MOVE_DURATION_MS), origin, xOffset, yOffset));
    }

    @Override
    public TouchGestures pause(Duration duration)
    {
        return tick(new Pause(touchPointer, duration));
    }

    @Override
    public TouchGestures tick(Interaction... actions)
    {
        super.tick(actions);
        return this;
    }

    @Override
    public Action build()
    {
        pointerMoved = false;
        return super.build();
    }
}

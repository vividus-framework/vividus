/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.action;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.vividus.ui.model.Point;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public final class PlaywrightActions
{
    private PlaywrightActions()
    {
    }

    private static void moveCursorToTheCenterOfElement(Locator element, Page page)
    {
        element.scrollIntoViewIfNeeded();
        BoundingBox boundingBox = element.boundingBox();
        page.mouse().move(boundingBox.x + boundingBox.width / 2, boundingBox.y + boundingBox.height / 2);
    }

    public static class Click extends AbstractPlaywrightActions
    {
        public Click()
        {
            super("CLICK", false, PlaywrightLocator.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            if (locator != null)
            {
                locator.click();
            }
            else
            {
                Mouse pageMouse = page.mouse();
                pageMouse.down();
                pageMouse.up();
            }
        }

        @Override
        public Click createAction()
        {
            return new Click();
        }
    }

    public static class DoubleClick extends AbstractPlaywrightActions
    {
        public DoubleClick()
        {
            super("DOUBLE_CLICK", false, PlaywrightLocator.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            if (locator != null)
            {
                locator.dblclick();
            }
            else
            {
                throw new UnsupportedOperationException("'DOUBLE_CLICK' action can be performed on page elements only");
            }
        }

        @Override
        public DoubleClick createAction()
        {
            return new DoubleClick();
        }
    }

    public static class ClickAndHold extends AbstractPlaywrightActions
    {
        public ClickAndHold()
        {
            super("CLICK_AND_HOLD", false, PlaywrightLocator.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            Mouse pageMouse = page.mouse();
            if (locator != null)
            {
                moveCursorToTheCenterOfElement(locator, page);
                pageMouse.down();
            }
            else
            {
                pageMouse.down();
            }
        }

        @Override
        public ClickAndHold createAction()
        {
            return new ClickAndHold();
        }
    }

    public static class Release extends AbstractPlaywrightActions
    {
        public Release()
        {
            super("RELEASE", false, PlaywrightLocator.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            Mouse pageMouse = page.mouse();
            if (locator != null)
            {
                moveCursorToTheCenterOfElement(locator, page);
                pageMouse.up();
            }
            else
            {
                pageMouse.up();
            }
        }

        @Override
        public Release createAction()
        {
            return new Release();
        }
    }

    public static class MoveTo extends AbstractPlaywrightActions
    {
        public MoveTo()
        {
            super("MOVE_TO", true, PlaywrightLocator.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            moveCursorToTheCenterOfElement(locator, page);
        }

        @Override
        public MoveTo createAction()
        {
            return new MoveTo();
        }
    }

    public static class MoveByOffset extends AbstractPlaywrightActions
    {
        public MoveByOffset()
        {
            super("MOVE_BY_OFFSET", true, Point.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            throw new UnsupportedOperationException(getName() + " action is not supported by Playwright");
        }

        @Override
        public MoveByOffset createAction()
        {
            return new MoveByOffset();
        }
    }

    public static class EnterText extends AbstractPlaywrightActions
    {
        public EnterText()
        {
            super("ENTER_TEXT", true, String.class);
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            page.keyboard().type((String) getArgument());
        }

        @Override
        public EnterText createAction()
        {
            return new EnterText();
        }
    }

    @SuppressWarnings("unchecked")
    public static class PressKeys extends AbstractPlaywrightActions
    {
        public PressKeys()
        {
            super("PRESS_KEYS", true, TypeUtils.parameterize(List.class, String.class));
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            Keyboard keyboard = page.keyboard();
            List<String> keys = new ArrayList<>((List<String>) getArgument());
            keys.forEach(keyboard::press);
        }

        @Override
        public PressKeys createAction()
        {
            return new PressKeys();
        }
    }

    @SuppressWarnings("unchecked")
    public static class KeyDown extends AbstractPlaywrightActions
    {
        public KeyDown()
        {
            super("KEY_DOWN", true, TypeUtils.parameterize(List.class, String.class));
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            Keyboard keyboard = page.keyboard();
            List<String> keys = new ArrayList<>((List<String>) getArgument());
            keys.forEach(keyboard::down);
        }

        @Override
        public KeyDown createAction()
        {
            return new KeyDown();
        }
    }

    @SuppressWarnings("unchecked")
    public static class KeyUp extends AbstractPlaywrightActions
    {
        public KeyUp()
        {
            super("KEY_UP", true, TypeUtils.parameterize(List.class, String.class));
        }

        @Override
        public void execute(Locator locator, Page page)
        {
            Keyboard keyboard = page.keyboard();
            List<String> keys = new ArrayList<>((List<String>) getArgument());
            keys.forEach(keyboard::up);
        }

        @Override
        public KeyUp createAction()
        {
            return new KeyUp();
        }
    }
}

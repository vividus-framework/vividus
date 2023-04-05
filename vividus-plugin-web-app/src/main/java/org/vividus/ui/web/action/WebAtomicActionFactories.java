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

package org.vividus.ui.web.action;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.selenium.KeysManager;
import org.vividus.ui.action.AtomicActionFactory;

public final class WebAtomicActionFactories
{
    private WebAtomicActionFactories()
    {
    }

    private static void buildKeysActions(KeysManager keysManager, List<String> keys,
            Consumer<CharSequence> actionBuilder)
    {
        notEmpty(keys, "At least one key should be provided");
        for (String key : keys)
        {
            actionBuilder.accept(key.length() == 1 ? key : keysManager.convertToKey(true, key));
        }
    }

    public static class Click extends AtomicActionFactory<Actions, WebElement>
    {
        public Click()
        {
            super("CLICK", WebElement.class, Actions::click, Actions::click);
        }
    }

    public static class DoubleClick extends AtomicActionFactory<Actions, WebElement>
    {
        public DoubleClick()
        {
            super("DOUBLE_CLICK", WebElement.class, Actions::doubleClick, Actions::doubleClick);
        }
    }

    public static class ClickAndHold extends AtomicActionFactory<Actions, WebElement>
    {
        public ClickAndHold()
        {
            super("CLICK_AND_HOLD", WebElement.class, Actions::clickAndHold, Actions::clickAndHold);
        }
    }

    public static class Release extends AtomicActionFactory<Actions, WebElement>
    {
        public Release()
        {
            super("RELEASE", WebElement.class, Actions::release, Actions::release);
        }
    }

    public static class MoveTo extends AtomicActionFactory<Actions, WebElement>
    {
        public MoveTo()
        {
            super("MOVE_TO", WebElement.class, Actions::moveToElement);
        }
    }

    public static class MoveByOffset extends AtomicActionFactory<Actions, Point>
    {
        public MoveByOffset()
        {
            super("MOVE_BY_OFFSET", Point.class,
                    (Actions actions, Point arg) -> actions.moveByOffset(arg.getX(), arg.getY()));
        }
    }

    public static class EnterText extends AtomicActionFactory<Actions, String>
    {
        public EnterText()
        {
            super("ENTER_TEXT", String.class, Actions::sendKeys);
        }
    }

    public static class PressKeys extends AtomicActionFactory<Actions, List<String>>
    {
        public PressKeys(KeysManager keysManager)
        {
            super("PRESS_KEYS", TypeUtils.parameterize(List.class, String.class),
                    (Actions actions, List<String> arg) -> actions.sendKeys(keysManager.convertToKeys(arg)));
        }
    }

    public static class KeyDown extends AtomicActionFactory<Actions, List<String>>
    {
        public KeyDown(KeysManager keysManager)
        {
            super("KEY_DOWN", TypeUtils.parameterize(List.class, String.class),
                    (Actions actions, List<String> arg) -> buildKeysActions(keysManager, arg, actions::keyDown));
        }
    }

    public static class KeyUp extends AtomicActionFactory<Actions, List<String>>
    {
        public KeyUp(KeysManager keysManager)
        {
            super("KEY_UP", TypeUtils.parameterize(List.class, String.class),
                    (Actions actions, List<String> arg) -> buildKeysActions(keysManager, arg, actions::keyUp));
        }
    }
}

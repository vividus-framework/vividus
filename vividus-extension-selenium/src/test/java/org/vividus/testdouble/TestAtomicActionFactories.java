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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.ui.action.AtomicActionFactory;

public class TestAtomicActionFactories
{
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

    public static class Release extends AtomicActionFactory<Actions, WebElement>
    {
        public Release()
        {
            super("RELEASE", WebElement.class, Actions::release, Actions::release);
        }
    }

    public static class EnterText extends AtomicActionFactory<Actions, String>
    {
        public EnterText()
        {
            super("ENTER_TEXT", String.class, Actions::sendKeys);
        }
    }
}

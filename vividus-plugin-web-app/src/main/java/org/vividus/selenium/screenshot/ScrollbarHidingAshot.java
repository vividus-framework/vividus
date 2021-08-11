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

package org.vividus.selenium.screenshot;

import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;

class ScrollbarHidingAshot extends AShot
{
    private static final long serialVersionUID = -3976031178886339719L;

    private final transient Optional<WebElement> scrollableElement;
    private final transient IScrollbarHandler scrollbarHandler;

    ScrollbarHidingAshot(Optional<WebElement> scrollableElement,
            IScrollbarHandler scrollbarHandler)
    {
        this.scrollableElement = scrollableElement;
        this.scrollbarHandler = scrollbarHandler;
    }

    @Override
    public Screenshot takeScreenshot(WebDriver driver)
    {
        return takeWithHiddentScrollbars(() -> super.takeScreenshot(driver));
    }

    @Override
    public Screenshot takeScreenshot(WebDriver driver, WebElement webElement)
    {
        return takeWithHiddentScrollbars(() -> super.takeScreenshot(driver, webElement));
    }

    private Screenshot takeWithHiddentScrollbars(Supplier<Screenshot> taker)
    {
        return scrollableElement.map(e -> scrollbarHandler.performActionWithHiddenScrollbars(taker, e))
                .orElseGet(() -> scrollbarHandler.performActionWithHiddenScrollbars(taker));
    }
}

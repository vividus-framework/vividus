/*
 * Copyright 2019-2022 the original author or authors.
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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;

public class ScrollBarHidingCoordsProviderDecorator extends CoordsProvider
{
    private static final long serialVersionUID = -4309766535331129861L;

    private final CoordsProvider coordsProvider;
    private final transient IScrollbarHandler scrollbarHandler;

    public ScrollBarHidingCoordsProviderDecorator(CoordsProvider coordsProvider, IScrollbarHandler scrollbarHandler)
    {
        this.coordsProvider = coordsProvider;
        this.scrollbarHandler = scrollbarHandler;
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        return scrollbarHandler.performActionWithHiddenScrollbars(() -> coordsProvider.ofElement(driver, element));
    }
}

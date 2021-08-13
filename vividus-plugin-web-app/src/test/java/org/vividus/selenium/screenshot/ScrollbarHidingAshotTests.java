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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

@ExtendWith(MockitoExtension.class)
class ScrollbarHidingAshotTests
{
    @Mock private ShootingStrategy strategy;
    @Mock private ScrollbarHandler scrollbarHandler;
    @Mock private WebDriver webDriver;
    @Mock private BufferedImage bufferedImage;
    @Mock private ru.yandex.qatools.ashot.Screenshot screenshot;

    @Test
    void shouldTakeScreenshotsWithHiddenScrollbars()
    {
        ScrollbarHidingAshot ashot = new ScrollbarHidingAshot(Optional.empty(), scrollbarHandler);
        ashot.shootingStrategy(strategy);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(screenshot);
        when(strategy.getScreenshot(webDriver)).thenReturn(bufferedImage);
        ashot.takeScreenshot(webDriver);
        assertSame(screenshot, ashot.takeScreenshot(webDriver));
    }

    @Test
    void shouldTakeScreenshotsWithElementAndWithHiddenScrollbars()
    {
        WebElement scrollableElement = mock(WebElement.class);
        ScrollbarHidingAshot ashot = new ScrollbarHidingAshot(Optional.of(scrollableElement), scrollbarHandler);
        ashot.shootingStrategy(strategy);
        ashot.coordsProvider(new CoordsProvider()
        {
            private static final long serialVersionUID = 8784997908203644003L;

            @Override
            public Coords ofElement(WebDriver driver, WebElement element)
            {
                return new Coords(0, 0, 0, 0);
            }
        });
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }), eq(scrollableElement))).thenReturn(screenshot);
        when(strategy.getScreenshot(webDriver, Set.of())).thenReturn(bufferedImage);
        ashot.takeScreenshot(webDriver, scrollableElement);
        assertSame(screenshot, ashot.takeScreenshot(webDriver, scrollableElement));
    }
}

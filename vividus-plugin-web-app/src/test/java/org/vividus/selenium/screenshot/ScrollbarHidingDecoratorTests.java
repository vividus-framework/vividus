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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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

import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class ScrollbarHidingDecoratorTests
{
    @Mock private ShootingStrategy strategy;
    @Mock private ScrollbarHandler scrollbarHandler;
    @Mock private WebDriver webDriver;
    @Mock private BufferedImage bufferedImage;

    @Test
    void shouldTakeScreenshotsWithHiddenScrollbars()
    {
        ScrollbarHidingDecorator decorator = new ScrollbarHidingDecorator(strategy, Optional.empty(), scrollbarHandler);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(bufferedImage);
        when(strategy.getScreenshot(webDriver)).thenReturn(bufferedImage);
        assertSame(bufferedImage, decorator.getScreenshot(webDriver));
    }

    @Test
    void shouldTakeScreenshotsWithElementAndWithHiddenScrollbars()
    {
        WebElement scrollableElement = mock(WebElement.class);
        ScrollbarHidingDecorator decorator = new ScrollbarHidingDecorator(strategy, Optional.of(scrollableElement),
                scrollbarHandler);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }), eq(scrollableElement))).thenReturn(bufferedImage);
        when(strategy.getScreenshot(webDriver, Set.of())).thenReturn(bufferedImage);
        assertSame(bufferedImage, decorator.getScreenshot(webDriver, Set.of()));
    }

    @Test
    void shouldPrepareCoords()
    {
        ScrollbarHidingDecorator decorator = new ScrollbarHidingDecorator(strategy, Optional.empty(),
                scrollbarHandler);
        Coords coords = mock(Coords.class);
        decorator.prepareCoords(Set.of(coords));
        verify(strategy).prepareCoords(Set.of(coords));
    }
}

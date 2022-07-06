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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;

@ExtendWith(MockitoExtension.class)
class ScrollBarHidingCoordsProviderDecoratorTests
{
    @Mock private CoordsProvider coordsProvider;
    @Mock private ScrollbarHandler scrollbarHandler;
    @Mock private Coords coords;
    @Mock private WebDriver webDriver;
    @Mock private WebElement webElement;
    @Mock private BufferedImage bufferedImage;

    @Test
    void shouldReturnCoordsOfElement()
    {
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(coords);
        ScrollBarHidingCoordsProviderDecorator decorator = new ScrollBarHidingCoordsProviderDecorator(coordsProvider,
                scrollbarHandler);
        Coords elementCoords = decorator.ofElement(webDriver, webElement);
        assertEquals(coords, elementCoords);
        verify(coordsProvider).ofElement(webDriver, webElement);
    }
}

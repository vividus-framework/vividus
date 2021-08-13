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

package org.vividus.selenium.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;

import ru.yandex.qatools.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class NativeHeaderAwareCoordsProviderTests
{
    @Mock private MobileAppWebDriverManager driverManager;
    @Mock private WebElement webElement;
    @InjectMocks private NativeHeaderAwareCoordsProvider coordsProvider;

    @Test
    void shoudProvideAdjustedWithNativeHeaderHeightCoordinates()
    {
        when(driverManager.getStatusBarSize()).thenReturn(100);
        when(webElement.getLocation()).thenReturn(new Point(0, 234));
        when(webElement.getSize()).thenReturn(new Dimension(1, 1));
        Coords coords = coordsProvider.ofElement(null, webElement);
        Assertions.assertAll(() -> assertEquals(0, coords.getX()),
                             () -> assertEquals(134, coords.getY()),
                             () -> assertEquals(1, coords.getWidth()),
                             () -> assertEquals(1, coords.getHeight()));
    }
}

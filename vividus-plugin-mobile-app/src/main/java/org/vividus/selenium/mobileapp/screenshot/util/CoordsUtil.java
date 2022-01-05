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

package org.vividus.selenium.mobileapp.screenshot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ru.yandex.qatools.ashot.coordinates.Coords;

public final class CoordsUtil
{
    private CoordsUtil()
    {
    }

    public static Coords scale(Coords coords, double dpr)
    {
        int x = scale(coords.x, dpr);
        int y = scale(coords.y, dpr);
        int width = scale(coords.width, dpr);
        int height = scale(coords.height, dpr);
        return new Coords(x, y, width, height);
    }

    public static int scale(int value, double dpr)
    {
        return BigDecimal.valueOf(value).multiply(BigDecimal.valueOf(dpr))
            .setScale(0, RoundingMode.CEILING)
            .intValue();
    }
}

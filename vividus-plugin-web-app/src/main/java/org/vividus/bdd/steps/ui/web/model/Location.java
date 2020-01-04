/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.model;

import java.util.function.BiFunction;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;

public enum Location
{
    TOP(Location::centerX, Location::topY),
    LEFT(Location::leftX, Location::centerY),
    RIGHT(Location::rightX, Location::centerY),
    BOTTOM(Location::centerX, Location::bottomY),
    CENTER(Location::centerX, Location::centerY),
    LEFT_TOP(Location::leftX, Location::topY),
    RIGHT_TOP(Location::rightX, Location::topY),
    LEFT_BOTTOM(Location::leftX, Location::bottomY),
    RIGHT_BOTTOM(Location::rightX, Location::bottomY);

    private final transient BiFunction<Rectangle, Rectangle, Integer> xOffsetFunction;
    private final transient BiFunction<Rectangle, Rectangle, Integer> yOffsetFunction;

    Location(BiFunction<Rectangle, Rectangle, Integer> xOffsetFunction,
            BiFunction<Rectangle, Rectangle, Integer> yOffsetFunction)
    {
        this.xOffsetFunction = xOffsetFunction;
        this.yOffsetFunction = yOffsetFunction;
    }

    public Point getPoint(Rectangle origin, Rectangle target)
    {
        return new Point(xOffsetFunction.apply(origin, target), yOffsetFunction.apply(origin, target));
    }

    private static int leftX(Rectangle origin, Rectangle target)
    {
        return target.getX() - origin.getX() - origin.getWidth();
    }

    private static int centerX(Rectangle origin, Rectangle target)
    {
        return target.getX() - origin.getX() - (origin.getWidth() - target.getWidth()) / 2;
    }

    private static int centerY(Rectangle origin, Rectangle target)
    {
        return target.getY() - origin.getY() - (origin.getHeight() - target.getHeight()) / 2;
    }

    private static int rightX(Rectangle origin, Rectangle target)
    {
        return target.getX() - origin.getX() + origin.getWidth() - (origin.getWidth() - target.getWidth());
    }

    private static int bottomY(Rectangle origin, Rectangle target)
    {
        return target.getY() - origin.getY() + target.getHeight();
    }

    private static int topY(Rectangle origin, Rectangle target)
    {
        int yOffset = target.getY() - origin.getY() - target.getHeight();
        int heightDiff = Math.abs(origin.getHeight() - target.getHeight());
        return target.getHeight() > origin.getHeight() ? yOffset + heightDiff : yOffset - heightDiff;
    }
}

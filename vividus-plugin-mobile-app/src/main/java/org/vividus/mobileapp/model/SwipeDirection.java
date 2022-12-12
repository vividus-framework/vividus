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

package org.vividus.mobileapp.model;

import java.util.function.ToIntFunction;

import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.vividus.mobileapp.configuration.SwipeConfiguration;

public enum SwipeDirection
{
    UP(true, true, SwipeDirection.VERTICAL_INDENT_COEFFICIENT, Rectangle::getHeight, Rectangle::getWidth,
            SwipeConfiguration::getSwipeVerticalXPosition),
    DOWN(false, true, SwipeDirection.VERTICAL_INDENT_COEFFICIENT, Rectangle::getHeight, Rectangle::getWidth,
            SwipeConfiguration::getSwipeVerticalXPosition),
    LEFT(true, false, SwipeDirection.HORIZONTAL_INDENT_COEFFICIENT, Rectangle::getWidth, Rectangle::getHeight,
            SwipeConfiguration::getSwipeHorizontalYPosition),
    RIGHT(false, false, SwipeDirection.HORIZONTAL_INDENT_COEFFICIENT, Rectangle::getWidth, Rectangle::getHeight,
            SwipeConfiguration::getSwipeHorizontalYPosition);

    private static final float VERTICAL_INDENT_COEFFICIENT = 0.2f;
    private static final float HORIZONTAL_INDENT_COEFFICIENT = 0.125f;

    private final boolean backward;
    private final boolean vertical;
    private final float indentCoefficient;
    private final ToIntFunction<Rectangle> axisLengthProvider;
    private final ToIntFunction<Rectangle> boundariesDimensionProvider;
    private final ToIntFunction<SwipeConfiguration> percentageProvider;

    SwipeDirection(boolean backward, boolean vertical, float indentCoefficient,
            ToIntFunction<Rectangle> axisLengthProvider, ToIntFunction<Rectangle> boundariesDimensionProvider,
            ToIntFunction<SwipeConfiguration> percentageProvider)
    {
        this.backward = backward;
        this.vertical = vertical;
        this.indentCoefficient = indentCoefficient;
        this.axisLengthProvider = axisLengthProvider;
        this.boundariesDimensionProvider = boundariesDimensionProvider;
        this.percentageProvider = percentageProvider;
    }

    public boolean isVertical()
    {
        return vertical;
    }

    public boolean isBackward()
    {
        return backward;
    }

    public int getAxisLength(Rectangle rectangle)
    {
        return axisLengthProvider.applyAsInt(rectangle);
    }

    public MoveCoordinates calculateCoordinates(Rectangle swipeArea, SwipeConfiguration configuration)
    {
        int swipeAxisLength = getAxisLength(swipeArea);
        int indent = (int) (swipeAxisLength * indentCoefficient);
        int coordinateFirst = swipeAxisLength - indent;
        int coordinateSecond = indent;
        if (backward)
        {
            return createCoordinates(coordinateFirst, coordinateSecond, configuration, swipeArea);
        }
        return createCoordinates(coordinateSecond, coordinateFirst, configuration, swipeArea);
    }

    public MoveCoordinates createCoordinates(int startCoordinate, int endCoordinate,
            SwipeConfiguration configuration, Rectangle swipeArea)
    {
        int swipeAxisCoordinate = calculateCoordinate(boundariesDimensionProvider.applyAsInt(swipeArea),
                percentageProvider.applyAsInt(configuration));
        if (vertical)
        {
            return createAdjustedCoordinates(swipeAxisCoordinate, startCoordinate, swipeAxisCoordinate,
                    endCoordinate, swipeArea.getPoint());
        }
        return createAdjustedCoordinates(startCoordinate, swipeAxisCoordinate, endCoordinate, swipeAxisCoordinate,
                swipeArea.getPoint());
    }

    private static MoveCoordinates createAdjustedCoordinates(int startX, int startY, int endX, int endY, Point point)
    {
        return new MoveCoordinates(startX + point.x, startY + point.y, endX + point.getX(), endY + point.getY());
    }

    @SuppressWarnings("MagicNumber")
    private static int calculateCoordinate(int dimension, int percentage)
    {
        if (percentage == 0)
        {
            return 1;
        }
        if (percentage == 100)
        {
            return dimension - 1;
        }
        return dimension * percentage / 100;
    }
}

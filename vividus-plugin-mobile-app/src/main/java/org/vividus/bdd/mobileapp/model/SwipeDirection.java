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

package org.vividus.bdd.mobileapp.model;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.vividus.bdd.mobileapp.configuration.MobileApplicationConfiguration;

public enum SwipeDirection
{
    UP
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Rectangle swipeArea,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = swipeArea.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return createCoordinates(swipeArea.getHeight() - indent, indent, swipeArea.getWidth(),
                    mobileApplicationConfiguration.getSwipeVerticalXPosition(), swipeArea.getPoint());
        }
    },
    DOWN
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Rectangle swipeArea,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = swipeArea.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return createCoordinates(indent, swipeArea.getHeight() - indent, swipeArea.getWidth(),
                    mobileApplicationConfiguration.getSwipeVerticalXPosition(), swipeArea.getPoint());
        }
    },
    LEFT
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Rectangle swipeArea,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = swipeArea.getWidth() / HORIZONTAL_INDENT_COEFFICIENT;
            int y = calculateCoordinate(swipeArea.getHeight(),
                    mobileApplicationConfiguration.getSwipeHorizontalYPosition());
            return createAdjustedCoordinates(swipeArea.getWidth() - indent, y, indent, y, swipeArea.getPoint());
        }
    },
    RIGHT
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Rectangle swipeArea,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            Dimension dimension = swipeArea.getDimension();
            Point point = swipeArea.getPoint();
            int indent = dimension.getWidth() / HORIZONTAL_INDENT_COEFFICIENT;
            int y = calculateCoordinate(dimension.getHeight(),
                    mobileApplicationConfiguration.getSwipeHorizontalYPosition());
            return createAdjustedCoordinates(indent, y, dimension.getWidth() - indent, y, point);
        }
    };

    private static final int VERTICAL_INDENT_COEFFICIENT = 5;
    private static final int HORIZONTAL_INDENT_COEFFICIENT = 8;

    public abstract SwipeCoordinates calculateCoordinates(Rectangle swipeArea,
            MobileApplicationConfiguration mobileApplicationConfiguration);

    public static SwipeCoordinates createCoordinates(int startY, int endY, int width, int xOffsetPercentage,
            Point point)
    {
        int x = calculateCoordinate(width, xOffsetPercentage);
        return createAdjustedCoordinates(x, startY, x, endY, point);
    }

    private static SwipeCoordinates createAdjustedCoordinates(int startX, int startY, int endX, int endY, Point point)
    {
        return new SwipeCoordinates(startX + point.x, startY + point.y, endX + point.getX(), endY + point.getY());
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

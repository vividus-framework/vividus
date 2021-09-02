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
import org.vividus.bdd.mobileapp.configuration.MobileApplicationConfiguration;

public enum SwipeDirection
{
    UP
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = dimension.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return createCoordinates(dimension.getHeight() - indent, indent, dimension.getWidth(),
                    mobileApplicationConfiguration.getSwipeVerticalXPosition());
        }
    },
    DOWN
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = dimension.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return createCoordinates(indent, dimension.getHeight() - indent, dimension.getWidth(),
                    mobileApplicationConfiguration.getSwipeVerticalXPosition());
        }
    },
    LEFT
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = dimension.getWidth() / HORIZONTAL_INDENT_COEFFICIENT;
            int y = calculateCoordinate(dimension.getHeight(),
                    mobileApplicationConfiguration.getSwipeHorizontalYPosition());
            return new SwipeCoordinates(dimension.getWidth() - indent, y, indent, y);
        }
    },
    RIGHT
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension,
                MobileApplicationConfiguration mobileApplicationConfiguration)
        {
            int indent = dimension.getWidth() / HORIZONTAL_INDENT_COEFFICIENT;
            int y = calculateCoordinate(dimension.getHeight(),
                    mobileApplicationConfiguration.getSwipeHorizontalYPosition());
            return new SwipeCoordinates(indent, y, dimension.getWidth() - indent, y);
        }
    };

    private static final int VERTICAL_INDENT_COEFFICIENT = 5;
    private static final int HORIZONTAL_INDENT_COEFFICIENT = 8;

    public abstract SwipeCoordinates calculateCoordinates(Dimension dimension,
            MobileApplicationConfiguration mobileApplicationConfiguration);

    public static SwipeCoordinates createCoordinates(int startY, int endY, int width, int xOffsetPercentage)
    {
        int x = calculateCoordinate(width, xOffsetPercentage);
        return new SwipeCoordinates(x, startY, x, endY);
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

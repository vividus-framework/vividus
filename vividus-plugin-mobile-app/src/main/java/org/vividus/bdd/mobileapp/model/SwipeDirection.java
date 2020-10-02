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

package org.vividus.bdd.mobileapp.model;

import org.openqa.selenium.Dimension;

public enum SwipeDirection
{
    UP
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension)
        {
            int centerX = dimension.getWidth() / 2;
            int indent = dimension.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return new SwipeCoordinates(centerX, dimension.getHeight() - indent, centerX, indent);
        }
    },
    DOWN
    {
        @Override
        public SwipeCoordinates calculateCoordinates(Dimension dimension)
        {
            int centerX = dimension.getWidth() / 2;
            int indent = dimension.getHeight() / VERTICAL_INDENT_COEFFICIENT;
            return new SwipeCoordinates(centerX, indent, centerX, dimension.getHeight() - indent);
        }
    };

    private static final int VERTICAL_INDENT_COEFFICIENT = 5;

    public abstract SwipeCoordinates calculateCoordinates(Dimension dimension);
}

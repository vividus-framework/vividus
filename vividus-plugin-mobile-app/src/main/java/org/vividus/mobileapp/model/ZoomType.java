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

import org.openqa.selenium.Rectangle;

public enum ZoomType
{
    IN, OUT;

    private static final float CENTER_INDENT_COEFFICIENT = 0.1f;

    @SuppressWarnings("MagicNumber")
    public ZoomCoordinates calculateCoordinates(Rectangle zoomArea)
    {
        int zoomAreaWidth = zoomArea.getWidth();
        int zoomAreaHeight = zoomArea.getHeight();
        int zoomAreaX = zoomArea.getX();
        int zoomAreaY = zoomArea.getY();

        int finger1CoordinateXFirst = (int) (zoomAreaWidth * (0.5 - CENTER_INDENT_COEFFICIENT) + zoomAreaX);
        int finger1CoordinateYFirst = (int) (zoomAreaHeight * (0.5 + CENTER_INDENT_COEFFICIENT) + zoomAreaY);
        int finger1CoordinateXSecond = zoomAreaX;
        int finger1CoordinateYSecond = zoomAreaHeight + zoomAreaY;
        int finger2CoordinateXFirst = (int) (zoomAreaWidth * (0.5 + CENTER_INDENT_COEFFICIENT) + zoomAreaX);
        int finger2CoordinateYFirst = (int) (zoomAreaHeight * (0.5 - CENTER_INDENT_COEFFICIENT) + zoomAreaY);
        int finger2CoordinateXSecond = zoomAreaWidth + zoomAreaX;
        int finger2CoordinateYSecond = zoomAreaY;

        MoveCoordinates moveCoordinates1;
        MoveCoordinates moveCoordinates2;
        if (ZoomType.OUT == this)
        {
            moveCoordinates1 = new MoveCoordinates(finger1CoordinateXSecond, finger1CoordinateYSecond,
                    finger1CoordinateXFirst, finger1CoordinateYFirst);
            moveCoordinates2 = new MoveCoordinates(finger2CoordinateXSecond, finger2CoordinateYSecond,
                    finger2CoordinateXFirst, finger2CoordinateYFirst);
        }
        else
        {
            moveCoordinates1 = new MoveCoordinates(finger1CoordinateXFirst, finger1CoordinateYFirst,
                    finger1CoordinateXSecond, finger1CoordinateYSecond);
            moveCoordinates2 = new MoveCoordinates(finger2CoordinateXFirst, finger2CoordinateYFirst,
                    finger2CoordinateXSecond, finger2CoordinateYSecond);
        }
        return new ZoomCoordinates(moveCoordinates1, moveCoordinates2);
    }
}

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
    private static final float TOP_INDENT_COEFFICIENT = 0.2f;
    private static final float BOTTOM_INDENT_COEFFICIENT = 0.4f;
    private static final float LEFT_INDENT_COEFFICIENT = 0.2f;
    private static final float RIGHT_INDENT_COEFFICIENT = 0.2f;

    @SuppressWarnings("MagicNumber")
    public ZoomCoordinates calculateCoordinates(Rectangle zoomArea)
    {
        int zoomAreaWidth = (int) (zoomArea.getWidth() * (1 - (LEFT_INDENT_COEFFICIENT + RIGHT_INDENT_COEFFICIENT)));
        int zoomAreaHeight = (int) (zoomArea.getHeight()
                * (1 - (TOP_INDENT_COEFFICIENT + BOTTOM_INDENT_COEFFICIENT)));
        int zoomAreaX = (int) (zoomArea.getX() + zoomArea.getWidth() * LEFT_INDENT_COEFFICIENT);
        int zoomAreaY = (int) (zoomArea.getY() + zoomArea.getHeight() * TOP_INDENT_COEFFICIENT);

        int finger1CoordinateXFirst = (int) (zoomAreaWidth * (0.5 - CENTER_INDENT_COEFFICIENT) + zoomAreaX);
        int finger1CoordinateYFirst = (int) (zoomAreaHeight * (0.5 + CENTER_INDENT_COEFFICIENT) + zoomAreaY);
        int finger1CoordinateYSecond = zoomAreaHeight + zoomAreaY;
        int finger2CoordinateXFirst = (int) (zoomAreaWidth * (0.5 + CENTER_INDENT_COEFFICIENT) + zoomAreaX);
        int finger2CoordinateYFirst = (int) (zoomAreaHeight * (0.5 - CENTER_INDENT_COEFFICIENT) + zoomAreaY);
        int finger2CoordinateXSecond = zoomAreaWidth + zoomAreaX;

        MoveCoordinates moveCoordinates1;
        MoveCoordinates moveCoordinates2;
        if (ZoomType.OUT == this)
        {
            moveCoordinates1 = new MoveCoordinates(zoomAreaX, finger1CoordinateYSecond, finger1CoordinateXFirst,
                    finger1CoordinateYFirst);
            moveCoordinates2 = new MoveCoordinates(finger2CoordinateXSecond, zoomAreaY, finger2CoordinateXFirst,
                    finger2CoordinateYFirst);
        }
        else
        {
            moveCoordinates1 = new MoveCoordinates(finger1CoordinateXFirst, finger1CoordinateYFirst, zoomAreaX,
                    finger1CoordinateYSecond);
            moveCoordinates2 = new MoveCoordinates(finger2CoordinateXFirst, finger2CoordinateYFirst,
                    finger2CoordinateXSecond, zoomAreaY);
        }
        return new ZoomCoordinates(moveCoordinates1, moveCoordinates2);
    }
}

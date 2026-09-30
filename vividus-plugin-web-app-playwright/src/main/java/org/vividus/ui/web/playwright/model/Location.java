/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.model;

import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.Position;

public enum Location
{
    TOP
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width / 2, 0);
        }
    },
    BOTTOM
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width / 2, targetBoundingBox.height);
        }
    },
    LEFT
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(0, targetBoundingBox.height / 2);
        }
    },
    RIGHT
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width, targetBoundingBox.height / 2);
        }
    },
    CENTER
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width / 2, targetBoundingBox.height / 2);
        }
    },
    LEFT_TOP
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(0, 0);
        }
    },
    RIGHT_TOP
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width, 0);
        }
    },
    LEFT_BOTTOM
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(0, targetBoundingBox.height);
        }
    },
    RIGHT_BOTTOM
    {
        @Override
        public Position calculatePosition(BoundingBox targetBoundingBox)
        {
            return new Position(targetBoundingBox.width, targetBoundingBox.height);
        }
    };

    /**
     * Calculates the position inside the target element bounding box the draggable element should be dropped at.
     *
     * @param targetBoundingBox The bounding box of the target element.
     * @return The position to drop the draggable element at.
     */
    public abstract Position calculatePosition(BoundingBox targetBoundingBox);
}

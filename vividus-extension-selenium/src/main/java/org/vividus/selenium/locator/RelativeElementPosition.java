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

package org.vividus.selenium.locator;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

public enum RelativeElementPosition
{
    ABOVE
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.above(element);
        }
    },
    BELOW
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.below(element);
        }
    },
    TO_LEFT_OF
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.toLeftOf(element);
        }
    },
    TO_RIGHT_OF
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.toRightOf(element);
        }
    },
    NEAR
    {
        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element)
        {
            return relativeBy.near(element);
        }

        @Override
        public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element,
                                                int atMostDistanceInPixels)
        {
            return relativeBy.near(element, atMostDistanceInPixels);
        }
    };

    public abstract RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element);

    public RelativeLocator.RelativeBy apply(RelativeLocator.RelativeBy relativeBy, WebElement element,
                                            int atMostDistanceInPixels)
    {
        throw new UnsupportedOperationException();
    }
}

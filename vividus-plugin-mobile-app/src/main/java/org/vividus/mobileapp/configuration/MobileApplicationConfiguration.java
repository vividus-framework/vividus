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

package org.vividus.mobileapp.configuration;

import java.time.Duration;

import org.apache.commons.lang3.Validate;

public class MobileApplicationConfiguration
{
    private final Duration swipeStabilizationDuration;
    private final int swipeLimit;
    private final int swipeVerticalXPosition;
    private final int swipeHorizontalYPosition;

    public MobileApplicationConfiguration(Duration swipeStabilizationDuration, int swipeLimit,
            int swipeVerticalXPosition, int swipeHorizontalYPosition)
    {
        validatePosition("x", swipeVerticalXPosition);
        validatePosition("y", swipeHorizontalYPosition);
        this.swipeStabilizationDuration = swipeStabilizationDuration;
        this.swipeLimit = swipeLimit;
        this.swipeVerticalXPosition = swipeVerticalXPosition;
        this.swipeHorizontalYPosition = swipeHorizontalYPosition;
    }

    @SuppressWarnings("MagicNumber")
    private void validatePosition(String name, int value)
    {
        Validate.isTrue(value >= 0 && value <= 100,
            "The %s percentage value must be between 0 and 100, but got: %d", name, value);
    }

    public Duration getSwipeStabilizationDuration()
    {
        return swipeStabilizationDuration;
    }

    public int getSwipeLimit()
    {
        return swipeLimit;
    }

    public int getSwipeVerticalXPosition()
    {
        return swipeVerticalXPosition;
    }

    public int getSwipeHorizontalYPosition()
    {
        return swipeHorizontalYPosition;
    }
}

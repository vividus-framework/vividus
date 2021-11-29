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

package org.vividus.mobileapp.action;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.PointOption;

@SuppressWarnings("rawtypes")
public class PositionCachingTouchAction extends TouchAction<PositionCachingTouchAction>
{
    private RetrievablePointOption position;

    public PositionCachingTouchAction(PerformsTouchActions performsTouchActions)
    {
        super(performsTouchActions);
    }

    @Override
    public PositionCachingTouchAction press(PointOption pressOptions)
    {
        cachePosition(pressOptions);
        return super.press(pressOptions);
    }

    @Override
    public PositionCachingTouchAction moveTo(PointOption moveToOptions)
    {
        cachePosition(moveToOptions);
        return super.moveTo(moveToOptions);
    }

    private void cachePosition(PointOption pointOption)
    {
        if (pointOption instanceof RetrievablePointOption)
        {
            position = (RetrievablePointOption) pointOption;
        }
    }

    public RetrievablePointOption getPosition()
    {
        return position;
    }
}

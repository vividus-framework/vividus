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

package org.vividus.bdd.steps.ui.web.model;

import java.util.Optional;

import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public enum ActionType
{
    DOUBLE_CLICK(false)
    {
        @Override
        public void addAction(Actions actions, Optional<WebElement> element)
        {
            element.ifPresentOrElse(actions::doubleClick, actions::doubleClick);
        }
    },
    CLICK_AND_HOLD(false)
    {
        @Override
        public void addAction(Actions actions, Optional<WebElement> element)
        {
            element.ifPresentOrElse(actions::clickAndHold, actions::clickAndHold);
        }
    },
    MOVE_BY_OFFSET(true)
    {
        @Override
        public void addAction(Actions actions, Point offset)
        {
            actions.moveByOffset(offset.x, offset.y);
        }
    },
    RELEASE(false)
    {
        @Override
        public void addAction(Actions actions, Optional<WebElement> element)
        {
            element.ifPresentOrElse(actions::release, actions::release);
        }
    };

    private final boolean coordinatesRequired;

    ActionType(boolean coordinatesRequired)
    {
        this.coordinatesRequired = coordinatesRequired;
    }

    public boolean isCoordinatesRequired()
    {
        return coordinatesRequired;
    }

    public void addAction(Actions actions, Optional<WebElement> element)
    {
        throw new UnsupportedOperationException();
    }

    public void addAction(Actions actions, Point offset)
    {
        throw new UnsupportedOperationException();
    }
}

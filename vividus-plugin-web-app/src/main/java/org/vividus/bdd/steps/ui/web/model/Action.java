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

import org.jbehave.core.annotations.AsParameters;
import org.openqa.selenium.Point;
import org.vividus.ui.web.action.search.SearchAttributes;

@AsParameters
public class Action
{
    private ActionType type;
    private Optional<SearchAttributes> searchAttributes;
    private Optional<Point> offset;

    public ActionType getType()
    {
        return type;
    }

    public void setType(ActionType type)
    {
        this.type = type;
    }

    public Optional<SearchAttributes> getSearchAttributes()
    {
        return searchAttributes;
    }

    public void setSearchAttributes(Optional<SearchAttributes> searchAttributes)
    {
        this.searchAttributes = searchAttributes;
    }

    public Optional<Point> getOffset()
    {
        return offset;
    }

    public void setOffset(Optional<Point> offset)
    {
        this.offset = offset;
    }
}

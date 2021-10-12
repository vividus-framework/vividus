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

package org.vividus.testdouble;

import java.util.Set;

import org.openqa.selenium.By;
import org.vividus.ui.action.search.ByLocatorSearch;
import org.vividus.ui.action.search.IElementAction;
import org.vividus.ui.action.search.LocatorType;

public enum TestLocatorType implements LocatorType
{
    SEARCH("Search", TestElementSearch.class),
    ADDITIONAL_SEARCH("Additional Search", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.xpath(value);
        }
    },
    COMPETING_SEARCH("Competing Search", TestElementSearch.class),
    FILTER("Filter", TestElementFilter.class),
    ADDITIONAL_FILTER("Additional Filter", TestElementFilter.class),
    COMPETING_FILTER("Competing Filter", TestElementFilter.class);

    private final String attributeName;
    private final Class<? extends IElementAction> actionClass;
    private Set<LocatorType> competingKeys;

    TestLocatorType(String attributeName, Class<? extends IElementAction> actionClass)
    {
        this.attributeName = attributeName;
        this.actionClass = actionClass;
        this.competingKeys = Set.of();
    }

    static
    {
        FILTER.competingKeys = Set.of(COMPETING_FILTER);
        SEARCH.competingKeys = Set.of(COMPETING_SEARCH, COMPETING_FILTER);
    }

    @Override
    public String getKey()
    {
        return this.name();
    }

    @Override
    public String getAttributeName()
    {
        return attributeName;
    }

    @Override
    public Class<? extends IElementAction> getActionClass()
    {
        return actionClass;
    }

    @Override
    public Set<LocatorType> getCompetingTypes()
    {
        return competingKeys;
    }
}

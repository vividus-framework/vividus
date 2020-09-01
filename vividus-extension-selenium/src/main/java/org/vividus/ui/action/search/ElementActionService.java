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

package org.vividus.ui.action.search;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ElementActionService
{
    private final Set<IElementAction> elementActions;

    private final Set<IElementAction> searchActions;
    private final Set<IElementAction> filterActions;

    public ElementActionService(Set<IElementAction> elementActions)
    {
        this.elementActions = elementActions;
        this.searchActions = filterByActionClass(IElementSearchAction.class);
        this.filterActions = filterByActionClass(IElementFilterAction.class);
    }

    @SuppressWarnings("unchecked")
    public <T extends IElementAction> T find(LocatorType actionType)
    {
        return (T) elementActions.stream()
                             .filter(action -> action.getType() == actionType)
                             .findFirst()
                             .orElseThrow(() -> new IllegalArgumentException(
                                 "There is no mapped element action for attribute: " + actionType.getAttributeName()));
    }

    public Set<IElementAction> getSearchActions()
    {
        return searchActions;
    }

    public Set<IElementAction> getFilterActions()
    {
        return filterActions;
    }

    private Set<IElementAction> filterByActionClass(Class<? extends IElementAction> actionClass)
    {
        return elementActions.stream()
                             .filter(t -> actionClass.isAssignableFrom(t.getClass()))
                             .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}

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

package org.vividus.ui.web.action.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SearchAttributes
{
    private final IActionAttributeType searchAttributeType;
    private final SearchParameters searchParameters;
    private final Map<IActionAttributeType, List<String>> filterAttributes = new LinkedHashMap<>();
    private final List<SearchAttributes> childSearchAttributes = new ArrayList<>();

    public SearchAttributes(IActionAttributeType searchAttributeType, SearchParameters searchParameters)
    {
        if (searchAttributeType.getSearchLocatorBuilder() == null)
        {
            checkIfApplicable(searchAttributeType, IElementSearchAction.class, "Search");
        }
        this.searchAttributeType = searchAttributeType;
        this.searchParameters = searchParameters;
    }

    public SearchAttributes(ActionAttributeType searchAttributeType, String searchValue)
    {
        this(searchAttributeType, new SearchParameters(searchValue));
    }

    public SearchAttributes addFilter(IActionAttributeType filterAttributeType, String filterValue)
    {
        checkIfApplicable(filterAttributeType, IElementFilterAction.class, "Filter");
        checkIfNotCompeting(filterAttributeType);
        List<String> values = filterAttributes.computeIfAbsent(filterAttributeType, k -> new ArrayList<>());
        values.add(filterValue);
        return this;
    }

    private static void checkIfApplicable(IActionAttributeType type, Class<? extends IElementAction> actionClass,
            String actionType)
    {
        if (!actionClass.isAssignableFrom(type.getActionClass()))
        {
            throw new UnsupportedOperationException(actionType + " by attribute '" + type + "' is not supported");
        }
    }

    private void checkIfNotCompeting(IActionAttributeType filterAttributeType)
    {
        for (IActionAttributeType currentFilterAttribute : filterAttributes.keySet())
        {
            checkIfAttributesAreNotCompeting(filterAttributeType, currentFilterAttribute);
        }
        checkIfAttributesAreNotCompeting(filterAttributeType, searchAttributeType);
    }

    private void checkIfAttributesAreNotCompeting(IActionAttributeType supplementAttributeType,
            IActionAttributeType existingAttributeType)
    {
        IActionAttributeType competingAttribute = existingAttributeType.getCompetingType();
        if (competingAttribute != null && competingAttribute == supplementAttributeType)
        {
            throw new UnsupportedOperationException(String.format("Competing attributes: '%1$s' and '%2$s'",
                    supplementAttributeType.getAttributeName(), existingAttributeType.getAttributeName()));
        }
    }

    public IActionAttributeType getSearchAttributeType()
    {
        return searchAttributeType;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public Map<IActionAttributeType, List<String>> getFilterAttributes()
    {
        return filterAttributes;
    }

    @Override
    public String toString()
    {
        StringBuilder message = new StringBuilder();
        appendEntry(message, searchAttributeType, searchParameters);
        message.append(" Visibility: ").append(searchParameters.getVisibility()).append(';');
        filterAttributes.forEach((k, v) -> appendEntry(message, k, v));
        return message.toString();
    }

    private static void appendEntry(StringBuilder builder, IActionAttributeType actionAttribute, Object value)
    {
        builder.append(' ').append(actionAttribute.getAttributeName()).append(": '").append(value).append("';");
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(searchAttributeType, searchParameters, filterAttributes);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof SearchAttributes))
        {
            return false;
        }
        SearchAttributes other = (SearchAttributes) obj;
        return searchAttributeType == other.searchAttributeType
                && Objects.equals(searchParameters, other.searchParameters)
                && Objects.equals(filterAttributes, other.filterAttributes);
    }

    public List<SearchAttributes> getChildSearchAttributes()
    {
        return Collections.unmodifiableList(childSearchAttributes);
    }

    public void addChildSearchAttributes(SearchAttributes childSearchAttributes)
    {
        this.childSearchAttributes.add(childSearchAttributes);
    }
}

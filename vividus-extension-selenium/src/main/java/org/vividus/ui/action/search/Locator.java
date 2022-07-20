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

package org.vividus.ui.action.search;

import static org.apache.commons.lang3.StringUtils.SPACE;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Locator
{
    private static final char QUOTE = '\'';

    private final LocatorType locatorType;
    private final SearchParameters searchParameters;
    private final Map<LocatorType, List<String>> filterAttributes = new LinkedHashMap<>();

    public Locator(LocatorType locatorType, SearchParameters searchParameters)
    {
        checkIfApplicable(locatorType, IElementSearchAction.class, "Search");
        this.locatorType = locatorType;
        this.searchParameters = searchParameters;
    }

    public Locator(LocatorType locatorType, String searchValue)
    {
        this(locatorType, new SearchParameters(searchValue));
    }

    public Locator addFilter(LocatorType filterAttributeType, String filterValue)
    {
        checkIfApplicable(filterAttributeType, IElementFilterAction.class, "Filter");
        checkIfNotCompeting(filterAttributeType);
        List<String> values = filterAttributes.computeIfAbsent(filterAttributeType, k -> new ArrayList<>());
        values.add(filterValue);
        return this;
    }

    private static void checkIfApplicable(LocatorType type, Class<? extends IElementAction> actionClass,
            String actionType)
    {
        if (!actionClass.isAssignableFrom(type.getActionClass()))
        {
            throw new UnsupportedOperationException(actionType + " by attribute '" + type + "' is not supported");
        }
    }

    private void checkIfNotCompeting(LocatorType filterAttributeType)
    {
        for (LocatorType currentFilterAttribute : filterAttributes.keySet())
        {
            checkIfAttributesAreNotCompeting(filterAttributeType, currentFilterAttribute);
        }
        checkIfAttributesAreNotCompeting(filterAttributeType, locatorType);
    }

    private void checkIfAttributesAreNotCompeting(LocatorType supplementAttributeType,
            LocatorType existingAttributeType)
    {
        Set<LocatorType> competingTypes = existingAttributeType.getCompetingTypes();
        if (!competingTypes.isEmpty() && competingTypes.contains(supplementAttributeType))
        {
            throw new UnsupportedOperationException(String.format("Competing attributes: '%1$s' and '%2$s'",
                    supplementAttributeType.getAttributeName(), existingAttributeType.getAttributeName()));
        }
    }

    public LocatorType getLocatorType()
    {
        return locatorType;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public Map<LocatorType, List<String>> getFilterAttributes()
    {
        return filterAttributes;
    }

    /**
     * Returns string representation of the locator
     * @deprecated use {@link #toHumanReadableString()}
     */
    @Deprecated(forRemoval = true, since = "0.2.9")
    @Override
    public String toString()
    {
        StringBuilder message = new StringBuilder();
        appendEntry(message, locatorType, searchParameters);
        message.append(" Visibility: ").append(searchParameters.getVisibility()).append(';');
        filterAttributes.forEach((k, v) -> appendEntry(message, k, v));
        return message.toString();
    }

    private static void appendEntry(StringBuilder builder, LocatorType actionAttribute, Object value)
    {
        builder.append(' ').append(actionAttribute.getAttributeName()).append(": '").append(value).append("';");
    }

    public String toHumanReadableString()
    {
        StringBuilder message = new StringBuilder();
        message.append(locatorType.getAttributeName().toLowerCase())
               .append(SPACE).append(QUOTE).append(searchParameters.getValue()).append(QUOTE)
               .append(SPACE).append('(').append(searchParameters.getVisibility().getDescription()).append(')');

        if (!filterAttributes.isEmpty())
        {
            message.append(SPACE).append("with").append(SPACE);

            filterAttributes.entrySet()
                            .stream()
                            .map(e -> e.getKey().getAttributeName().toLowerCase() + SPACE + join(e.getValue()))
                            .collect(Collectors.collectingAndThen(Collectors.joining(" and "), message::append));
        }
        return message.toString();
    }

    private static String join(List<String> filterValues)
    {
        return filterValues.stream().map(v -> QUOTE + v + QUOTE).collect(Collectors.joining(", "));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(locatorType, searchParameters, filterAttributes);
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
        if (!(obj instanceof Locator))
        {
            return false;
        }
        Locator other = (Locator) obj;
        return Objects.equals(locatorType, other.locatorType)
                && Objects.equals(searchParameters, other.searchParameters)
                && Objects.equals(filterAttributes, other.filterAttributes);
    }
}

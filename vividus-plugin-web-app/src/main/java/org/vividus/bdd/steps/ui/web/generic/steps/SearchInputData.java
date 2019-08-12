/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.generic.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.EnumUtils;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.steps.ui.web.util.FormatUtil;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.IActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;

public class SearchInputData
{
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String ATTRIBUTE_NAME = "attributeName";

    private final Map<String, String> additionalParameters = new HashMap<>();
    private SearchAttributes searchAttributes;

    public SearchInputData(Parameters row, IActionAttributeType... attributeTypes)
    {
        for (String key : row.values().keySet())
        {
            String value = row.valueAs(key, String.class, null);
            String searchParametersKey = FormatUtil.changeCamelToUpperUnderscore(key);
            if (EnumUtils.isValidEnum(ActionAttributeType.class, searchParametersKey))
            {
                ActionAttributeType type = ActionAttributeType.valueOf(searchParametersKey);
                if (ArrayUtils.contains(attributeTypes, type) && !value.isEmpty())
                {
                    fillSearchAttributes(type, value);
                }
            }
            else
            {
                additionalParameters.put(key, value);
            }
        }
        if (searchAttributes != null)
        {
            searchAttributes.getSearchParameters().setDisplayedOnly(isVisible());
        }
    }

    private void fillSearchAttributes(ActionAttributeType type, String value)
    {
        if (searchAttributes == null)
        {
            SearchParameters parameters = new SearchParameters(value);
            searchAttributes = new SearchAttributes(type, parameters);
        }
        else
        {
            searchAttributes.addFilter(type, value);
        }
    }

    public SearchAttributes getSearchAttributes()
    {
        return searchAttributes;
    }

    public WebElementAttribute getAttribute()
    {
        return new WebElementAttribute(additionalParameters.get(ATTRIBUTE_NAME),
                additionalParameters.get(ATTRIBUTE_VALUE));
    }

    public String getCssProperty()
    {
        return additionalParameters.get("cssProperty");
    }

    public String getCssValue()
    {
        return additionalParameters.get("cssValue");
    }

    public String getCssValuePart()
    {
        return additionalParameters.get("cssValuePart");
    }

    public Integer getAbsWidth()
    {
        String absWidth = additionalParameters.get("absWidth");
        return null != absWidth ? Integer.valueOf(absWidth) : null;
    }

    public final boolean isVisible()
    {
        String visibility = additionalParameters.get("visibility");
        return null == visibility || Boolean.parseBoolean(visibility);
    }

    public List<WebElementAttribute> getAdditionalAttributes()
    {
        int attributeNamePrefixLength = ATTRIBUTE_NAME.length();
        List<WebElementAttribute> additionalAttributes = new ArrayList<>();
        for (Entry<String, String> entry : additionalParameters.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(ATTRIBUTE_NAME) && !key.equals(ATTRIBUTE_NAME))
            {
                String attributeValue = additionalParameters
                        .get(ATTRIBUTE_VALUE + key.substring(attributeNamePrefixLength));
                additionalAttributes.add(new WebElementAttribute(entry.getValue(), attributeValue));
            }
        }
        return additionalAttributes;
    }
}

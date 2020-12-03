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

package org.vividus.ui.mobile.action.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.LocatorType;

public class ElementAttributeFilter implements IElementFilterAction
{
    @Override
    public LocatorType getType()
    {
        return AppiumLocatorType.ATTRIBUTE;
    }

    @Override
    public boolean matches(WebElement element, String value)
    {
        String[] values = StringUtils.splitPreserveAllTokens(value, "=", 2);
        int length = values.length;

        Validate.isTrue(values.length > 0, "Attribute value can not be empty, expected formats are:"
                + "\n* attribute - an element has the attribute with any value"
                + "\n* attribute= - an element has the attribute with an empty value"
                + "\n* attribute=value - an element has the attribute with the value");

        String attributeValue = element.getAttribute(values[0]);
        return length > 1 ? values[1].equals(attributeValue) : attributeValue != null;
    }
}

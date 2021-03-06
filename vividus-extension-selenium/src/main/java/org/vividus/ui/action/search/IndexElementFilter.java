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

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.openqa.selenium.WebElement;

public class IndexElementFilter implements IElementFilterAction
{
    @Override
    public LocatorType getType()
    {
        return GenericLocatorType.INDEX;
    }

    @Override
    public List<WebElement> filter(List<WebElement> elements, String value)
    {
        isTrue(NumberUtils.isDigits(value), "Index must be a positive number");
        int index = Integer.parseInt(value) - 1;
        isTrue(index >= 0, "Index must be greater than zero");
        return index < elements.size() ? List.of(elements.get(index)) : List.of();
    }

    @Override
    public boolean matches(WebElement element, String value)
    {
        throw new UnsupportedOperationException();
    }
}

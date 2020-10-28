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

import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.web.action.IJavascriptActions;

public class FieldTextPartFilter implements IElementFilterAction
{
    private final IJavascriptActions javascriptActions;

    public FieldTextPartFilter(IJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    @Override
    public boolean matches(WebElement element, String textPart)
    {
        return javascriptActions.getElementValue(element).contains(textPart);
    }

    @Override
    public LocatorType getType()
    {
        return WebLocatorType.FIELD_TEXT_PART;
    }
}

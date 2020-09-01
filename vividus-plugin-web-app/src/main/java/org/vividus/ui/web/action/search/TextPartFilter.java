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

import javax.inject.Inject;

import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IWebElementActions;

public class TextPartFilter extends AbstractElementFilterAction
{
    @Inject private IWebElementActions webElementActions;

    public TextPartFilter()
    {
        super(WebLocatorType.TEXT_PART);
    }

    @Override
    protected boolean matches(WebElement element, String textPart)
    {
        return webElementActions.getElementText(element).contains(textPart);
    }
}

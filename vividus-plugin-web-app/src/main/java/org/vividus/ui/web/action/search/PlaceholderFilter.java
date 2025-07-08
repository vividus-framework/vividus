/*
 * Copyright 2019-2024 the original author or authors.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.LocatorType;

@Deprecated(since = "0.6.14", forRemoval = true)
public class PlaceholderFilter implements IElementFilterAction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PlaceholderFilter.class);

    @Override
    public boolean matches(WebElement element, String placeholder)
    {
        LOGGER.warn("'placeholder' filter is deprecated  and will be removed in VIVIDUS 0.7.0");
        return placeholder.equals(element.getAttribute("placeholder"));
    }

    @Override
    public LocatorType getType()
    {
        return WebLocatorType.PLACEHOLDER;
    }
}

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

package org.vividus.bdd.steps.ui.web;

import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.util.LocatorUtil;

public final class ElementPattern
{
    public static final String LOCAL_NAME_INPUT = ".//*[(local-name() = 'input' ";
    public static final String LINK_IMAGE_TOOLTIP_PATTERN = ".//a[./img[@alt=%1$s or @title=%1$s]]";
    public static final String SELECT_PATTERN = ".//select[@*=%s]";
    public static final String LABEL_PATTERN = ".//label[text()=%1$s or *=%1$s or @*=%1$s]";
    public static final String RADIO_OPTION_INPUT_PATTERN = ".//input[@type='radio' and @id=%s]";

    private ElementPattern()
    {
    }

    public static SearchAttributes getFrameSearchAttributes(String attributeType, String attributeValue)
    {
        String xPath = LocatorUtil.getXPath(
                ".//*[(local-name()='frame' or local-name()='iframe') and @" + attributeType + "=%s]", attributeValue);
        return new SearchAttributes(ActionAttributeType.XPATH, xPath);
    }
}

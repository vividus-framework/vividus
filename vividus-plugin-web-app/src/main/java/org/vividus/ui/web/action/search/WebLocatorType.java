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

import java.util.Set;

import org.vividus.ui.action.search.GenericTextFilter;
import org.vividus.ui.action.search.IElementAction;
import org.vividus.ui.action.search.LocatorType;

public enum WebLocatorType implements LocatorType
{
    LINK_TEXT("Link text", LinkTextSearch.class),
    LINK_URL("URL", LinkUrlSearch.class),
    LINK_URL_PART("URL part", LinkUrlPartSearch.class),
    CASE_SENSITIVE_TEXT("Case sensitive text", CaseSensitiveTextSearch.class),
    CASE_INSENSITIVE_TEXT("Case insensitive text", CaseInsensitiveTextSearch.class),
    TOOLTIP("Tooltip", TooltipFilter.class),
    CSS_SELECTOR("CSS selector", BySeleniumLocatorSearch.class),
    IMAGE_SRC("Image source", ImageWithSourceSearch.class),
    IMAGE_SRC_PART("Image source part", ImageWithSourcePartFilter.class),
    BUTTON_NAME("Button name", ButtonNameSearch.class),
    FIELD_NAME("Field name", FieldNameSearch.class),
    TEXT_PART("Text part", GenericTextFilter.class),
    PLACEHOLDER("Placeholder", PlaceholderFilter.class),
    STATE("State", StateFilter.class),
    DROP_DOWN_STATE("Drop down state", DropDownStateFilter.class),
    VALIDATION_ICON_SOURCE("Validation icon source", ValidationIconSourceFilter.class),
    RELATIVE_TO_PARENT_WIDTH("Relative to parent width", RelativeToParentWidthFilter.class),
    CLASS_ATTRIBUTE_PART("Attribute class part", ClassAttributePartFilter.class),
    CHECKBOX_NAME("Checkbox name", CheckboxNameSearch.class),
    FIELD_TEXT("Field text", FieldTextFilter.class),
    FIELD_TEXT_PART("Field text part", FieldTextPartFilter.class),
    DROP_DOWN_TEXT("Drop down text", DropDownTextFilter.class),
    ELEMENT_NAME("Element name", ElementNameSearch.class),
    NAME("Name", ElementNameSearch.class),
    PARTIAL_LINK_TEXT("Partial link text", PartialLinkTextSearch.class),
    CLASS_NAME("Class name", BySeleniumLocatorSearch.class),
    XPATH("XPath", XpathSearch.class),
    ID("Id", BySeleniumLocatorSearch.class),
    TAG_NAME("Tag name", BySeleniumLocatorSearch.class);

    private final String attributeName;
    private final Class<? extends IElementAction> actionClass;
    private Set<LocatorType> competingKeys;

    WebLocatorType(String attributeName, Class<? extends IElementAction> actionClass)
    {
        this.attributeName = attributeName;
        this.actionClass = actionClass;
        this.competingKeys = Set.of();
    }

    static
    {
        CASE_INSENSITIVE_TEXT.competingKeys = Set.of(TEXT_PART);
        TEXT_PART.competingKeys = Set.of(CASE_INSENSITIVE_TEXT, CASE_SENSITIVE_TEXT);
        CASE_SENSITIVE_TEXT.competingKeys = Set.of(TEXT_PART);
        IMAGE_SRC.competingKeys = Set.of(IMAGE_SRC_PART);
        IMAGE_SRC_PART.competingKeys = Set.of(IMAGE_SRC);
        LINK_URL.competingKeys = Set.of(LINK_URL_PART);
        LINK_URL_PART.competingKeys = Set.of(LINK_URL);
        FIELD_TEXT.competingKeys = Set.of(FIELD_TEXT_PART);
        FIELD_TEXT_PART.competingKeys = Set.of(FIELD_TEXT);
    }

    @Override
    public String getKey()
    {
        return this.name();
    }

    @Override
    public String getAttributeName()
    {
        return attributeName;
    }

    @Override
    public Class<? extends IElementAction> getActionClass()
    {
        return actionClass;
    }

    @Override
    public Set<LocatorType> getCompetingTypes()
    {
        return competingKeys;
    }
}

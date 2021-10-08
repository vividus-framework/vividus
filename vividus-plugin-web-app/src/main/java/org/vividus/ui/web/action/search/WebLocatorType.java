/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codeborne.selenide.selector.ByShadow;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.vividus.ui.action.search.ByLocatorSearch;
import org.vividus.ui.action.search.GenericTextFilter;
import org.vividus.ui.action.search.IElementAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.web.util.LocatorUtil;

public enum WebLocatorType implements LocatorType
{
    LINK_TEXT("Link text", LinkTextSearch.class),
    LINK_URL("URL", LinkUrlSearch.class),
    LINK_URL_PART("URL part", LinkUrlPartSearch.class),
    CASE_SENSITIVE_TEXT("Case sensitive text", CaseSensitiveTextSearch.class),
    CASE_INSENSITIVE_TEXT("Case insensitive text", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return AbstractWebElementSearchAction.generateCaseInsensitiveLocator(value.toLowerCase(), "*");
        }
    },
    TOOLTIP("Tooltip", TooltipFilter.class)
    {
        @Override
        public By buildBy(String value)
        {
            return LocatorUtil.getXPathLocator(".//*[@title=%s]", value);
        }
    },
    CSS_SELECTOR("CSS selector", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.cssSelector(value);
        }
    },
    IMAGE_SRC("Image source", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return LocatorUtil.getXPathLocator(".//img[@src='%s']", value);
        }
    },
    IMAGE_SRC_PART("Image source part", ImageWithSourcePartFilter.class)
    {
        @Override
        public By buildBy(String value)
        {
            return LocatorUtil.getXPathLocator(".//img[contains(@src,'%s')]", value);
        }
    },
    BUTTON_NAME("Button name", ButtonNameSearch.class),
    FIELD_NAME("Field name", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            // due to firefox bug, we can't use name() and must use local-name()
            // as workaround 'body' represents CKE editor
            return LocatorUtil.getXPathLocator(".//*[(local-name() = 'input' or local-name() = 'textarea' or "
                    + "local-name()='body') and ((@* | text())=%s)]", value);
        }
    },
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
    PARTIAL_LINK_TEXT("Partial link text", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return LocatorUtil.getXPathLocatorByInnerTextWithTagName("a", value);
        }
    },
    CLASS_NAME("Class name", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.className(value);
        }
    },
    XPATH("XPath", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.xpath(LocatorUtil.getXPath(value));
        }
    },
    ID("Id", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.id(value);
        }
    },
    TAG_NAME("Tag name", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.tagName(value);
        }
    },
    SHADOW_CSS_SELECTOR("Shadow CSS selector", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String searchValue)
        {
            List<String> searchValues = Stream.of(StringUtils.split(searchValue, ';'))
                    .map(String::strip)
                    .collect(Collectors.toList());
            String targetValue = searchValues.get(searchValues.size() - 1);
            String upperShadowHost = searchValues.get(0);
            String[] innerShadowHosts = searchValues.stream().skip(1)
                    .limit(searchValues.size() - 2)
                    .toArray(String[]::new);

            return ByShadow.cssSelector(targetValue, upperShadowHost, innerShadowHosts);
        }
    };

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

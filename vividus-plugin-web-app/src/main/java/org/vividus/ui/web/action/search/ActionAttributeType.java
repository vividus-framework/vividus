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

import static org.vividus.ui.web.action.search.AbstractElementSearchAction.generateCaseInsensitiveLocator;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.support.How;
import org.vividus.ui.web.util.LocatorUtil;

public enum ActionAttributeType implements IActionAttributeType
{
    LINK_TEXT("Link text", LinkTextSearch.class),
    LINK_URL("URL", LinkUrlSearch.class),
    LINK_URL_PART("URL part", LinkUrlPartSearch.class),
    CASE_SENSITIVE_TEXT("Case sensitive text", CaseSensitiveTextSearch.class),
    CASE_INSENSITIVE_TEXT("Case insensitive text",
        parameters -> generateCaseInsensitiveLocator(parameters.getValue().toLowerCase(), "*")),
    TOOLTIP("Tooltip",
        parameters -> LocatorUtil.getXPathLocator(".//*[@title=%s]", parameters.getValue()),
        TooltipFilter.class),
    DEFAULT("", DefaultSearch.class),
    XPATH("XPath", parameters -> How.XPATH.buildBy(parameters.getValue())),
    CSS_SELECTOR("CSS selector", parameters -> How.CSS.buildBy(parameters.getValue())),
    TAG_NAME("Tag name", parameters -> How.TAG_NAME.buildBy(parameters.getValue())),
    IMAGE_SRC("Image source",
        parameters -> LocatorUtil.getXPathLocator(".//img[@src='%s']", parameters.getValue())),
    IMAGE_SRC_PART("Image source part",
        parameters -> LocatorUtil.getXPathLocator(".//img[contains(@src,'%s')]", parameters.getValue()),
        ImageWithSourcePartFilter.class),
    BUTTON_NAME("Button name", ButtonNameSearch.class),
    FIELD_NAME("Field name",
        // due to firefox bug, we can't use name() and must use local-name() as workaround 'body' represents CKE editor
        parameters -> LocatorUtil.getXPathLocator(".//*[(local-name() = 'input' or local-name() = 'textarea' or "
                + "local-name()='body') and ((@* | text())=%s)]", parameters.getValue())),
    TEXT_PART("Text part", TextPartFilter.class),
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
    ID("Id", parameters -> How.ID.buildBy(parameters.getValue())),
    CLASS_NAME("Class name", parameters -> How.CLASS_NAME.buildBy(parameters.getValue()));

    private static final Set<ActionAttributeType> SEARCH_TYPES;
    private static final Set<ActionAttributeType> FILTER_TYPES;

    private final String attributeName;
    private final Class<? extends IElementAction> actionClass;
    private final transient Function<SearchParameters, By> searchLocatorBuilder;
    private IActionAttributeType competingType;

    ActionAttributeType(String attributeName, Class<? extends IElementAction> actionClass)
    {
        this.attributeName = attributeName;
        this.actionClass = actionClass;
        searchLocatorBuilder = null;
    }

    ActionAttributeType(String attributeName, Function<SearchParameters, By> searchLocatorBuilder)
    {
        this(attributeName, searchLocatorBuilder, null);
    }

    ActionAttributeType(String attributeName, Function<SearchParameters, By> searchLocatorBuilder,
            Class<? extends IElementFilterAction> filterActionClass)
    {
        this.attributeName = attributeName;
        this.actionClass = filterActionClass;
        this.searchLocatorBuilder = searchLocatorBuilder;
    }

    static
    {
        CASE_INSENSITIVE_TEXT.competingType = TEXT_PART;
        TEXT_PART.competingType = CASE_INSENSITIVE_TEXT;
        CASE_SENSITIVE_TEXT.competingType = TEXT_PART;
        TEXT_PART.competingType = CASE_SENSITIVE_TEXT;
        IMAGE_SRC.competingType = IMAGE_SRC_PART;
        IMAGE_SRC_PART.competingType = IMAGE_SRC;
        LINK_URL.competingType = LINK_URL_PART;
        LINK_URL_PART.competingType = LINK_URL;
        FIELD_TEXT.competingType = FIELD_TEXT_PART;
        FIELD_TEXT_PART.competingType = FIELD_TEXT;

        SEARCH_TYPES = filterByActionClass(IElementSearchAction.class, t -> Objects.nonNull(t.searchLocatorBuilder));
        FILTER_TYPES = filterByActionClass(IElementFilterAction.class, t -> false);
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
    public Function<SearchParameters, By> getSearchLocatorBuilder()
    {
        return searchLocatorBuilder;
    }

    @Override
    public IActionAttributeType getCompetingType()
    {
        return competingType;
    }

    // Checkstyle bug: https://github.com/sevntu-checkstyle/sevntu.checkstyle/issues/166
    @SuppressWarnings("checkstyle:SimpleAccessorNameNotation")
    public static Set<ActionAttributeType> getSearchTypes()
    {
        return SEARCH_TYPES;
    }

    // Checkstyle bug: https://github.com/sevntu-checkstyle/sevntu.checkstyle/issues/166
    @SuppressWarnings("checkstyle:SimpleAccessorNameNotation")
    public static Set<ActionAttributeType> getFilterTypes()
    {
        return FILTER_TYPES;
    }

    private static Set<ActionAttributeType> filterByActionClass(Class<? extends IElementAction> actionClass,
            Predicate<ActionAttributeType> orPredicate)
    {
        return Stream.of(values())
                .filter(t -> t.getActionClass() != null && actionClass.isAssignableFrom(t.getActionClass())
                        || orPredicate.test(t))
                .collect(Collectors.toSet());
    }
}

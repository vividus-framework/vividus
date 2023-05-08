/*
 * Copyright 2019-2023 the original author or authors.
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

import org.openqa.selenium.By;
import org.vividus.ui.action.search.ByLocatorSearch;
import org.vividus.ui.action.search.GenericTextFilter;
import org.vividus.ui.action.search.IElementAction;
import org.vividus.ui.action.search.LocatorType;

import io.appium.java_client.AppiumBy;

public enum AppiumLocatorType implements LocatorType
{
    XPATH("Appium XPath", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return By.xpath(value);
        }
    },
    ACCESSIBILITY_ID("Accessibility Id", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return AppiumBy.accessibilityId(value);
        }
    },
    IOS_CLASS_CHAIN("iOS Class Chain", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return AppiumBy.iOSClassChain(value);
        }
    },
    IOS_NS_PREDICATE("iOS NS Predicate", ByLocatorSearch.class)
    {
        @Override
        public By buildBy(String value)
        {
            return AppiumBy.iOSNsPredicateString(value);
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
    TEXT_PART("Text part", GenericTextFilter.class),
    TEXT("Text", GenericTextFilter.class);

    private final String attributeName;
    private final Class<? extends IElementAction> actionClass;

    AppiumLocatorType(String attributeName, Class<? extends IElementAction> actionClass)
    {
        this.attributeName = attributeName;
        this.actionClass = actionClass;
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
}

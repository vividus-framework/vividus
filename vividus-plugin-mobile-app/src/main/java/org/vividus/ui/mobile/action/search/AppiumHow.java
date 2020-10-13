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

import org.openqa.selenium.By;
import org.openqa.selenium.support.How;

import io.appium.java_client.MobileBy;

public enum AppiumHow
{
    XPATH
    {
        @Override
        public By buildBy(String value)
        {
            return How.XPATH.buildBy(value);
        }
    },
    ACCESSIBILITY_ID
    {
        @Override
        public By buildBy(String value)
        {
            return MobileBy.AccessibilityId(value);
        }
    },
    IOS_CLASS_CHAIN
    {
        @Override
        public By buildBy(String value)
        {
            return MobileBy.iOSClassChain(value);
        }
    };

    public abstract By buildBy(String value);
}

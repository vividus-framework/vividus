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

package org.vividus.ui.web.playwright.steps;

import com.microsoft.playwright.Locator;

import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;

public enum ElementState
{
    ENABLED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementEnabled(locator, false);
        }
    },
    DISABLED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementDisabled(locator, false);
        }
    },
    SELECTED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementSelected(locator, false);
        }
    },
    NOT_SELECTED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementNotSelected(locator, false);
        }
    },
    VISIBLE
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementVisible(locator, false);
        }
    },
    NOT_VISIBLE
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementHidden(locator, false);
        }
    };

    public abstract void assertElementState(Locator locator);
}

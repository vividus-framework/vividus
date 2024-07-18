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

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementEnabled(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return locator.isEnabled(new Locator.IsEnabledOptions().setTimeout(NO_WAIT_TIMEOUT));
        }
    },
    DISABLED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementDisabled(locator, false);
        }

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementDisabled(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return locator.isDisabled(new Locator.IsDisabledOptions().setTimeout(NO_WAIT_TIMEOUT));
        }
    },
    SELECTED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementSelected(locator, false);
        }

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementSelected(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return isElementSelected(locator);
        }
    },
    NOT_SELECTED
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementNotSelected(locator, false);
        }

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementNotSelected(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return !isElementSelected(locator);
        }
    },
    VISIBLE
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementVisible(locator, false);
        }

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementVisible(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return locator.isVisible();
        }
    },
    NOT_VISIBLE
    {
        @Override
        public void assertElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementHidden(locator, false);
        }

        @Override
        public void waitForElementState(Locator locator)
        {
            PlaywrightLocatorAssertions.assertElementHidden(locator, true);
        }

        @Override
        public boolean isElementState(Locator locator)
        {
            return locator.isHidden();
        }
    };

    private static final int NO_WAIT_TIMEOUT = 0;

    public abstract void assertElementState(Locator locator);

    public abstract void waitForElementState(Locator locator);

    public abstract boolean isElementState(Locator locator);

    private static boolean isElementSelected(Locator locator)
    {
        return locator.isChecked(new Locator.IsCheckedOptions().setTimeout(NO_WAIT_TIMEOUT));
    }
}

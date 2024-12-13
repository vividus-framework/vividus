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

package org.vividus.ui.web.playwright.assertions;

import static org.vividus.ui.web.playwright.PlaywrightAssertionConfiguration.ASSERTION_NO_WAIT_TIMEOUT;

import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

public final class PlaywrightLocatorAssertions
{
    private PlaywrightLocatorAssertions()
    {
    }

    public static void assertElementHasTextMatchingRegex(Locator locator, Pattern pattern, boolean waitForState)
    {
        LocatorAssertions.ContainsTextOptions options = waitForState ? null
                : new LocatorAssertions.ContainsTextOptions().setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        getLocatorAssertions(locator).containsText(pattern, options);
    }

    public static void assertElementVisible(Locator locator, boolean waitForState)
    {
        LocatorAssertions.IsVisibleOptions options = waitForState ? null : new LocatorAssertions.IsVisibleOptions()
                .setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        getLocatorAssertions(locator).isVisible(options);
    }

    public static void assertElementHidden(Locator locator, boolean waitForState)
    {
        LocatorAssertions.IsHiddenOptions options = waitForState ? null : new LocatorAssertions.IsHiddenOptions()
                .setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        getLocatorAssertions(locator).isHidden(options);
    }

    public static void assertElementEnabled(Locator locator, boolean waitForState)
    {
        LocatorAssertions.IsEnabledOptions options = waitForState ? null : new LocatorAssertions.IsEnabledOptions()
                .setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        getLocatorAssertions(locator).isEnabled(options);
    }

    public static void assertElementDisabled(Locator locator, boolean waitForState)
    {
        LocatorAssertions.IsDisabledOptions options = waitForState ? null : new LocatorAssertions.IsDisabledOptions()
                .setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        getLocatorAssertions(locator).isDisabled(options);
    }

    public static void assertElementSelected(Locator locator, boolean waitForState)
    {
        assertElementSelected(locator, true, waitForState);
    }

    public static void assertElementNotSelected(Locator locator, boolean waitForState)
    {
        assertElementSelected(locator, false, waitForState);
    }

    private static void assertElementSelected(Locator locator, boolean selected, boolean waitForState)
    {
        LocatorAssertions.IsCheckedOptions options = new LocatorAssertions.IsCheckedOptions();
        options.setChecked(selected);
        if (!waitForState)
        {
            options.setTimeout(ASSERTION_NO_WAIT_TIMEOUT);
        }
        getLocatorAssertions(locator).isChecked(options);
    }

    private static LocatorAssertions getLocatorAssertions(Locator locator)
    {
        return PlaywrightAssertions.assertThat(locator);
    }
}

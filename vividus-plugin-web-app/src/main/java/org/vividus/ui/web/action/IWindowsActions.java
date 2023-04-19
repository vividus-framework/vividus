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

package org.vividus.ui.web.action;

import org.hamcrest.Matcher;

public interface IWindowsActions
{
    /**
     * Closing all tabs and left only larger one
     */
    void closeAllTabsExceptOne();

    /**
     * Switches to a new tab.
     * If there is no new tabs stays on current.
     * @param currentTab identifier
     * @return window identifier
     */
    String switchToNewTab(String currentTab);

    /**
     * Switches to a tab with a title matching matcher or else
     * last available tab.
     * @param matcher to match tab title
     * @return tab title
     */
    String switchToTabWithMatchingTitle(Matcher<String> matcher);

    /**
     * Switches to a previous tab
     */
    void switchToPreviousTab();
}

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

package org.vividus.ui.web.action;

import org.hamcrest.Matcher;

public interface IWindowsActions
{
    /**
     * Closing all windows and left only larger one
     */
    void closeAllWindowsExceptOne();

    /**
     * Switches to a new window.
     * If there is no new window stays on current.
     * @param currentWindow identifier
     * @return window identifier
     */
    String switchToNewWindow(String currentWindow);

    /**
     * Switches to a window with a title matching matcher or else
     * last available window.
     * @param matcher to match window title
     * @return window title
     */
    String switchToWindowWithMatchingTitle(Matcher<String> matcher);

    /**
     * Switches to a previous window
     */
    void switchToPreviousWindow();
}

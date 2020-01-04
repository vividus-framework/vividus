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

package org.vividus.ui.web.listener;

import org.vividus.ui.web.action.JavascriptActions;

public enum AlertHandlingOptions
{
    ACCEPT
    {
        @Override
        public void selectOptionForAlertHandling(JavascriptActions javascriptActions, String alertHandlingScript)
        {
            javascriptActions.executeScript(alertHandlingScript, Boolean.TRUE);
        }
    },
    DISMISS
    {
        @Override
        public void selectOptionForAlertHandling(JavascriptActions javascriptActions, String alertHandlingScript)
        {
            javascriptActions.executeScript(alertHandlingScript, Boolean.FALSE);
        }
    },
    DO_NOTHING
    {
        @Override
        public void selectOptionForAlertHandling(JavascriptActions javascriptActions, String alertHandlingScript)
        {
            // Nothing to do
        }
    };

    protected abstract void selectOptionForAlertHandling(JavascriptActions javascriptActions,
            String alertHandlingScript);
}

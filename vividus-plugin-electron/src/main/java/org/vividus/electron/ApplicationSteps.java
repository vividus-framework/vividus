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

package org.vividus.electron;

import org.jbehave.core.annotations.Given;
import org.vividus.selenium.IWebDriverProvider;

public class ApplicationSteps
{
    private final IWebDriverProvider webDriverProvider;

    public ApplicationSteps(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Starts Electron application by creation of chrome driver session;
     * Please mind that you should pass path to an executable via property:
     * <br> <b>web.driver.CHROME.binary-path</b>
     */
    @Given("I start electron application")
    public void startApplication()
    {
        webDriverProvider.get();
    }
}

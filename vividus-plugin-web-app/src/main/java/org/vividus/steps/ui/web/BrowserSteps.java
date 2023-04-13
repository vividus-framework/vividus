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

package org.vividus.steps.ui.web;

import org.jbehave.core.annotations.When;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverStartContext;
import org.vividus.selenium.WebDriverStartParameters;

public class BrowserSteps
{
    private final WebDriverStartContext webDriverStartContext;
    private final IWebDriverProvider webDriverProvider;

    public BrowserSteps(WebDriverStartContext webDriverStartContext, IWebDriverProvider webDriverProvider)
    {
        this.webDriverStartContext = webDriverStartContext;
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Sets the browser command line arguments for the current test.
     * <div><b>Example:</b></div>
     * <code>
     * When I set browser command line arguments to `--window-size=800,600 --start-in-incognito`
     * </code>
     * @param argsString Command line arguments
     * @see <a href="https://peter.sh/experiments/chromium-command-line-switches/"><i>Chrome command line arguments
     * </i></a>
     * @see <a href="https://www-archive.mozilla.org/docs/command-line-args"><i>Firefox command line arguments
     * </i></a>
     * @see <a href="https://textslashplain.com/2022/01/05/edge-command-line-arguments/"><i>Edge command line arguments
     * </i></a>
     */
    @When("I set browser command line arguments to `$argsString`")
    public void setWebDriverCliArguments(String argsString)
    {
        webDriverStartContext.put(WebDriverStartParameters.COMMAND_LINE_ARGUMENTS, argsString);
    }

    /**
     * Closes the browser
     */
    @When("I close browser")
    public void closeBrowser()
    {
        webDriverProvider.end();
    }
}

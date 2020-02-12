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

package org.vividus.bdd.steps.ui.web;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.context.IWebUiContext;

public class ScrollSteps
{
    private final IWebUiContext webUiContext;
    private final IJavascriptActions javascriptActions;

    public ScrollSteps(IWebUiContext webUiContext, IJavascriptActions javascriptActions)
    {
        this.webUiContext = webUiContext;
        this.javascriptActions = javascriptActions;
    }

    /**
     * Scrolls current context to the picked direction;
     * If context is not set scroll will be applied to a page;
     * @param scrollDirection to apply scroll
     * <br>Possible directions:
     * <br><b>LEFT</b> - start of a page/element horizontally,
     * <br><b>RIGHT</b> - end of a page/element horizontally,
     * <br><b>TOP</b> - start of a page/element vertically,
     * <br><b>BOTTOM</b> - end of a page/element horizontally
     */
    @When("I scroll context to $scrollDirection edge")
    public void scrollContextIn(ScrollDirection scrollDirection)
    {
        scrollDirection.scroll(webUiContext, javascriptActions);
    }

    /**
     * Scrolls to the end of the page with dynamically loading content upon scrolling
     * Before using step, it is necessary to wait until scroll appears if it exists
     * <p>
     * Attribute scroll manages scroll bars in a browser window
     * when the content of a web page exceeds the size of the current window
     * </p>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Scrolls to the end of the page</li>
     * </ul>
     */
    @When("I scroll to the end of the page")
    public void scrollToTheEndOfThePage()
    {
        javascriptActions.scrollToEndOfPage();
    }

    /**
     * Scrolls to the start of the page
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Scrolls to the start of the page</li>
     * </ul>
     */
    @When("I scroll to the start of the page")
    public void scrollToTheStartOfThePage()
    {
        javascriptActions.scrollToStartOfPage();
    }
}

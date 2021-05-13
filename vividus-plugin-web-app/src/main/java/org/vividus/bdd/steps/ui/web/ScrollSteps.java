/*
 * Copyright 2019-2021 the original author or authors.
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
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebJavascriptActions;

public class ScrollSteps
{
    private final IUiContext uiContext;
    private final WebJavascriptActions javascriptActions;
    private final IBaseValidations baseValidaitons;

    public ScrollSteps(IUiContext uiContext, WebJavascriptActions javascriptActions,
            IBaseValidations baseValidations)
    {
        this.uiContext = uiContext;
        this.javascriptActions = javascriptActions;
        this.baseValidaitons = baseValidations;
    }

    /**
     * Scrolls current context to the picked direction;
     * If context is not set scroll will be applied to a page;
     * @param scrollDirection to apply scroll
     * <br>Possible directions:
     * <br><b>LEFT</b> - start of a page/element horizontally,
     * <br><b>RIGHT</b> - end of a page/element horizontally,
     * <br><b>TOP</b> - start of a page/element vertically,
     * <br><b>BOTTOM</b> - end of a page/element vertically
     */
    @When("I scroll context to $scrollDirection edge")
    public void scrollContextIn(ScrollDirection scrollDirection)
    {
        scrollDirection.scroll(uiContext, javascriptActions);
    }

    /**
     * Scroll the element into view by calling API:
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollIntoView">Scroll into view</a>
     * @param locator to locate an element
     */
    @When("I scroll element located `$locator` into view")
    public void scrollIntoView(Locator locator)
    {
        WebElement toScroll = baseValidaitons.assertIfAtLeastOneElementExists("Element to scroll into view", locator);
        if (null != toScroll)
        {
            javascriptActions.scrollIntoView(toScroll, true);
        }
    }
}

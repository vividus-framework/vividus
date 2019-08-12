/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.validation;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.search.SearchAttributes;

public interface ILinkValidations
{
    WebElement assertIfLinkWithTextExists(SearchContext searchContext, String text);

    boolean assertIfLinkWithTextNotExists(SearchContext searchContext, String text);

    WebElement assertIfLinkExists(SearchContext searchContext, SearchAttributes attributes);

    boolean assertIfLinkDoesNotExist(SearchContext searchContext, SearchAttributes attributes);

    WebElement assertIfLinkWithTooltipExists(SearchContext searchContext, String tooltip);

    /**
     * Asserts, if element has href attribute and it matches provided URL
     * @param link Link WebElement
     * @param url Expected URL value
     * @param equals Equals assertion if true and NotEquals if false
     * @return true if assertion is passed, otherwise false
     */
    boolean assertIfLinkHrefMatchesURL(WebElement link, String url, boolean equals);
}

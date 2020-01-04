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

package org.vividus.bdd.steps.ui.web.validation;

import java.util.List;

import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.Dimension;

public interface IElementValidations
{
    boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            Matcher<? super List<WebElement>> matcher);

    boolean assertIfElementContainsText(WebElement element, String text, boolean isTrue);

    boolean assertIfElementContainsTooltip(WebElement element, String expectedTooltip);

    boolean assertAllWebElementsHaveEqualDimension(List<WebElement> elements, Dimension dimension);

    boolean assertIfElementHasWidthInPerc(WebElement parent, WebElement element, int widthInPerc);
}

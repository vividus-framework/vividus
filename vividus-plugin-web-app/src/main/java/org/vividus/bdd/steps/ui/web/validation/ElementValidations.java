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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.Dimension;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.util.ElementUtil;

public class ElementValidations implements IElementValidations
{
    public static final double HUNDRED = 100;
    public static final int ACCURACY = 2;

    @Inject private IWebElementActions webElementActions;
    @Inject private IHighlightingSoftAssert softAssert;

    @Override
    public boolean assertElementNumber(String businessDescription, String systemDescription, List<WebElement> elements,
            Matcher<? super List<WebElement>> matcher)
    {
        return softAssert.withHighlightedElements(elements).assertThat(businessDescription, systemDescription,
                elements, matcher);
    }

    @Override
    public boolean assertIfElementContainsText(WebElement element, String text, boolean isTrue)
    {
        if (element != null)
        {
            StringBuilder description = new StringBuilder("Element ");
            description = isTrue ? description.append("contains") : description.append("does not contain");
            description.append(" text");
            Matcher<String> matcher = isTrue ? containsString(text) : not(containsString(text));
            String actualText = element.getText();
            if (actualText.contains(text) != isTrue)
            {
                String pseudoElementContent = webElementActions.getPseudoElementContent(element);
                if (!pseudoElementContent.isEmpty())
                {
                    actualText = pseudoElementContent;
                }
                String difference = StringUtils.difference(actualText, text);
                if (!difference.isEmpty())
                {
                    description.append(" (Difference in substring: \"").append(difference).append("\")");
                }
            }
            return softAssert.withHighlightedElement(element).assertThat(description.toString(), actualText,
                    matcher);
        }
        return false;
    }

    @Override
    public boolean assertIfElementContainsTooltip(WebElement element, String expectedTooltip)
    {
        return element != null && softAssert.withHighlightedElement(element)
                .assertEquals("Element has correct tooltip", expectedTooltip, element.getAttribute("title"));
    }

    @Override
    public boolean assertAllWebElementsHaveEqualDimension(List<WebElement> elements, Dimension dimension)
    {
        int firstElementDimension = dimension.getDimension(elements.get(0).getSize());
        boolean result = elements.size() > 1;
        for (int i = 1; i < elements.size(); i++)
        {
            WebElement element = elements.get(i);
            result = softAssert.withHighlightedElements(elements).assertEquals(
                    String.format("Element <%1$s:%2$s[%3$s]> has correct '%4$s'", element.getTagName(),
                            element.getText(), i, dimension),
                    firstElementDimension, dimension.getDimension(element.getSize())) && result;
        }
        return result;
    }

    @Override
    public boolean assertIfElementHasWidthInPerc(WebElement parent, WebElement element, int widthInPerc)
    {
        return parent != null && element != null
                && softAssert.withHighlightedElements(Arrays.asList(parent, element)).assertEquals(
                        "Element has correct width", widthInPerc, ElementUtil.getElementWidthInPerc(parent, element),
                        ACCURACY);
    }
}

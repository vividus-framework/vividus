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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.validation.IBaseValidations;
import org.vividus.bdd.steps.ui.web.validation.IElementValidations;
import org.vividus.ui.web.State;
import org.vividus.ui.web.action.IClickActions;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.SearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

@ExtendWith(MockitoExtension.class)
class ImageStepsTests
{
    private static final String THE_FOUND_IMAGE_IS_ENABLED = "The found image is ENABLED";
    private static final String AN_IMAGE_WITH_THE_TOOLTIP_TOOLTIP_IMAGE = "An image with the tooltip 'tooltipImage'";
    private static final String XPATH_IMAGE_NAME =
            ".//img[@*[normalize-space()=\"imageName\"] or text()[normalize-space()=\"imageName\"]]";
    private static final String SRC = "src";
    private static final String SRCPART = "srcpart";
    private static final String AN_IMAGE_WITH_THE_SRC_PATTERN = "An image with the src '%s'";
    private static final String AN_IMAGE_WITH_THE_SRC_PART_PATTERN = "An image with the src part '%s'";
    private static final String TOOLTIP = "tooltip";
    private static final String TOOLTIP_IMAGE = "tooltipImage";
    private static final String IMAGE_NAME = "imageName";
    private static final String IMAGE_TOOLTIP_PATTERN = ".//img[@alt=%1$s or @title=%1$s]";

    @Mock
    private WebElement webElement;

    @Mock
    private IBaseValidations baseValidations;

    @Mock
    private IElementValidations elementValidations;

    @Mock
    private IWebUiContext webUiContext;

    @Mock
    private SearchActions searchActions;

    @Mock
    private IMouseActions mouseActions;

    @Mock
    private IClickActions clickActions;

    @InjectMocks
    private ImageSteps imageSteps;

    @Test
    void testIfImageWithSrcExists()
    {
        imageSteps.ifImageWithSrcExists(SRC);
        verify(baseValidations).assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC));
    }

    @Test
    void testIfStateImageWithSrcExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(webElement);
        imageSteps.ifStateImageWithSrcExists(State.ENABLED, SRC);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullImageWithSrcExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(null);
        imageSteps.ifStateImageWithSrcExists(State.ENABLED, SRC);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_IMAGE_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testDoesNotImageWithSrcExist()
    {
        imageSteps.doesNotImageWithSrcExist(SRC);
        verify(baseValidations).assertIfElementDoesNotExist(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC));
    }

    @Test
    void testIfImageWithSrcPartExists()
    {
        imageSteps.ifImageWithSrcPartExists(SRCPART);
        verify(baseValidations).assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PART_PATTERN, SRCPART),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, SRCPART));
    }

    @Test
    void testIfStateImageWithSrcPartExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PART_PATTERN, SRCPART),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, SRCPART))).thenReturn(webElement);
        imageSteps.ifStateImageWithSrcPartExists(State.ENABLED, SRCPART);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfStateNullImageWithSrcPartExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PART_PATTERN, SRCPART),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, SRCPART))).thenReturn(null);
        imageSteps.ifStateImageWithSrcPartExists(State.ENABLED, SRCPART);
        verify(baseValidations, never()).assertExpectedCondition(THE_FOUND_IMAGE_IS_ENABLED,
                State.ENABLED.getExpectedCondition(webElement));
    }

    @Test
    void testIfImageWithTooltipAndSrcPartExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PART_PATTERN, SRCPART),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, SRCPART))).thenReturn(webElement);
        imageSteps.ifImageWithTooltipAndSrcPartExists(TOOLTIP, SRCPART);
        verify(elementValidations).assertIfElementContainsTooltip(webElement, TOOLTIP);
    }

    @Test
    void testIfImageWithSrcAndTooltipExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(webElement);
        imageSteps.ifImageWithSrcAndTooltipExists(SRC, TOOLTIP);
        verify(baseValidations).assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC));
        verify(elementValidations).assertIfElementContainsTooltip(webElement, TOOLTIP);
    }

    @Test
    void testIfStateImageWithSrcAndTooltipExists()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(webElement);
        imageSteps.ifImageWithSrcAndTooltipExists(State.ENABLED, SRC, TOOLTIP);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfImageWithTooltipExists()
    {
        imageSteps.ifImageWithTooltipExists(TOOLTIP_IMAGE);
        verify(baseValidations).assertIfElementExists(AN_IMAGE_WITH_THE_TOOLTIP_TOOLTIP_IMAGE,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(IMAGE_TOOLTIP_PATTERN, TOOLTIP_IMAGE)));
    }

    @Test
    void testIfImageWithTooltipExistsState()
    {
        when(baseValidations.assertIfElementExists(AN_IMAGE_WITH_THE_TOOLTIP_TOOLTIP_IMAGE,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(IMAGE_TOOLTIP_PATTERN, TOOLTIP_IMAGE))))
                            .thenReturn(webElement);

        imageSteps.ifImageWithTooltipExists(State.ENABLED, TOOLTIP_IMAGE);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfNullImageWithTooltipExistsState()
    {
        imageSteps.ifImageWithTooltipExists(State.ENABLED, TOOLTIP_IMAGE);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, (WebElement) null);
    }

    @Test
    void testIfImageWithUrlAndTooltipExistsState()
    {
        Mockito.lenient().when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC_PART, SRCPART))).thenReturn(webElement);
        imageSteps.ifImageWithUrlAndTooltipExists(State.ENABLED, SRC, TOOLTIP_IMAGE);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, (WebElement) null);
    }

    @Test
    void testIfNullImageWithUrlAndTooltipExistsState()
    {
        ImageSteps spy = Mockito.spy(imageSteps);
        when(spy.ifImageWithSrcExists(SRC)).thenReturn(webElement);
        when(elementValidations.assertIfElementContainsTooltip(webElement, TOOLTIP_IMAGE)).thenReturn(true);
        spy.ifImageWithUrlAndTooltipExists(State.ENABLED, SRC, TOOLTIP_IMAGE);
        verify(baseValidations).assertElementState(THE_FOUND_IMAGE_IS_ENABLED, State.ENABLED, webElement);
    }

    @Test
    void testIfImageWithUrlAndTooltipExists()
    {
        ImageSteps spy = Mockito.spy(imageSteps);
        when(spy.ifImageWithSrcExists(SRC)).thenReturn(webElement);
        when(elementValidations.assertIfElementContainsTooltip(webElement, TOOLTIP_IMAGE)).thenReturn(true);
        WebElement element = spy.ifImageWithUrlAndTooltipExists(SRC, TOOLTIP_IMAGE);
        assertEquals(webElement, element);
    }

    @Test
    void testClickImageWithSrc()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(webElement);
        imageSteps.clickImageWithSrc(SRC);
        verify(clickActions).click(webElement);
    }

    @Test
    void testClickImageWithSrcInNullSearchContext()
    {
        imageSteps.clickImageWithSrc(SRC);
        verify(clickActions, never()).click(webElement);
    }

    @Test
    void testClickImageWithNameByImageLocator()
    {
        when(webUiContext.getSearchContext()).thenReturn(webElement);
        List<WebElement> images = List.of(webElement);
        By imageLocator = By.xpath(XPATH_IMAGE_NAME);
        Mockito.lenient().when(searchActions.findElements(webElement, imageLocator)).thenReturn(images);
        imageSteps.clickImageWithName(IMAGE_NAME);
        verify(clickActions).click(webElement);
    }

    @Test
    void testClickImageWithLabelName()
    {
        when(baseValidations.assertIfElementExists("An image label to click",
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.LABEL_PATTERN, IMAGE_NAME)))).thenReturn(webElement);
        imageSteps.clickImageWithName(IMAGE_NAME);
        verify(clickActions).click(webElement);
    }

    @Test
    void testMouseOverLinkImage()
    {
        when(baseValidations.assertIfElementExists(String.format(AN_IMAGE_WITH_THE_SRC_PATTERN, SRC),
                new SearchAttributes(ActionAttributeType.IMAGE_SRC, SRC))).thenReturn(webElement);
        imageSteps.mouseOverLinkImage(SRC);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testMouseOverLinkImageTooltip()
    {
        when(baseValidations.assertIfElementExists(AN_IMAGE_WITH_THE_TOOLTIP_TOOLTIP_IMAGE,
                new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(IMAGE_TOOLTIP_PATTERN, TOOLTIP_IMAGE))))
                            .thenReturn(webElement);

        imageSteps.mouseOverLinkImageTooltip(TOOLTIP_IMAGE);
        verify(mouseActions).moveToElement(webElement);
    }
}

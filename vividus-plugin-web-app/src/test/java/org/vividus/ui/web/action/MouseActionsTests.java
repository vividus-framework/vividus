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

package org.vividus.ui.web.action;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class MouseActionsTests
{
    private static final String COULD_NOT_MOVE_TO_ERROR_MESSAGE = "Could not move to the element because of an error: ";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock(extraInterfaces = Locatable.class)
    private WebElement locatableWebElement;

    @Mock(extraInterfaces = { HasCapabilities.class, HasInputDevices.class })
    private WebDriver webDriver;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private Mouse mouse;

    @Mock
    private IWebDriverManager webDriverManager;

    @InjectMocks
    private MouseActions mouseActions;

    @Test
    void testContextClick()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        mouseActions.contextClick(locatableWebElement);
        verify(mouse).contextClick(coordinates);
    }

    @Test
    void testContextClickWebElementIsNull()
    {
        mouseActions.contextClick(null);
        verifyZeroInteractions(mouse);
    }

    @Test
    void testMoveToElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        mouseActions.moveToElement(locatableWebElement);
        verify(mouse).mouseMove(coordinates);
    }

    @Test
    void testMoveToNullElement()
    {
        mouseActions.moveToElement(null);
        verifyZeroInteractions(webDriverProvider);
        verifyZeroInteractions(javascriptActions);
        verifyZeroInteractions(softAssert);
    }

    @Test
    void testMoveToElementMoveTargetOutOfBoundsException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        MoveTargetOutOfBoundsException boundsException = new MoveTargetOutOfBoundsException(
                COULD_NOT_MOVE_TO_ERROR_MESSAGE);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        doThrow(boundsException).when(mouse).mouseMove(coordinates);
        mouseActions.moveToElement(locatableWebElement);
        verify(softAssert).recordFailedAssertion(COULD_NOT_MOVE_TO_ERROR_MESSAGE + boundsException);
    }

    @Test
    void testMoveToElementOnMobile()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        when(webDriverManager.isMobile()).thenReturn(true);
        mouseActions.moveToElement(locatableWebElement);
        verify(javascriptActions).scrollIntoView(locatableWebElement, true);
    }

    @Test
    void testMoveToElementOnSafari()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        when(webDriverManager.isMobile()).thenReturn(false);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        mouseActions.moveToElement(locatableWebElement);
        verify(javascriptActions).scrollIntoView(locatableWebElement, true);
    }
}

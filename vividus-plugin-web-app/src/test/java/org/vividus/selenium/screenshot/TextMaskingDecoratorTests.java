/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.vividus.ui.web.action.WebJavascriptActions;

import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class TextMaskingDecoratorTests
{
    private static final String TEXT_TO_MASK = "secretValue123";
    private static final String REPLACEMENT = "**************";
    private static final String MASK_SCRIPT_NAME = "mask-text.js";
    private static final Set<Coords> COORDS = Set.of(new Coords(0, 0, 100, 100));

    @Mock private ShootingStrategy shootingStrategy;
    @Mock private WebJavascriptActions webJavascriptActions;
    @Mock private WebDriver webDriver;
    @Mock private BufferedImage bufferedImage;

    private TextMaskingDecorator decorator;

    @BeforeEach
    void setUp()
    {
        decorator = new TextMaskingDecorator(shootingStrategy, TEXT_TO_MASK, webJavascriptActions);
    }

    @Test
    void shouldTakeScreenshotWithMasking()
    {
        when(shootingStrategy.getScreenshot(webDriver)).thenReturn(bufferedImage);

        BufferedImage result = decorator.getScreenshot(webDriver);

        assertSame(bufferedImage, result);
        InOrder inOrder = Mockito.inOrder(webJavascriptActions, shootingStrategy);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        inOrder.verify(shootingStrategy).getScreenshot(webDriver);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }

    @Test
    void shouldTakeScreenshotWithCoordsWithMasking()
    {
        when(shootingStrategy.getScreenshot(webDriver, COORDS)).thenReturn(bufferedImage);

        BufferedImage result = decorator.getScreenshot(webDriver, COORDS);

        assertSame(bufferedImage, result);
        InOrder inOrder = Mockito.inOrder(webJavascriptActions, shootingStrategy);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        inOrder.verify(shootingStrategy).getScreenshot(webDriver, COORDS);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }

    @Test
    void shouldHandleMaskingFailureAndStillTakeScreenshot()
    {
        doThrow(new WebDriverException("Script execution failed")).when(webJavascriptActions)
                .executeScriptFromResource(any(), eq(MASK_SCRIPT_NAME), eq(TEXT_TO_MASK), eq(REPLACEMENT));
        when(shootingStrategy.getScreenshot(webDriver)).thenReturn(bufferedImage);

        BufferedImage result = decorator.getScreenshot(webDriver);

        assertSame(bufferedImage, result);
        verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        verify(shootingStrategy).getScreenshot(webDriver);
        verify(webJavascriptActions, never()).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }

    @Test
    void shouldHandleMaskingFailureAndStillTakeScreenshotWithCoords()
    {
        doThrow(new WebDriverException("JavaScript error")).when(webJavascriptActions)
                .executeScriptFromResource(any(), eq(MASK_SCRIPT_NAME), eq(TEXT_TO_MASK), eq(REPLACEMENT));
        when(shootingStrategy.getScreenshot(webDriver, COORDS)).thenReturn(bufferedImage);

        BufferedImage result = decorator.getScreenshot(webDriver, COORDS);

        assertSame(bufferedImage, result);
        verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        verify(shootingStrategy).getScreenshot(webDriver, COORDS);
        verify(webJavascriptActions, never()).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }

    @Test
    void shouldEnsureUnmaskingIsCalledEvenIfScreenshotFails()
    {
        WebDriverException screenshotException = new WebDriverException("Screenshot failed");
        when(shootingStrategy.getScreenshot(webDriver)).thenThrow(screenshotException);

        WebDriverException thrown = assertThrows(WebDriverException.class, () -> decorator.getScreenshot(webDriver));
        assertSame(screenshotException, thrown);

        InOrder inOrder = Mockito.inOrder(webJavascriptActions, shootingStrategy);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        inOrder.verify(shootingStrategy).getScreenshot(webDriver);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }

    @Test
    void shouldEnsureUnmaskingIsCalledEvenIfScreenshotWithCoordsFails()
    {
        WebDriverException screenshotException = new WebDriverException("Screenshot with coords failed");
        when(shootingStrategy.getScreenshot(webDriver, COORDS)).thenThrow(screenshotException);

        WebDriverException thrown = assertThrows(WebDriverException.class,
                () -> decorator.getScreenshot(webDriver, COORDS));
        assertSame(screenshotException, thrown);

        InOrder inOrder = Mockito.inOrder(webJavascriptActions, shootingStrategy);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, TEXT_TO_MASK, REPLACEMENT);
        inOrder.verify(shootingStrategy).getScreenshot(webDriver, COORDS);
        inOrder.verify(webJavascriptActions).executeScriptFromResource(TextMaskingDecorator.class,
                MASK_SCRIPT_NAME, REPLACEMENT, TEXT_TO_MASK);
        verifyNoMoreInteractions(webJavascriptActions, shootingStrategy);
    }
}

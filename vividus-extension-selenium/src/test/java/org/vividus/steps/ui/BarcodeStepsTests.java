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

package org.vividus.steps.ui;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.zxing.NotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.BarcodeActions;
import org.vividus.ui.context.IUiContext;
import org.vividus.variable.VariableScope;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class BarcodeStepsTests
{
    private static final BufferedImage QR_CODE_IMAGE = new BufferedImage(10, 10, TYPE_INT_RGB);
    private static final String QR_CODE_VALUE = "QR Code Value";
    private static final String VARIABLE_NAME = "variableName";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private IUiContext uiContext;
    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private AshotScreenshotTaker ashotScreenshotTaker;
    @Mock private BarcodeActions barcodeActions;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private EventBus eventBus;
    @InjectMocks private BarcodeSteps barCodeSteps;

    @Test
    void shouldScanBarcodeSuccessfully() throws IOException, NotFoundException
    {
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(QR_CODE_IMAGE);
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenReturn(QR_CODE_VALUE);

        barCodeSteps.scanBarcode(VARIABLE_SCOPE, VARIABLE_NAME);

        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, QR_CODE_VALUE);
    }

    @Test
    void whenIScanBarcodeAndBarcodeIsAbsent() throws IOException, NotFoundException
    {
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(QR_CODE_IMAGE);
        var exception = NotFoundException.getNotFoundInstance();
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenThrow(exception);

        barCodeSteps.scanBarcode(VARIABLE_SCOPE, VARIABLE_NAME);

        verifyScreenshotNotFoundBehavior("There is no barcode on the screen", exception);
    }

    @Test
    void shouldScanBarcodeSuccessfullyFromSearchContext() throws IOException, NotFoundException
    {
        mockScreenshotFromSearchContext();
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenReturn(QR_CODE_VALUE);
        barCodeSteps.scanBarcodeFromContext(VARIABLE_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, QR_CODE_VALUE);
    }

    @Test
    void shouldNotScanBarcodeFromSearchContextIfItNotPresent() throws IOException
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        barCodeSteps.scanBarcodeFromContext(VARIABLE_SCOPE, VARIABLE_NAME);
        verifyNoInteractions(barcodeActions);
    }

    @Test
    void whenIScanBarcodeFromSearchContextAndBarcodeIsAbsent() throws IOException, NotFoundException
    {
        mockScreenshotFromSearchContext();
        var exception = NotFoundException.getNotFoundInstance();
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenThrow(exception);
        barCodeSteps.scanBarcodeFromContext(VARIABLE_SCOPE, VARIABLE_NAME);
        verifyScreenshotNotFoundBehavior("There is no barcode on the selected context, page or screen", exception);
    }

    private void mockScreenshotFromSearchContext()
    {
        SearchContext searchContextMock = mock(SearchContext.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContextMock));
        Screenshot screenshot = mock(Screenshot.class);
        when(ashotScreenshotTaker.takeAshotScreenshot(searchContextMock, Optional.empty())).thenReturn(screenshot);
        when(screenshot.getImage()).thenReturn(QR_CODE_IMAGE);
    }

    private void verifyScreenshotNotFoundBehavior(String assertionMessage, Exception exception) throws IOException
    {
        var ordered = inOrder(eventBus, softAssert);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        ordered.verify(eventBus).post(eventCaptor.capture());
        Object event = eventCaptor.getValue();
        assertThat(event, instanceOf(AttachmentPublishEvent.class));
        Attachment attachment = ((AttachmentPublishEvent) event).getAttachment();
        assertEquals("Screenshot", attachment.getTitle());
        assertEquals("image/png", attachment.getContentType());
        assertArrayEquals(ImageTool.toByteArray(QR_CODE_IMAGE), attachment.getContent());
        ordered.verify(softAssert).recordFailedAssertion(assertionMessage, exception);
        verifyNoInteractions(variableContext);
    }
}

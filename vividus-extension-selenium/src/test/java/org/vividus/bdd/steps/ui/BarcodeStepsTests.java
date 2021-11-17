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

package org.vividus.bdd.steps.ui;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import com.google.common.eventbus.EventBus;
import com.google.zxing.NotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.BarcodeActions;

import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class BarcodeStepsTests
{
    private static final BufferedImage QR_CODE_IMAGE = new BufferedImage(10, 10, TYPE_INT_RGB);
    private static final String QR_CODE_VALUE = "QR Code Value";
    private static final String VARIABLE_NAME = "variableName";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private BarcodeActions barcodeActions;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private EventBus eventBus;
    @InjectMocks private BarcodeSteps barCodeSteps;

    @Test
    void shouldScanBarcodeSuccessfully() throws IOException, NotFoundException
    {
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(QR_CODE_IMAGE);
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenReturn(QR_CODE_VALUE);

        barCodeSteps.scanBarcode(VARIABLE_SCOPE, VARIABLE_NAME);

        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, QR_CODE_VALUE);
    }

    @Test
    void whenIScanBarcodeAndBarcodeIsAbsent() throws IOException, NotFoundException
    {
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(QR_CODE_IMAGE);
        var exception = NotFoundException.getNotFoundInstance();
        when(barcodeActions.scanBarcode(QR_CODE_IMAGE)).thenThrow(exception);

        barCodeSteps.scanBarcode(VARIABLE_SCOPE, VARIABLE_NAME);

        verify(softAssert).recordFailedAssertion("There is no barcode on the screen", exception);
        var eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventBus).post(eventCaptor.capture());
        Object event = eventCaptor.getValue();
        assertThat(event, instanceOf(AttachmentPublishEvent.class));
        Attachment attachment = ((AttachmentPublishEvent) event).getAttachment();
        assertEquals("Viewport Screenshot", attachment.getTitle());
        assertEquals("image/png", attachment.getContentType());
        assertArrayEquals(ImageTool.toByteArray(QR_CODE_IMAGE), attachment.getContent());
        verifyNoInteractions(bddVariableContext);
    }
}

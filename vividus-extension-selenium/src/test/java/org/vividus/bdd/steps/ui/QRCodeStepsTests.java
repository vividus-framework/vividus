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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.ui.action.QRCodeActions;

@ExtendWith(MockitoExtension.class)
public class QRCodeStepsTests
{
    private static final Path PATH = Paths.get("path");
    private static final String QR_CODE_SCREENSHOT = "QR_Code_Screenshot";
    private static final String QR_CODE_VALUE = "QR Code Value";
    private static final String VARIABLE_NAME = "variableName";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private QRCodeActions qrCodeActions;
    @Mock private IBddVariableContext bddVariableContext;

    @InjectMocks
    private QRCodeSteps qrCodeSteps;

    @Test
    void whenIScanAQRCode() throws IOException
    {
        when(screenshotTaker.takeScreenshotAsFile(QR_CODE_SCREENSHOT)).thenReturn(PATH);
        when(qrCodeActions.scanQRCode(PATH)).thenReturn(QR_CODE_VALUE);

        qrCodeSteps.whenIScanningAQRCode(VARIABLE_SCOPE, VARIABLE_NAME);

        verify(screenshotTaker).takeScreenshotAsFile(QR_CODE_SCREENSHOT);
        verify(qrCodeActions).scanQRCode(PATH);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, QR_CODE_VALUE);
    }
}

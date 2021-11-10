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

package org.vividus.ui.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.qrCode.QRCodeReader;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class QRCodeActionsTests
{
    private static final Path PATH = Paths.get("path");
    private static final String QR_CODE_VALUE = "QR Code Value";

    @Mock private QRCodeReader qrCodeReader;
    @InjectMocks private QRCodeActions qrCodeActions;

    @Test
    void shouldReadQRCode() throws IOException
    {
        when(qrCodeReader.readQRCode(PATH)).thenReturn(QR_CODE_VALUE);

        String qrCodeValue = qrCodeActions.scanQRCode(PATH);

        verify(qrCodeReader).readQRCode(PATH);
        assertEquals(qrCodeValue, QR_CODE_VALUE);
    }

    @Test
    void shouldThrowExceptionInCaseOfQRCodeAbsents() throws IOException
    {
        when(qrCodeReader.readQRCode(PATH)).thenReturn("");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> qrCodeActions.scanQRCode(PATH));
        assertEquals(
                "There is no QR code in the image",
                exception.getMessage());
        verify(qrCodeReader).readQRCode(PATH);
    }
}

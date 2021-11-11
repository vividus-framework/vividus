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

import java.awt.image.BufferedImage;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QrCodeActions
{
    private static final String NO_QR_CODE_IN_THE_IMAGE_MESSAGE = "There is no QR code in the image";

    private static final Logger LOGGER = LoggerFactory.getLogger(QrCodeActions.class);

    public String scanQrCode(BufferedImage qrCode)
    {
        try
        {
            LuminanceSource source = new BufferedImageLuminanceSource(qrCode);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        }
        catch (NotFoundException e)
        {
            LOGGER.error(NO_QR_CODE_IN_THE_IMAGE_MESSAGE);
            throw new IllegalArgumentException(NO_QR_CODE_IN_THE_IMAGE_MESSAGE, e);
        }
    }
}

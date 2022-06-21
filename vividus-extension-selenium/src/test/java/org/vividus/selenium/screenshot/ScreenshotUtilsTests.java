/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

@ExtendWith(MockitoExtension.class)
class ScreenshotUtilsTests
{
    private static final byte[] BYTES = { 0, 1, 0 };

    @Mock(extraInterfaces = TakesScreenshot.class)
    private WebDriver webDriver;

    @BeforeEach
    void init()
    {
        TakesScreenshot takesScreenshot = (TakesScreenshot) webDriver;
        when(takesScreenshot.getScreenshotAs(OutputType.BYTES)).thenReturn(BYTES);
    }

    @Test
    void shouldTakeViewportScreenshot() throws IOException
    {
        try (MockedStatic<ImageIO> imageIo = mockStatic(ImageIO.class))
        {
            BufferedImage mockedImage = mock(BufferedImage.class);
            imageIo.when(() -> ImageIO.read((ByteArrayInputStream) argThat(arg ->
            {
                ByteArrayInputStream bais = (ByteArrayInputStream) arg;
                return Arrays.equals(BYTES, bais.readAllBytes());
            }))).thenReturn(mockedImage);

            BufferedImage image = ScreenshotUtils.takeViewportScreenshot(webDriver);
            assertEquals(mockedImage, image);
        }
    }

    @Test
    void shouldTakeViewportScreenshotAsByteArray()
    {
        assertArrayEquals(BYTES, ScreenshotUtils.takeViewportScreenshotAsByteArray(webDriver));
    }
}

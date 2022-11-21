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

package org.vividus.report.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.vividus.util.ResourceUtils;

import pazone.ashot.util.ImageTool;

class ImageCompressorTests
{
    private static final float IMAGE_COMPRESSION_QUALITY = 0.2f;

    @Test
    void shouldNotDoAnythingWhenCompressionDisabled()
    {
        var imageCompressor = new ImageCompressor();
        imageCompressor.setImageCompressionQuality(1f);
        byte[] image = new byte[] { };
        assertSame(image, imageCompressor.compress(image));
    }

    @Test
    void shouldCompressTheImage()
    {
        byte[] image = ResourceUtils.loadResourceAsByteArray("org/vividus/report/ui/test.png");
        var compressor = new ImageCompressor();
        compressor.setImageCompressionQuality(IMAGE_COMPRESSION_QUALITY);
        var compressed = compressor.compress(image);
        assertThat(compressed.length, Matchers.lessThan(image.length));
    }

    @Test
    void  shouldWrapIOException()
    {
        var image = new byte[] {0, 0, 0};
        var compressor = new ImageCompressor();
        compressor.setImageCompressionQuality(IMAGE_COMPRESSION_QUALITY);
        try (MockedStatic<ImageTool> mockedImageTool = mockStatic(ImageTool.class))
        {
            var ioException = new IOException();
            mockedImageTool.when(() -> ImageTool.toBufferedImage(image)).thenThrow(ioException);
            var uioe = assertThrows(UncheckedIOException.class, () -> compressor.compress(image));
            assertSame(ioException, uioe.getCause());
        }
    }
}

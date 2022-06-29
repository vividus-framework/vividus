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

package org.vividus.ui.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImageUtilsTests
{
    @Test
    void shouldWriteImageAsPng(@TempDir File tempDir) throws IOException
    {
        String fileName = "image.png";
        BufferedImage image = new BufferedImage(10, 10, 5);
        ImageUtils.writeAsPng(image, new File(tempDir, fileName));
        BufferedImage imageFromDisk = ImageIO.read(new File(tempDir, fileName));
        assertEquals(image.getHeight(), imageFromDisk.getHeight());
        assertEquals(image.getWidth(), imageFromDisk.getWidth());
    }

    @Test
    void shouldEncodeAsPng() throws IOException
    {
        BufferedImage image = new BufferedImage(1, 1, 1);
        byte[] bytes = ImageUtils.encodeAsPng(image);
        byte[] expected = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 2,
                0, 0, 0, -112, 119, 83, -34, 0, 0, 0, 12, 73, 68, 65, 84, 120, 94, 99, 96, 96, 96, 0, 0, 0, 4, 0, 1, 15,
                -46, -83, -28, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
        assertArrayEquals(expected, bytes);
    }
}

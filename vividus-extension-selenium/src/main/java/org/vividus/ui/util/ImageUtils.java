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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class ImageUtils
{
    private static final String PNG_FORMAT_NAME = "png";

    private ImageUtils()
    {
    }

    public static void writeAsPng(BufferedImage image, File location) throws IOException
    {
        ImageIO.write(image, PNG_FORMAT_NAME, location);
    }

    public static byte[] encodeAsPng(BufferedImage image) throws IOException
    {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            ImageIO.write(image, PNG_FORMAT_NAME, output);
            return output.toByteArray();
        }
    }
}

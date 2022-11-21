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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;

import pazone.ashot.util.ImageTool;

public class ImageCompressor
{
    private float imageCompressionQuality;

    public byte[] compress(byte[] image)
    {
        if (imageCompressionQuality < 1)
        {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
            {
                Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpg");
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(imageCompressionQuality);
                writer.setOutput(ImageIO.createImageOutputStream(baos));
                writer.write(null, new IIOImage(toRGB(image), null, null), param);
                return baos.toByteArray();
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }
        return image;
    }

    private BufferedImage toRGB(byte[] imageBytes) throws IOException
    {
        var image = ImageTool.toBufferedImage(imageBytes);
        BufferedImage jpgImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        jpgImage.createGraphics().drawImage(image, 0, 0, Color.BLACK, null);
        return jpgImage;
    }

    public void setImageCompressionQuality(float imageCompressionQuality)
    {
        this.imageCompressionQuality = imageCompressionQuality;
    }
}

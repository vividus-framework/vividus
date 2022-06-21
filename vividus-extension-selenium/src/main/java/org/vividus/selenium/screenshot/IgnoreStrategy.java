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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import ru.yandex.qatools.ashot.coordinates.Coords;

public enum IgnoreStrategy
{
    ELEMENT
    {
        private final Color transparent = new Color(0, true);

        @Override
        public BufferedImage crop(BufferedImage source, Set<Coords> ignoredCoords)
        {
            Graphics2D g2 = source.createGraphics();
            g2.setBackground(transparent);
            ignoredCoords.forEach(c -> g2.clearRect(c.x, c.y, c.width, c.height));
            g2.dispose();
            return source;
        }
    },
    AREA
    {
        @Override
        public BufferedImage crop(BufferedImage source, Set<Coords> ignoredCoords)
        {
            int totalHeightToCrop = ignoredCoords.stream().mapToInt(c -> c.height).sum();
            int imageWidth = source.getWidth();
            BufferedImage result = new BufferedImage(imageWidth, source.getHeight() - totalHeightToCrop,
                    source.getType());
            Set<Coords> sortedIgnoredCoords = new TreeSet<>(Comparator.comparingInt((Coords c) -> c.y));
            sortedIgnoredCoords.addAll(ignoredCoords);
            int nextInsertY = 0;
            int sourceY1 = 0;
            Graphics2D g2 = result.createGraphics();
            for (Coords coords : sortedIgnoredCoords)
            {
                nextInsertY = drawImagePart(g2, source, nextInsertY, sourceY1, coords.y);
                sourceY1 = coords.y + coords.height;
            }
            drawImagePart(g2, source, nextInsertY, sourceY1, source.getHeight());
            g2.dispose();
            return result;
        }

        private int drawImagePart(Graphics2D g2, BufferedImage source, int nextInsertY, int sourceY1, int sourceY2)
        {
            int imageWidth = source.getWidth();
            int destinationY2 = nextInsertY + (sourceY2 - sourceY1);
            g2.drawImage(source, 0, nextInsertY, imageWidth, destinationY2, 0, sourceY1, imageWidth, sourceY2, null);
            return destinationY2;
        }
    };

    public abstract BufferedImage crop(BufferedImage source, Set<Coords> ignoredCoords);
}

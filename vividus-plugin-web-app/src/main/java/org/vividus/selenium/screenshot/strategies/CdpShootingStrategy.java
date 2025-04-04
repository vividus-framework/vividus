/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.selenium.screenshot.strategies;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;
import org.vividus.selenium.WebDriverUtils;

import pazone.ashot.ShootingStrategy;
import pazone.ashot.coordinates.Coords;
import pazone.ashot.util.ImageTool;

public class CdpShootingStrategy implements ShootingStrategy
{
    @Override
    public BufferedImage getScreenshot(WebDriver driver)
    {
        return getScreenshot(driver, Set.of());
    }

    @Override
    public BufferedImage getScreenshot(WebDriver driver, Set<Coords> coords)
    {
        Map<String, Object> args = new HashMap<>();
        args.put("captureBeyondViewport", true);
        System.out.println(coords);

        if (!coords.isEmpty())
        {
            Coords elementCoords = coords.iterator().next();
            args.put("clip", Map.of(
                "x", elementCoords.x,
                "y", elementCoords.y,
                "width", elementCoords.width,
                "height", elementCoords.height,
                "scale", 1)
            );
        }

        HasCdp cdp = WebDriverUtils.unwrap(driver, HasCdp.class);
        Map<String, Object> results = cdp.executeCdpCommand("Page.captureScreenshot", args);
        String base64 = (String) results.get("data");
        byte[] bytes = OutputType.BYTES.convertFromBase64Png(base64);

        try
        {
            return ImageTool.toBufferedImage(bytes);
        }
        catch (IOException thrown)
        {
            throw new UncheckedIOException(thrown);
        }
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet)
    {
        return coordsSet;
    }
}

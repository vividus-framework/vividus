/*
 * Copyright 2019 the original author or authors.
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.util.ImageUtils;

public class FilesystemScreenshotDebugger implements ScreenshotDebugger
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemScreenshotDebugger.class);
    private static final String UNDERSCORE = "_";

    private Optional<File> debugScreenshotsLocation;

    @Override
    public void debug(Class<?> clazz, String suffix, BufferedImage debugImage)
    {
        debugScreenshotsLocation.ifPresent(l ->
        {
            File location = new File(l, System.currentTimeMillis() + UNDERSCORE + clazz.getSimpleName()
                + UNDERSCORE + suffix);
            String filePath = location.toString() + ".png";
            try
            {
                ImageUtils.writeAsPng(debugImage, location);
                LOGGER.debug("Debug screenshot saved to {}", filePath);
            }
            catch (IOException e)
            {
                LOGGER.debug("Unable to save debug screenshot to {}", filePath, e);
            }
        });
    }

    public void setDebugScreenshotsLocation(Optional<File> debugScreenshotsLocation)
    {
        this.debugScreenshotsLocation = debugScreenshotsLocation;
    }
}

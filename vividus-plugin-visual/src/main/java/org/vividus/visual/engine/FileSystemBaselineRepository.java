/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.visual.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.google.common.base.Suppliers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.resource.ResourceLoadException;
import org.vividus.ui.web.util.ImageUtils;
import org.vividus.util.ResourceUtils;

import ru.yandex.qatools.ashot.Screenshot;

public class FileSystemBaselineRepository implements IBaselineRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemBaselineRepository.class);

    private File baselinesFolder;

    private final Supplier<File> baselineFolderResolver = Suppliers.memoize(() -> {
        if (!baselinesFolder.isAbsolute())
        {
            String replacement = "/";
            return ResourceUtils.loadFile(FileSystemBaselineRepository.class,
                            StringUtils.removeStart(baselinesFolder.toString(), ".").replace("\\", replacement));
        }
        return baselinesFolder;
    });

    @Override
    public Optional<Screenshot> getBaseline(String baselineName) throws IOException
    {
        File baselineFile = new File(baselineFolderResolver.get(), appendExtension(baselineName));
        if (!baselineFile.exists())
        {
            LOGGER.warn("Unable to find a baseline at the path: {}", baselineFile);
            return Optional.empty();
        }
        BufferedImage baselineImage = ImageIO.read(baselineFile);
        if (baselineImage == null)
        {
            throw new ResourceLoadException(
                    "The baseline at the path '" + baselineFile + "' is broken or has unsupported format");
        }
        return Optional.of(new Screenshot(baselineImage));
    }

    private String appendExtension(String baselineName)
    {
        return baselineName + ".png";
    }

    @Override
    public void saveBaseline(Screenshot toSave, String baselineName) throws IOException
    {
        File baselineToSave = new File(baselineFolderResolver.get(), baselineName);
        ImageUtils.writeAsPng(toSave.getImage(), baselineToSave);
        LOGGER.info("Baseline saved to: {}", appendExtension(baselineToSave.getAbsolutePath()));
    }

    public void setBaselinesFolder(File baselinesFolder)
    {
        this.baselinesFolder = baselinesFolder;
    }
}

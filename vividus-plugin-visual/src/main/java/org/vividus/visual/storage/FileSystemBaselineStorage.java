/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.visual.storage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.google.common.base.Suppliers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.resource.ResourceLoadException;
import org.vividus.ui.util.ImageUtils;
import org.vividus.util.ResourceUtils;

import pazone.ashot.Screenshot;

public class FileSystemBaselineStorage implements BaselineStorage
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemBaselineStorage.class);

    private final Supplier<File> baselineFolderResolver;
    private final Supplier<File> deltaFolderResolver;

    public FileSystemBaselineStorage(File baselinesFolder, @Nullable File deltaFolder)
    {
        this.baselineFolderResolver = Suppliers.memoize(() -> resolveFolder(baselinesFolder));
        this.deltaFolderResolver = deltaFolder == null ? baselineFolderResolver
                : Suppliers.memoize(() -> resolveFolder(deltaFolder));
    }

    private static File resolveFolder(File folder)
    {
        if (!folder.isAbsolute())
        {
            String replacement = "/";
            return ResourceUtils.loadFile(FileSystemBaselineStorage.class,
                            Strings.CS.removeStart(folder.toString(), ".").replace("\\", replacement));
        }
        return folder;
    }

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
    public void saveBaseline(Screenshot screenshot, String baselineName) throws IOException
    {
        saveBaseline(baselineFolderResolver.get(), screenshot, baselineName);
    }

    @Override
    public void saveDelta(Screenshot screenshot, String baselineName) throws IOException
    {
        saveBaseline(deltaFolderResolver.get(), screenshot, baselineName);
    }

    private void saveBaseline(File file, Screenshot screenshot, String baselineName) throws IOException
    {
        File baselineToSave = new File(file, appendExtension(baselineName));
        FileUtils.forceMkdirParent(baselineToSave);
        ImageUtils.writeAsPng(screenshot.getImage(), baselineToSave);
        LOGGER.atInfo().addArgument(baselineToSave::getAbsolutePath).log("Baseline saved to: {}");
    }
}

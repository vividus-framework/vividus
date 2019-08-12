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

package org.vividus.visual.engine;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import org.apache.tools.ant.util.StringUtils;
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

    public void init()
    {
        if (!baselinesFolder.isAbsolute())
        {
            String replacement = "/";
            baselinesFolder = ResourceUtils.loadFile(FileSystemBaselineRepository.class,
                            StringUtils.removePrefix(baselinesFolder.toString(), ".").replaceAll("\\\\", replacement));
        }
    }

    @Override
    public Optional<Screenshot> getBaseline(String baselineName) throws IOException
    {
        try
        {
            File baselineFile = new File(baselinesFolder, appendExtension(baselineName));
            Optional<Screenshot> loadedScreenshot = Optional
                    .ofNullable(ImageIO.read(baselineFile))
                    .map(Screenshot::new);
            if (loadedScreenshot.isEmpty())
            {
                throw new ResourceLoadException("Unable to load baseline with path: " + baselineFile);
            }
            return loadedScreenshot;
        }
        catch (IIOException e)
        {
            return Optional.empty();
        }
    }

    private String appendExtension(String baselineName)
    {
        return baselineName + ".png";
    }

    @Override
    public void saveBaseline(Screenshot toSave, String baselineName) throws IOException
    {
        File baselineToSave = new File(baselinesFolder, baselineName);
        ImageUtils.writeAsPng(toSave.getImage(), baselineToSave);
        LOGGER.info("Baseline saved to: {}", appendExtension(baselineToSave.getAbsolutePath()));
    }

    public void setBaselinesFolder(File baselinesFolder)
    {
        this.baselinesFolder = baselinesFolder;
    }
}

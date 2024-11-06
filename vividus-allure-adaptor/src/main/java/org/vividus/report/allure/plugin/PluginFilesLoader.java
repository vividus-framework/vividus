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

package org.vividus.report.allure.plugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.vividus.util.ResourceUtils;

public class PluginFilesLoader
{
    private FileSystem fileSystem;

    public Path loadPluginFile(String pluginId, String fileName)
    {
        try
        {
            return loadResource("/allure-plugins/" + pluginId + "/" + fileName);
        }
        catch (IOException | URISyntaxException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public Path loadResource(String resourceLocation) throws URISyntaxException, IOException
    {
        URI resource = ResourceUtils.findResource(getClass(), resourceLocation).toURI();
        try
        {
            return Paths.get(resource);
        }
        catch (FileSystemNotFoundException e)
        {
            if (fileSystem == null)
            {
                fileSystem = FileSystems.newFileSystem(resource, Map.of());
            }
            return fileSystem.provider().getPath(resource);
        }
    }

    public void destroy() throws IOException
    {
        if (fileSystem != null)
        {
            fileSystem.close();
        }
    }
}

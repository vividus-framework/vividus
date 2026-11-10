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

package org.vividus.report.allure.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.plugin.DefaultPlugin;

public final class CustomTabPlugin extends DefaultPlugin
{
    private static final String INDEX_JS = "index.js";
    private static final String PLUGIN_ID = "custom-tab";
    private static final String FILES_FOLDER_PATH = "/report-plugin/" + PLUGIN_ID;

    private final Map<String, Path> pluginFiles = new HashMap<>();

    public CustomTabPlugin(Boolean pluginEnabled, PluginFilesLoader pluginFilesLoader) throws IOException
    {
        super(new PluginConfiguration().setId(PLUGIN_ID), List.of(), null);

        if (pluginEnabled)
        {
            Path indexJs = pluginFilesLoader.loadPluginFile(PLUGIN_ID, INDEX_JS);
            getPluginFiles().put(INDEX_JS, indexJs);
            this.getConfig().setJsFiles(List.of(INDEX_JS));

            try (Stream<Path> pathStream = Files.walk(pluginFilesLoader.loadResource(FILES_FOLDER_PATH)))
            {
                pathStream.filter(p -> p.toFile().isFile())
                        .forEach(p -> getPluginFiles().put(
                                StringUtils.substringAfter(p.toString(), PLUGIN_ID + File.separator), p));
            }
        }
    }

    public Map<String, Path> getPluginFiles()
    {
        return this.pluginFiles;
    }
}

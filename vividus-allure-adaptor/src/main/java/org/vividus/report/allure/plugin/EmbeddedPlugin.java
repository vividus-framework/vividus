/*
 * Copyright 2019-2023 the original author or authors.
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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.plugin.DefaultPlugin;

public class EmbeddedPlugin extends DefaultPlugin
{
    private final List<String> pluginFiles;
    private final PluginFilesLoader pluginFilesLoader;

    public EmbeddedPlugin(String id, List<String> pluginFiles, PluginFilesLoader pluginFilesLoader)
    {
        this(id, pluginFiles, List.of(), pluginFilesLoader);
    }

    public EmbeddedPlugin(String id, List<String> pluginFiles, Extension extension, PluginFilesLoader pluginFilesLoader)
    {
        this(id, pluginFiles, List.of(extension), pluginFilesLoader);
    }

    private EmbeddedPlugin(String id, List<String> pluginFiles, List<Extension> extensions,
            PluginFilesLoader pluginFilesLoader)
    {
        super(new PluginConfiguration()
                        .setId(id)
                        .setJsFiles(filterFilesByExtension(pluginFiles, ".js"))
                        .setCssFiles(filterFilesByExtension(pluginFiles, ".css")),
                extensions, null);
        this.pluginFiles = pluginFiles;
        this.pluginFilesLoader = pluginFilesLoader;
    }

    private static List<String> filterFilesByExtension(List<String> pluginFiles, String extension)
    {
        return pluginFiles.stream().filter(file -> file.endsWith(extension)).toList();
    }

    @Override
    public Map<String, Path> getPluginFiles()
    {
        return pluginFiles.stream().collect(Collectors.toMap(Function.identity(),
                pluginFile -> pluginFilesLoader.loadPluginFile(getConfig().getId(), pluginFile)
        ));
    }
}

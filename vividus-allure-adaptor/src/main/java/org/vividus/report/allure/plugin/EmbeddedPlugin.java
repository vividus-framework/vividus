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

package org.vividus.report.allure.plugin;

import java.nio.file.Path;
import java.util.List;

import io.qameta.allure.Extension;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.plugin.DefaultPlugin;

public class EmbeddedPlugin extends DefaultPlugin
{
    public EmbeddedPlugin(String id, List<String> jsFiles, Extension extension)
    {
        super(new PluginConfiguration().setId(id).setJsFiles(jsFiles), List.of(extension), null);
    }

    public EmbeddedPlugin(String id)
    {
        super(new PluginConfiguration().setId(id).setCssFiles(List.of("styles.css")), List.of(), null);
    }

    public EmbeddedPlugin(String id, Extension extension)
    {
        super(new PluginConfiguration().setId(id), List.of(extension), null);
    }

    @Override
    public void unpackReportStatic(Path outputDirectory)
    {
        // do nothing
    }
}

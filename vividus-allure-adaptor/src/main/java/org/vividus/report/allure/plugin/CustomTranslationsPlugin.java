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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.qameta.allure.PluginConfiguration;
import io.qameta.allure.plugin.DefaultPlugin;

public class CustomTranslationsPlugin extends DefaultPlugin
{
    private static final String INDEX_JS = "index.js";
    private final Map<String, Path> pluginFiles;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public CustomTranslationsPlugin(PropertyMappedCollection<Map<String, ?>> customTranslations, JsonUtils jsonUtils)
            throws IOException
    {
        super(new PluginConfiguration()
                        .setId("custom-translations")
                        .setJsFiles(List.of(INDEX_JS)),
                List.of(), null);

        List<String> jsFileLines = new ArrayList<>();
        jsFileLines.add("'use strict';");
        customTranslations.getData().forEach((lang, value) -> jsFileLines.add(
                    "allure.api.addTranslation('%s', %s);".formatted(lang, jsonUtils.toJson(value))
                )
        );

        Path indexJs = ResourceUtils.createTempFile("index", ".js");
        Files.write(indexJs, jsFileLines);

        this.pluginFiles = Map.of(INDEX_JS, indexJs);
    }

    @Override
    public Map<String, Path> getPluginFiles()
    {
        return pluginFiles;
    }
}

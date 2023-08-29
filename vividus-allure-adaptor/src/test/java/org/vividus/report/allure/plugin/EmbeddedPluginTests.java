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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.qameta.allure.Extension;

@ExtendWith(MockitoExtension.class)
class EmbeddedPluginTests
{
    private static final String INDEX_JS = "index.js";

    private final PluginFilesLoader pluginFilesLoader = new PluginFilesLoader();

    @AfterEach
    void afterEach() throws IOException
    {
        pluginFilesLoader.destroy();
    }

    @Test
    void shouldCreatePluginWithAllTypesOfResources()
    {
        Extension extension = mock();
        var id = "all-types-of-resources";
        var jsFile = INDEX_JS;
        var cssFile = "styles.css";
        var logoFile = "logo.csv";
        var plugin = new EmbeddedPlugin(id, List.of(jsFile, cssFile, logoFile), extension, pluginFilesLoader);

        assertEquals(id, plugin.getConfig().getId());
        assertEquals(List.of(jsFile), plugin.getConfig().getJsFiles());
        assertEquals(List.of(cssFile), plugin.getConfig().getCssFiles());
        assertEquals(List.of(extension), plugin.getExtensions());

        var pluginFiles = plugin.getPluginFiles();
        assertEquals(3, pluginFiles.size());
        assertPluginFile(pluginFiles, id, jsFile);
        assertPluginFile(pluginFiles, id, cssFile);
        assertPluginFile(pluginFiles, id, logoFile);
    }

    @Test
    void shouldCreateMinimalPlugin()
    {
        var id = "behaviors";
        var jsFile = INDEX_JS;
        var plugin = new EmbeddedPlugin(id, List.of(jsFile), pluginFilesLoader);

        assertEquals(id, plugin.getConfig().getId());
        assertEquals(id, plugin.getConfig().getId());
        assertEquals(List.of(jsFile), plugin.getConfig().getJsFiles());
        assertEquals(List.of(), plugin.getConfig().getCssFiles());
        assertEquals(List.of(), plugin.getExtensions());

        var pluginFiles = plugin.getPluginFiles();
        assertEquals(1, pluginFiles.size());
        assertPluginFile(pluginFiles, id, jsFile);
    }

    private static void assertPluginFile(Map<String, Path> pluginFiles, String pluginId, String file)
    {
        assertThat(pluginFiles.get(file).toString(), endsWith(
                File.separatorChar + "allure-plugins" + File.separatorChar + pluginId + File.separatorChar + file));
    }
}

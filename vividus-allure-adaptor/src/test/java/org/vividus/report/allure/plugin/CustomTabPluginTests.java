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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;

class CustomTabPluginTests
{
    private final PluginFilesLoader pluginFilesLoader = new PluginFilesLoader();

    private CustomTabPlugin customTabPlugin;

    @Test
    void testPluginDisabled() throws IOException
    {
        customTabPlugin = new CustomTabPlugin(false, pluginFilesLoader);
        assertTrue(customTabPlugin.getPluginFiles().isEmpty());
    }

    @Test
    void testPluginEnabled() throws IOException
    {
        customTabPlugin = new CustomTabPlugin(true, pluginFilesLoader);
        Map<String, Path> pluginFiles = customTabPlugin.getPluginFiles();
        assertEquals(3, pluginFiles.size());
        assertTrue(pluginFiles.containsKey("index.js"));
        assertTrue(pluginFiles.containsKey("file1.txt"));
        assertTrue(pluginFiles.containsKey("data" + File.separator + "file2.txt"));
    }
}

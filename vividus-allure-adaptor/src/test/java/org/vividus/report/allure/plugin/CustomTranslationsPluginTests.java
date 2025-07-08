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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

class CustomTranslationsPluginTests
{
    @Test
    void shouldGenerateIndexJs() throws IOException
    {
        assertEquals("""
                'use strict';
                allure.api.addTranslation('en', {"tab":{"suites":{"name":"Batches Tab"}}});
                """, Files.readString(generatePluginJsFile()).replace("\r", ""));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldGenerateIndexJsWith644Permissions() throws IOException
    {
        var permissions = PosixFilePermissions.toString(Files.getPosixFilePermissions(generatePluginJsFile()));
        assertEquals("rw-r--r--", permissions);
    }

    @Test
    void shouldNotGenerateIndexJsIfNoTranslationsAreProvided() throws IOException
    {
        var plugin = new CustomTranslationsPlugin(new PropertyMappedCollection<>(Map.of()), new JsonUtils());
        assertEquals(0, plugin.getPluginFiles().size());
    }

    private Path generatePluginJsFile() throws IOException
    {
        var plugin = new CustomTranslationsPlugin(new PropertyMappedCollection<>(Map.of("en", Map.of(
                "tab", Map.of(
                        "suites", Map.of(
                                "name", "Batches Tab"
                        )
                )
        ))), new JsonUtils());
        var pluginFiles = plugin.getPluginFiles();
        assertEquals(1, pluginFiles.size());
        return pluginFiles.get("index.js");
    }
}

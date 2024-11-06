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

import org.junit.jupiter.api.Test;

class BrandLogoPluginTests
{
    private static final String PATH = "/allure-plugins/vividus-logo/vividus-logo.svg";
    private static final String EXPECTED_CSS = """
            .side-nav__brand {
              background: url('vividus-logo.svg') no-repeat left center !important;
              background-size: 44px 44px !important;
            }
            """;
    private final PluginFilesLoader pluginFilesLoader = new PluginFilesLoader();

    @Test
    void shouldCreatePluginFiles() throws IOException
    {
        var plugin = new BrandLogoPlugin(PATH, pluginFilesLoader);
        var pluginFiles = plugin.getPluginFiles();
        assertEquals(2, pluginFiles.size());
        assertEquals(EXPECTED_CSS, Files.readString(pluginFiles.get("styles.css")).replace("\r", ""));
    }
}

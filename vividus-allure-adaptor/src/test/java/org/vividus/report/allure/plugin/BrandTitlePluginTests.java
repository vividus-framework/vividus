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

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

class BrandTitlePluginTests
{
    private static final String EXPECTED_CSS = """
            .side-nav__brand-text {
              display: none;
            }
            .side-nav__brand:after {
              content: 'VIVIDUS';
              font-size: var(--font-size-xl);
              font-weight: var(--font-weight-extra-bold);
              color: var(--color-text-primary);
            }
            .side-nav__footer:after {
              content: 'Powered by Allure';
              color: var(--color-text-tertiary);
              display: block;
              width: 100%;
              text-align: center;
              padding-top: 20px;
              font-size: var(--font-size-xs);
            }
            .side-nav_collapsed .side-nav__brand:after {
              display: none;
            }
            .side-nav_collapsed .side-nav__footer:after {
              display: none;
            }
            """;

    @Test
    void shouldGenerateStylesCss() throws IOException
    {
        var plugin = new BrandTitlePlugin("VIVIDUS");
        var pluginFiles = plugin.getPluginFiles();
        assertEquals(1, pluginFiles.size());
        assertEquals(EXPECTED_CSS, Files.readString(pluginFiles.get("styles.css")).replace("\r", ""));
    }
}

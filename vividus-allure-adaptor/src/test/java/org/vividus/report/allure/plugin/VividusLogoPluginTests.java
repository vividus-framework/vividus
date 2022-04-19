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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.qameta.allure.PluginConfiguration;

class VividusLogoPluginTests
{
    @Test
    void testInitialization()
    {
        VividusTitlePlugin plugin = new VividusTitlePlugin();
        PluginConfiguration config = plugin.getConfig();

        assertEquals("vividus-title", config.getId());
        assertEquals(1, config.getCssFiles().size());
        assertEquals("styles.css", config.getCssFiles().get(0));
    }
}

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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.qameta.allure.Extension;

@ExtendWith(MockitoExtension.class)
class EmbeddedPluginTests
{
    private static final String ID = "id";
    private static final List<String> RESOURCE_LIST = List.of();

    @Mock
    private Extension extension;

    @Test
    void testInitializationPluginWithJsAndExtension()
    {
        EmbeddedPlugin plugin = new EmbeddedPlugin(ID, RESOURCE_LIST, extension);

        assertEquals(ID, plugin.getConfig().getId());
        assertEquals(RESOURCE_LIST, plugin.getConfig().getJsFiles());
        assertEquals(1, plugin.getExtensions().size());
        assertEquals(extension, plugin.getExtensions().get(0));
    }

    @Test
    void testInitializationPluginWithCss()
    {
        EmbeddedPlugin plugin = new EmbeddedPlugin(ID);

        assertEquals(ID, plugin.getConfig().getId());
        assertEquals(List.of("styles.css"), plugin.getConfig().getCssFiles());
    }

    @Test
    void testInitializationPluginWithExtension()
    {
        EmbeddedPlugin plugin = new EmbeddedPlugin(ID, extension);

        assertEquals(ID, plugin.getConfig().getId());
        assertEquals(1, plugin.getExtensions().size());
        assertEquals(extension, plugin.getExtensions().get(0));
    }
}

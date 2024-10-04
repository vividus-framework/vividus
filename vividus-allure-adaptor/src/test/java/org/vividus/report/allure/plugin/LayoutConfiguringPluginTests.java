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
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.vividus.report.allure.plugin.LayoutConfiguringPlugin.Component;
import org.vividus.util.property.PropertyMappedCollection;

@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:MultipleStringLiteralsExtended" })
class LayoutConfiguringPluginTests
{
    @Test
    void shouldGenerateIndexJs() throws IOException
    {
        assertEquals("""
                'use strict';
                allure.api.tabs = allure.api.tabs.filter(\
                t => !['categories','suites','graph','timeline','behaviors'].includes(t.tabName));
                delete allure.api.widgets.widgets['summary'];
                delete allure.api.widgets.widgets['suites'];
                delete allure.api.widgets.widgets['environment'];
                delete allure.api.widgets.widgets['history-trend'];
                delete allure.api.widgets.widgets['categories'];
                delete allure.api.widgets.widgets['behaviors'];
                delete allure.api.widgets.widgets['executors'];
                delete allure.api.widgets.graph['status-chart'];
                delete allure.api.widgets.graph['severity'];
                delete allure.api.widgets.graph['duration'];
                delete allure.api.widgets.graph['duration-trend'];
                delete allure.api.widgets.graph['retry-trend'];
                delete allure.api.widgets.graph['categories-trend'];
                delete allure.api.widgets.graph['history-trend'];
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
    void shouldNotGenerateIndexJsIfNoDataIsProvided() throws IOException
    {
        var plugin = new LayoutConfiguringPlugin(new PropertyMappedCollection<>(Map.of()),
                new PropertyMappedCollection<>(Map.of()), new PropertyMappedCollection<>(Map.of()));
        assertEquals(0, plugin.getPluginFiles().size());
    }

    private Path generatePluginJsFile() throws IOException
    {
        var plugin = new LayoutConfiguringPlugin(
                new PropertyMappedCollection<>(createTabsConfiguration()),
                new PropertyMappedCollection<>(createWidgetsConfiguration()),
                new PropertyMappedCollection<>(createChartsConfiguration())
        );
        var pluginFiles = plugin.getPluginFiles();
        assertEquals(1, pluginFiles.size());
        return pluginFiles.get("index.js");
    }

    private static Map<String, Component> createTabsConfiguration()
    {
        Map<String, Component> tabsConfiguration = new LinkedHashMap<>();
        tabsConfiguration.put("categories", createDisabledComponent());
        tabsConfiguration.put("suites", createDisabledComponent());
        tabsConfiguration.put("graph", createDisabledComponent());
        tabsConfiguration.put("timeline", createDisabledComponent());
        tabsConfiguration.put("behaviors", createDisabledComponent());
        return tabsConfiguration;
    }

    private static Map<String, Component> createWidgetsConfiguration()
    {
        Map<String, Component> widgetsConfiguration = new LinkedHashMap<>();
        widgetsConfiguration.put("summary", createDisabledComponent());
        widgetsConfiguration.put("suites", createDisabledComponent());
        widgetsConfiguration.put("environment", createDisabledComponent());
        widgetsConfiguration.put("history-trend", createDisabledComponent());
        widgetsConfiguration.put("categories", createDisabledComponent());
        widgetsConfiguration.put("behaviors", createDisabledComponent());
        widgetsConfiguration.put("executors", createDisabledComponent());
        return widgetsConfiguration;
    }

    private static Map<String, Component> createChartsConfiguration()
    {
        Map<String, Component> chartsConfiguration = new LinkedHashMap<>();
        chartsConfiguration.put("status-chart", createDisabledComponent());
        chartsConfiguration.put("severity", createDisabledComponent());
        chartsConfiguration.put("duration", createDisabledComponent());
        chartsConfiguration.put("duration-trend", createDisabledComponent());
        chartsConfiguration.put("retry-trend", createDisabledComponent());
        chartsConfiguration.put("categories-trend", createDisabledComponent());
        chartsConfiguration.put("history-trend", createDisabledComponent());
        return chartsConfiguration;
    }

    private static Component createDisabledComponent()
    {
        Component component = new Component();
        component.setEnabled(false);
        return component;
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.vividus.util.property.PropertyMappedCollection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class LayoutConfiguringPlugin extends DynamicPlugin
{
    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public LayoutConfiguringPlugin(PropertyMappedCollection<Component> tabs,
            PropertyMappedCollection<Component> widgets, PropertyMappedCollection<Component> charts) throws IOException
    {
        super("layout-configuration", () -> {
            List<String> jsFileLines = new ArrayList<>();

            String tabsToExclude = tabs.getData().entrySet().stream()
                    .filter(e -> e.getValue().isDisabled())
                    .map(Entry::getKey)
                    .map("'%s'"::formatted)
                    .collect(Collectors.joining(","));
            if (!tabsToExclude.isEmpty())
            {
                jsFileLines.add("allure.api.tabs = allure.api.tabs.filter(t => ![%s].includes(t.tabName));".formatted(
                        tabsToExclude));
            }

            addJsLinesDisablingComponents(widgets, "delete allure.api.widgets.widgets['%s'];", jsFileLines);
            addJsLinesDisablingComponents(charts, "delete allure.api.widgets.graph['%s'];", jsFileLines);

            return jsFileLines;
        });
    }

    private static void addJsLinesDisablingComponents(PropertyMappedCollection<Component> components,
            String jsLineFormat, List<String> jsFileLines)
    {
        components.getData().entrySet().stream()
                .filter(e -> e.getValue().isDisabled())
                .map(Entry::getKey)
                .map(jsLineFormat::formatted)
                .forEach(jsFileLines::add);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Component
    {
        private Boolean enabled;

        public Boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(Boolean enabled)
        {
            this.enabled = enabled;
        }

        public boolean isDisabled()
        {
            return Boolean.FALSE.equals(isEnabled());
        }
    }
}

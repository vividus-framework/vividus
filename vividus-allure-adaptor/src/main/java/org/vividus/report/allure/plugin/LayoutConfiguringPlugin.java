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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.vividus.util.property.PropertyMappedCollection;

public class LayoutConfiguringPlugin extends DynamicPlugin
{
    public LayoutConfiguringPlugin(PropertyMappedCollection<Component> tabs,
            PropertyMappedCollection<Component> widgets, PropertyMappedCollection<Component> charts) throws IOException
    {
        super("layout-configuration", "styles.css", () -> {
            List<String> cssLines = new ArrayList<>();

            tabs.getData().entrySet().stream()
                    .filter(e -> e.getValue().isDisabled())
                    .map(Entry::getKey)
                    .map(".side-nav__item:has([href=\"#%s\"]) { display: none; }"::formatted)
                    .forEach(cssLines::add);

            widgets.getData().entrySet().stream()
                    .filter(e -> e.getValue().isDisabled())
                    .map(Entry::getKey)
                    .map("[data-widget-id=\"%s\"] { display: none; }"::formatted)
                    .forEach(cssLines::add);

            charts.getData().entrySet().stream()
                    .filter(e -> e.getValue().isDisabled())
                    .map(Entry::getKey)
                    .map("[data-chart-id=\"%s\"] { display: none; }"::formatted)
                    .forEach(cssLines::add);

            return cssLines;
        });
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

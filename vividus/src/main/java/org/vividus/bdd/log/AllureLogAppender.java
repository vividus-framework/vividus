/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.log;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.vividus.bdd.report.allure.AllureStoryReporter;

@Plugin(name = "AllureLogAppender", category = "Core", elementType = "appender", printObject = true)
public class AllureLogAppender extends AbstractAppender
{
    private static AllureLogAppender instance;
    private AllureStoryReporter allureStoryReporter;

    protected AllureLogAppender(String name, Filter filter, Layout<? extends Serializable> layout)
    {
        super(name, filter, layout, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static AllureLogAppender createAppender(@PluginAttribute("name") final String name,
            @PluginElement("Filter") Filter filter, @PluginElement("Layout") Layout<? extends Serializable> layout)
    {
        if (name == null)
        {
            LOGGER.error("No name provided for AllureLogAppender");
            instance = null;
        }
        else
        {
            instance = new AllureLogAppender(name, filter, layout);
        }

        return instance;
    }

    public static AllureLogAppender getInstance()
    {
        return instance;
    }

    @Override
    public void append(LogEvent event)
    {
        if (allureStoryReporter != null)
        {
            String logEntry = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
            String logLevel = event.getLevel().name();
            allureStoryReporter.addLogStep(logLevel, logEntry);
        }
    }

    public void setAllureStoryReporter(AllureStoryReporter allureStoryReporter)
    {
        this.allureStoryReporter = allureStoryReporter;
    }
}

/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.configuration;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.vividus.util.json.JsonPathUtils;

public final class Vividus
{
    private Vividus()
    {
        // Nothing to do
    }

    public static void init()
    {
        createJulToSlf4jBridge();
        configureFreemarkerLogger();
        BeanFactory.open();

        // Load JsonPathUtils to configure JsonPath SPI
        try
        {
            Class.forName(JsonPathUtils.class.getName());
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private static void createJulToSlf4jBridge()
    {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(Level.FINEST);
    }

    private static void configureFreemarkerLogger()
    {
        System.setProperty(freemarker.log.Logger.SYSTEM_PROPERTY_NAME_LOGGER_LIBRARY,
                freemarker.log.Logger.LIBRARY_NAME_SLF4J);
    }
}

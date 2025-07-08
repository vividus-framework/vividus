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

package org.vividus.mcp;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.vividus.configuration.BeanFactory;
import org.vividus.log.LoggerConfigurer;
import org.vividus.util.Sleeper;

public final class McpServer
{
    private static final Duration DEFAULT_SLEEP = Duration.ofSeconds(10);

    private McpServer()
    {
    }

    public static void main(String[] args) throws InterruptedException
    {
        configureLoggers();

        BeanFactory.open();
        BeanFactory.getBean(VividusMcpServer.class).startSyncServer();

        while (true)
        {
            Sleeper.sleep(DEFAULT_SLEEP);
        }
    }

    private static void configureLoggers()
    {
        LoggerConfigurer.configureLoggers();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.removeAppender("console");

        Configurator.reconfigure(config);
    }
}

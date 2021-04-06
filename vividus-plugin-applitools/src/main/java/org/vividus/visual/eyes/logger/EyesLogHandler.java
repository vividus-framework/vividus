/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.visual.eyes.logger;

import java.util.function.Consumer;

import com.applitools.eyes.NullLogHandler;
import com.applitools.eyes.logging.ClientEvent;
import com.applitools.eyes.logging.TraceLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.json.JsonUtils;

public class EyesLogHandler extends NullLogHandler
{
    private final JsonUtils jsonUtils;
    private final Logger logger;

    public EyesLogHandler(Class<?> clazz, JsonUtils jsonUtils)
    {
        logger = LoggerFactory.getLogger(clazz);
        this.jsonUtils = jsonUtils;
    }

    @Override
    public void onMessage(ClientEvent event)
    {
        TraceLevel level = event.getLevel();
        TraceLevel actualLevel = level == null ? TraceLevel.Notice : level;
        switch (actualLevel)
        {
            case Debug:
                logMessage(event, logger::debug);
                break;
            case Warn:
                logMessage(event, logger::warn);
                break;
            case Error:
                logMessage(event, logger::error);
                break;
            case Info:
            case Notice:
            default:
                logMessage(event, logger::info);
        }
    }

    private void logMessage(ClientEvent event, Consumer<String> logger)
    {
        logger.accept(jsonUtils.toPrettyJson(event));
    }

    @Override
    public boolean equals(Object o)
    {
        return super.equals(o);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }
}

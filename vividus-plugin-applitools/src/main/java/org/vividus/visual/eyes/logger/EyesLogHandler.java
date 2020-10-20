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

package org.vividus.visual.eyes.logger;

import com.applitools.eyes.NullLogHandler;
import com.applitools.eyes.logging.TraceLevel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EyesLogHandler extends NullLogHandler
{
    private final Logger logger;

    public EyesLogHandler(Class<?> clazz)
    {
        logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void onMessage(TraceLevel level, String message)
    {
        TraceLevel actualLevel = level == null ? TraceLevel.Notice : level;
        switch (actualLevel)
        {
            case Debug:
                logger.debug(message);
                break;
            case Warn:
                logger.warn(message);
                break;
            case Error:
                logger.error(message);
                break;
            case Info:
            case Notice:
            default:
                logger.info(message);
        }
    }
}

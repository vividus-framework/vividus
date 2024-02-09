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

package org.vividus.configuration;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;
import org.vividus.log.LoggerConfigurer;
import org.vividus.log.TestInfoLogger;
import org.vividus.util.Sleeper;
import org.vividus.util.json.JsonPathUtils;

public final class Vividus
{
    // +2 months after planned migration to Java 21 in order to do not slow down old projects
    private static final LocalDateTime JAVA_MIGRATION_NOTIFICATION_DATE = LocalDateTime.of(2024, 8, 1, 0, 0);

    private Vividus()
    {
    }

    public static void init()
    {
        LoggerConfigurer.configureLoggers();
        TestInfoLogger.drawBanner();
        checkJavaVersion();
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

    private static void checkJavaVersion()
    {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_21))
        {
            return;
        }

        long daysAfterNotification = Duration.between(JAVA_MIGRATION_NOTIFICATION_DATE, LocalDateTime.now()).toDays();
        long secondsToWait = daysAfterNotification > 0 ? daysAfterNotification : 0;
        String lineSeparator = System.lineSeparator();
        String separator = lineSeparator + "====================================================================";
        String message = separator
                + lineSeparator + "    Java of version {} is used."
                + lineSeparator + "    VIVIDUS will require Java 21 starting from July 1, 2024,"
                + lineSeparator + "    you won't be able to run tests using current Java version."
                + lineSeparator + "    Please, upgrade to Java 21 at the earliest convenient time."
                + lineSeparator + "    Execution will resume in {}s."
                + separator;
        LoggerFactory.getLogger(Vividus.class).warn(message, SystemUtils.JAVA_VERSION, secondsToWait);
        Sleeper.sleep(Duration.ofSeconds(secondsToWait));
    }
}

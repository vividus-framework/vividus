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

package org.vividus.configuration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.vividus.log.TestInfoLogger;
import org.vividus.util.Sleeper;
import org.vividus.util.json.JsonPathUtils;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;

public final class Vividus
{
    // +2 months after planned migration to Java 17 in order to do not slow down old projects
    private static final LocalDateTime JAVA_MIGRATION_NOTIFICATION_DATE = LocalDateTime.of(2023, 4, 1, 0, 0);

    private Vividus()
    {
    }

    public static void init()
    {
        configureLog4j2();
        createJulToSlf4jBridge();
        configureFreemarkerLogger();
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

    private static void configureLog4j2()
    {
        Predicate<String> log4j2XmlPredicate = Pattern.compile("log4j2.*\\.xml").asMatchPredicate();
        try (ScanResult scanResult = new ClassGraph().acceptPackagesNonRecursive("").scan();
                ResourceList log4j2Resources = scanResult.getAllResources()
                        .filter(resource -> log4j2XmlPredicate.test(resource.getPath())))
        {
            log4j2Resources.stream()
                .map(Resource::getPath)
                .collect(Collectors.collectingAndThen(Collectors.joining(","),
                    cfg -> System.setProperty(ConfigurationFactory.CONFIGURATION_FILE_PROPERTY, cfg)));
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

    private static void checkJavaVersion()
    {
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_17))
        {
            return;
        }

        long daysAfterNotification = Duration.between(JAVA_MIGRATION_NOTIFICATION_DATE, LocalDateTime.now()).toDays();
        long secondsToWait = daysAfterNotification > 0 ? daysAfterNotification : 0;
        String lineSeparator = System.lineSeparator();
        String separator = lineSeparator + "====================================================================";
        String message = separator
                        + lineSeparator + "    Java of version {} is used."
                        + lineSeparator + "    VIVIDUS will require Java 17 starting from February 1, 2023,"
                        + lineSeparator + "    you won't be able to run tests using current Java instance."
                        + lineSeparator + "    Please, upgrade to Java 17 at the earliest convenient time."
                        + lineSeparator + "    Execution will resume in {}s."
                        + separator;
        LoggerFactory.getLogger(Vividus.class).warn(message, SystemUtils.JAVA_VERSION, secondsToWait);
        Sleeper.sleep(Duration.ofSeconds(secondsToWait));
    }
}

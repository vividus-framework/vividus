/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.runner;

import static org.vividus.configuration.ConfigurationResolver.PLACEHOLDER_PREFIX;
import static org.vividus.configuration.ConfigurationResolver.PLACEHOLDER_SUFFIX;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.DeprecatedPropertiesHandler;
import org.vividus.configuration.Vividus;

public class DeprecatedPropertiesReplacer
{
    private static final String DEPRECATED_PROPERTIES_CLASSPATH =
            "classpath*:/properties/deprecated/deprecated.properties";
    private static final String NOT_COMMENTED_LINE_PATTERN = "(?m)^(?!#)";
    private static final Properties DEPRECATED_PROPERTIES = new Properties();

    private final ResourcePatternResolver resourcePatternResolver;

    public DeprecatedPropertiesReplacer(ResourcePatternResolver resourcePatternResolver)
    {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public static void main(String[] args) throws IOException, ParseException
    {
        Vividus.init();

        Options options = new Options();
        Option directoryOption = new Option("pRD", "propertiesRootDirectory", true, "Directory to find properties.");
        options.addOption(directoryOption);
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        String propertiesRootDirectory = commandLine.getOptionValue(directoryOption.getOpt());

        DeprecatedPropertiesReplacer deprecatedPropertiesReplacer = new DeprecatedPropertiesReplacer(
                BeanFactory.getResourcePatternResolver());
        deprecatedPropertiesReplacer.replaceDeprecatedProperties(propertiesRootDirectory);
    }

    private void replaceDeprecatedProperties(String propertiesRootDirectory) throws IOException
    {
        loadDeprecatedProperties();
        DeprecatedPropertiesHandler deprecatedPropertiesHandler = new DeprecatedPropertiesHandler(DEPRECATED_PROPERTIES,
                PLACEHOLDER_PREFIX, PLACEHOLDER_SUFFIX);

        Resource[] allPropertiesAsResources = resourcePatternResolver
                .getResources(Path.of(propertiesRootDirectory).toUri() + "/**/*.properties");
        for (Resource resource : allPropertiesAsResources)
        {
            String path = resource.getURL().toString();
            if (!path.endsWith("deprecated/deprecated.properties"))
            {
                replaceDeprecatedPropertiesInResource(resource, deprecatedPropertiesHandler);
            }
        }
    }

    private void replaceDeprecatedPropertiesInResource(Resource resource,
            DeprecatedPropertiesHandler deprecatedPropertiesHandler) throws IOException
    {
        String propertiesAsString = resource.getContentAsString(StandardCharsets.UTF_8);
        Properties currentProperties = loadProperties(propertiesAsString);
        Properties actualProperties = (Properties) currentProperties.clone();
        deprecatedPropertiesHandler.replaceDeprecated(actualProperties);

        for (Map.Entry<Object, Object> entry : currentProperties.entrySet())
        {
            String oldKey = (String) entry.getKey();
            Optional<String> actualKey = deprecatedPropertiesHandler.getReplacingPropertyKey(oldKey);

            if (actualKey.isPresent())
            {
                propertiesAsString = propertiesAsString.replaceAll(NOT_COMMENTED_LINE_PATTERN + Pattern.quote(oldKey),
                        Matcher.quoteReplacement(actualKey.get()));
            }
        }
        FileUtils.writeStringToFile(resource.getFile(), propertiesAsString, StandardCharsets.UTF_8);
    }

    private void loadDeprecatedProperties() throws IOException
    {
        Resource[] deprecatedPropertiesAsResources = resourcePatternResolver
                .getResources(DEPRECATED_PROPERTIES_CLASSPATH);
        for (Resource deprecatedPropertiesFile : deprecatedPropertiesAsResources)
        {
            Properties deprecatedProperties = loadProperties(
                    deprecatedPropertiesFile.getContentAsString(StandardCharsets.UTF_8));
            DEPRECATED_PROPERTIES.putAll(deprecatedProperties);
        }
    }

    private Properties loadProperties(String propertiesAsString) throws IOException
    {
        Properties properties = new Properties();
        properties.load(new StringReader(propertiesAsString));
        return properties;
    }
}

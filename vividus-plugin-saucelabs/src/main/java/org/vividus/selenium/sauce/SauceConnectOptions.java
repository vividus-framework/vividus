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

package org.vividus.selenium.sauce;

import static org.vividus.util.ResourceUtils.createTempFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.vividus.selenium.tunnel.TunnelOptions;

public class SauceConnectOptions extends TunnelOptions
{
    private static final String PAC_FILE_CONTENT_FORMAT =
              "function FindProxyForURL(url, host) {%n"
            + "    if (%s) {%n"
            + "        // KGP and REST connections. Another proxy can also be specified%n"
            + "        return \"DIRECT\";%n"
            + "    }%n"
            + "    // Test HTTP traffic, route it through the custom proxy%n"
            + "    return \"PROXY %s\";%n"
            + "}%n";
    private static final String FILE_PROTOCOL = "file://";
    private static final String FULL_FILE_PROTOCOL = FILE_PROTOCOL + (SystemUtils.IS_OS_WINDOWS ? "/" : "");

    private final boolean useLatestSauceConnect;
    private final String restUrl;
    private final String customArguments;
    private final Set<String> skipHostGlobPatterns;

    public SauceConnectOptions(boolean useLatestSauceConnect, String restUrl, String customArguments,
            Set<String> skipHostGlobPatterns)
    {
        this.useLatestSauceConnect = useLatestSauceConnect;
        this.restUrl = restUrl;
        this.customArguments = customArguments;
        this.skipHostGlobPatterns = new TreeSet<>(skipHostGlobPatterns);
        this.skipHostGlobPatterns.addAll(List.of(
            "*.miso.saucelabs.com",
            "*.api.testobject.com",
            "*.saucelabs.com",
            "saucelabs.com"
        ));
    }

    public String build(String tunnelName) throws IOException
    {
        StringBuilder options = Optional.ofNullable(customArguments).map(args -> new StringBuilder(args).append(' '))
                .orElseGet(StringBuilder::new);
        if (tunnelName != null)
        {
            appendOption(options, "tunnel-name", tunnelName);
            appendOption(options, "pidfile", createPidFile(tunnelName).toString());
        }

        if (getProxy() != null)
        {
            Path pacFilePath = createPacFile(tunnelName);

            String pacFileUrl = useLatestSauceConnect ? FULL_FILE_PROTOCOL + pacFilePath
                    : FILE_PROTOCOL + FilenameUtils.separatorsToUnix(pacFilePath.toString());

            appendOption(options, "pac", pacFileUrl);
        }
        if (restUrl != null)
        {
            appendOption(options, "rest-url", restUrl);
        }
        appendOption(options, "tunnel-pool");
        return options.substring(0, options.length() - 1);
    }

    private Path createPacFile(String tunnelName) throws IOException
    {
        return createTempFile("pac-saucelabs-" + tunnelName, ".js",
                String.format(PAC_FILE_CONTENT_FORMAT, getSkipShExpMatcher(), getProxy()));
    }

    private String getSkipShExpMatcher()
    {
        return skipHostGlobPatterns.stream()
                                   .map(host -> String.format("shExpMatch(host, \"%s\")", host))
                                   .collect(Collectors.joining(" || "));
    }

    private Path createPidFile(String tunnelName) throws IOException
    {
        return createTempFile("sc_client-" + tunnelName + "-", ".pid", null);
    }

    private static void appendOption(StringBuilder stringBuilder, String name, String... values)
    {
        stringBuilder.append("--").append(name).append(' ');
        Stream.of(values).forEach(value -> stringBuilder.append(value).append(' '));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }
        SauceConnectOptions that = (SauceConnectOptions) o;
        return useLatestSauceConnect == that.useLatestSauceConnect && Objects.equals(restUrl, that.restUrl)
                && Objects.equals(skipHostGlobPatterns, that.skipHostGlobPatterns);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), useLatestSauceConnect, restUrl, skipHostGlobPatterns);
    }
}

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

package org.vividus.selenium.sauce;

import static org.vividus.util.ResourceUtils.createTempFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.vividus.selenium.tunnel.TunnelOptions;

public class SauceConnectOptions extends TunnelOptions
{
    private static final String PAC_FILE_CONTENT_FORMAT =
              "function FindProxyForURL(url, host) {%n"
            + "    if (shExpMatch(host, \"*.miso.saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"*.api.testobject.com\") ||%n"
            + "        shExpMatch(host, \"*.saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"saucelabs.com\") ||%n"
            + "        shExpMatch(host, \"%1$s\")) {%n"
            + "        // KGP and REST connections. Another proxy can also be specified%n"
            + "        return \"DIRECT\";%n"
            + "    }%n"
            + "    // Test HTTP traffic, route it through the custom proxy%n"
            + "    return \"PROXY %2$s\";%n"
            + "}%n";

    private String skipProxyHostsPattern;
    private String restUrl;
    private String customArguments;

    public void setSkipProxyHostsPattern(String skipProxyHostsPattern)
    {
        this.skipProxyHostsPattern = skipProxyHostsPattern;
    }

    public void setRestUrl(String restUrl)
    {
        this.restUrl = restUrl;
    }

    public String build(String tunnelIdentifier) throws IOException
    {
        StringBuilder options = Optional.ofNullable(customArguments).map(args -> new StringBuilder(args).append(' '))
                .orElseGet(StringBuilder::new);
        if (tunnelIdentifier != null)
        {
            appendOption(options, "tunnel-identifier", tunnelIdentifier);
            appendOption(options, "pidfile", createPidFile(tunnelIdentifier).toString());
        }

        if (getProxy() != null)
        {
            /*
             * Separators conversion added as workaround for SauceConnect bug (doesn't take into account windows like
             * PAC-file path delimiters). Sauce lab ticket link: https://support.saucelabs.com/hc/en-us/requests/38183
             * Affected SauceConnect version: 4.4.4 and above.
             * */
            appendOption(options, "pac",
                    "file://" + FilenameUtils.separatorsToUnix(createPacFile(tunnelIdentifier).toString()));
        }
        if (restUrl != null)
        {
            appendOption(options, "rest-url", restUrl);
        }
        appendOption(options, "no-remove-colliding-tunnels");
        appendOption(options, "no-proxy-caching");
        return options.substring(0, options.length() - 1);
    }

    private Path createPacFile(String tunnelIdentifier) throws IOException
    {
        return createTempFile("pac-saucelabs-" + tunnelIdentifier, ".js",
                String.format(PAC_FILE_CONTENT_FORMAT, skipProxyHostsPattern, getProxy()));
    }

    private Path createPidFile(String tunnelIdentifier) throws IOException
    {
        return createTempFile("sc_client-" + tunnelIdentifier + "-", ".pid", null);
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
        if (!super.equals(o) || !(o instanceof SauceConnectOptions))
        {
            return false;
        }
        SauceConnectOptions that = (SauceConnectOptions) o;
        return Objects.equals(skipProxyHostsPattern, that.skipProxyHostsPattern)
                && Objects.equals(restUrl, that.restUrl);
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + Objects.hash(skipProxyHostsPattern, restUrl);
    }

    public void setCustomArguments(String customArguments)
    {
        this.customArguments = customArguments;
    }
}

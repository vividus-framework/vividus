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

package org.vividus.sauce;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class SauceConnectOptions
{
    private static final String PAC_FILE_CONTENT_FORMAT = "function FindProxyForURL(url, host) { "
            + "if (shExpMatch(host, \"*.miso.saucelabs.com\") || shExpMatch(host, \"saucelabs.com\")) {"
            + "return \"DIRECT\";}return \"PROXY %s\";}";

    private static final String SAUCE_CONNECT_AUTH_FORMAT = "%s:%d:%s";
    private static final int DEFAULT_HOST_PORT = 80;
    private static final String LOCALHOST_IP = "127.0.0.1";

    private String proxy;
    private String host;
    private String basicAuthUser;
    private int port;

    /**
     * Sets the proxy &lt;host:port&gt;.
     * <br>
     * Make sure that the format is the following, if the proxy is running on a local machine: <b>127.0.0.1:port</b>
     * @param proxy Proxy <b>host:port</b>
     */
    public void setProxy(String proxy)
    {
        this.proxy = proxy.replace("localhost", LOCALHOST_IP);
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public void setBasicAuthUser(String basicAuthUser)
    {
        this.basicAuthUser = basicAuthUser;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String build(String tunnelIdentifier) throws IOException
    {
        StringBuilder options = new StringBuilder();
        if (tunnelIdentifier != null)
        {
            appendOption(options, SauceConnectOptionName.TUNNEL_IDENTIFIER, tunnelIdentifier);
            appendOption(options, SauceConnectOptionName.PID_FILE, createPidFile(tunnelIdentifier).toString());
        }
        if (host != null && basicAuthUser != null)
        {
            appendOption(options, SauceConnectOptionName.AUTH,
                    String.format(SAUCE_CONNECT_AUTH_FORMAT, host, port > 0 ? port : DEFAULT_HOST_PORT, basicAuthUser));
        }
        if (proxy != null)
        {
            /*
             * Separators conversion added as workaround for SauceConnect bug (doesn't take into account windows like
             * PAC-file path delimiters). Sauce lab ticket link: https://support.saucelabs.com/hc/en-us/requests/38183
             * Affected SauceConnect version: 4.4.4 and above.
             */
            appendOption(options, SauceConnectOptionName.PAC,
                    "file://" + FilenameUtils.separatorsToUnix(createPacFile(tunnelIdentifier).toString()));
        }
        appendOption(options, SauceConnectOptionName.NO_REMOVE_COLLIDING_TUNNELS);
        appendOption(options, SauceConnectOptionName.NO_PROXY_CASHING);
        return options.length() > 0 ? options.substring(0, options.length() - 1) : "";
    }

    private Path createPacFile(String tunnelIdentifier) throws IOException
    {
        return createTempFile("pac-" + tunnelIdentifier, ".js", String.format(PAC_FILE_CONTENT_FORMAT, proxy));
    }

    private Path createPidFile(String tunnelIdentifier) throws IOException
    {
        return createTempFile("sc_client-" + tunnelIdentifier + "-", ".pid");
    }

    private Path createTempFile(String prefix, String suffix, String... lines) throws IOException
    {
        Path tempFilePath = Files.createTempFile(prefix, suffix);
        File tempFile = tempFilePath.toFile();
        FileUtils.writeLines(tempFile, StandardCharsets.UTF_8.toString(), Arrays.asList(lines));
        tempFile.deleteOnExit();
        return tempFilePath;
    }

    private static void appendOption(StringBuilder stringBuilder, SauceConnectOptionName name, String... values)
    {
        stringBuilder.append("--").append(name.getOptionName()).append(' ');
        Stream.of(values).forEach(value -> stringBuilder.append(value).append(' '));
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(proxy, host, basicAuthUser, port);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof SauceConnectOptions))
        {
            return false;
        }
        SauceConnectOptions other = (SauceConnectOptions) obj;
        return Objects.equals(proxy, other.proxy) && Objects.equals(host, other.host)
                && Objects.equals(basicAuthUser, other.basicAuthUser)
                && port == other.port;
    }

    enum SauceConnectOptionName
    {
        TUNNEL_IDENTIFIER("tunnel-identifier"),
        PID_FILE("pidfile"),
        AUTH("auth"),
        PAC("pac"),
        NO_REMOVE_COLLIDING_TUNNELS("no-remove-colliding-tunnels"),
        NO_PROXY_CASHING("no-proxy-caching");

        private final String optionName;

        SauceConnectOptionName(String optionName)
        {
            this.optionName = optionName;
        }

        public String getOptionName()
        {
            return optionName;
        }
    }
}

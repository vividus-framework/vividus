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

package org.vividus.selenium.sauce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SauceConnectOptionsTests
{
    private static final String SPACE = " ";
    private static final String PAC_FILE = "--pac file://";
    private static final String DOT_JS = ".js";
    private static final String PAC_TEST_TUNNEL = "pac-test-tunnel";
    private static final String PROXY = "test";
    private static final String TUNNEL_IDENTIFIER = "test-tunnel";
    private static final String PID_FILE_NAME = "sc_client-" + TUNNEL_IDENTIFIER + "-";
    private static final String TUNNEL_IDENTIFIER_OPTION = "--tunnel-identifier" + SPACE + TUNNEL_IDENTIFIER;
    private static final String PID_EXTENSION = ".pid";
    private static final String NO_REMOVE_COLLIDING_TUNNELS = "--no-remove-colliding-tunnels";
    private static final String NO_PROXY_CACHING = "--no-proxy-caching";
    private static final String SAUCE_LABS_REST_URL = "https://saucelabs.com/rest/v1/";

    private static final String PID_FILE = "--pidfile";

    private final SauceConnectOptions sauceConnectOptions = new SauceConnectOptions();

    @Test
    void testBuildWithProxy() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            Path pacPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PAC_TEST_TUNNEL, DOT_JS)).thenReturn(pacPath);
            File pacFile = mock(File.class);
            when(pacPath.toFile()).thenReturn(pacFile);
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + pacPath + SPACE
                            + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verifyTempFiles(fileUtils, pacFile, pidFile);
        }
    }

    @Test
    void testBuildWithProxyWindowsPathDelimiters() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            Path pacPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PAC_TEST_TUNNEL, DOT_JS)).thenReturn(pacPath);
            File pacFile = mock(File.class);
            when(pacPath.toFile()).thenReturn(pacFile);
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            when(pacPath.toString()).thenReturn("c:\\user\\temp.js");
            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + "c:/user/temp.js"
                            + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verifyTempFiles(fileUtils, pacFile, pidFile);
        }
    }

    @Test
    void testBuildWithProxyWithAuth() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        String customFlags = "--auth host:9999:user:pass";
        sauceConnectOptions.setCustomArguments(customFlags);
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            Path pacPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PAC_TEST_TUNNEL, DOT_JS)).thenReturn(pacPath);
            File pacFile = mock(File.class);
            when(pacPath.toFile()).thenReturn(pacFile);
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            assertEquals(customFlags + SPACE + TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE
                            + PAC_FILE + pacPath + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verifyTempFiles(fileUtils, pacFile, pidFile);
        }
    }

    @Test
    void testBuildWithProxyWithSkipHostsPattern() throws IOException
    {
        sauceConnectOptions.setProxy(PROXY);
        sauceConnectOptions.setSkipProxyHostsPattern("example.com");
        File pacFile = mock(File.class);
        Path pacPath = mock(Path.class);
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            files.when(() -> Files.createTempFile(PAC_TEST_TUNNEL, DOT_JS)).thenReturn(pacPath);
            when(pacPath.toFile()).thenReturn(pacFile);
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + PAC_FILE + pacPath + SPACE
                            + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verifyTempFiles(fileUtils, pacFile, pidFile);
        }
    }

    @Test
    void testBuildWOProxy() throws IOException
    {
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            assertEquals(
                    TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + NO_REMOVE_COLLIDING_TUNNELS
                            + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verify(pidFile).deleteOnExit();
            fileUtils.verify(() -> FileUtils.writeLines(pidFile, StandardCharsets.UTF_8.toString(), List.of()));
        }
    }

    @Test
    void testBuildWOProxyWithNoSslBumpDomains() throws IOException
    {
        String noSslValue = "all";
        String noSslOption = "--no-ssl-bump-domains" + SPACE + noSslValue;
        try (MockedStatic<Files> files = mockStatic(Files.class); MockedStatic<FileUtils> fileUtils = mockStatic(
                FileUtils.class))
        {
            Path pidPath = mock(Path.class);
            files.when(() -> Files.createTempFile(PID_FILE_NAME, PID_EXTENSION)).thenReturn(pidPath);
            File pidFile = mock(File.class);
            when(pidPath.toFile()).thenReturn(pidFile);
            sauceConnectOptions.setNoSslBumpDomains(noSslValue);
            assertEquals(TUNNEL_IDENTIFIER_OPTION + SPACE + PID_FILE + SPACE + pidPath + SPACE + noSslOption + SPACE
                            + NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING,
                    sauceConnectOptions.build(TUNNEL_IDENTIFIER));
            verify(pidFile).deleteOnExit();
            fileUtils.verify(() -> FileUtils.writeLines(pidFile, StandardCharsets.UTF_8.toString(), List.of()));
        }
    }

    @Test
    void testBuildWOProxyNullOption() throws IOException
    {
        assertEquals(NO_REMOVE_COLLIDING_TUNNELS + SPACE + NO_PROXY_CACHING, sauceConnectOptions.build(null));
    }

    @Test
    void testBuildWithRestUrl() throws IOException
    {
        sauceConnectOptions.setRestUrl(SAUCE_LABS_REST_URL);
        assertEquals("--rest-url" + SPACE + SAUCE_LABS_REST_URL + SPACE + NO_REMOVE_COLLIDING_TUNNELS + SPACE
                + NO_PROXY_CACHING, sauceConnectOptions.build(null));
    }

    @Test
    void testHashCode()
    {
        assertEquals(createDefaultOptions().hashCode(), createDefaultOptions().hashCode());
    }

    @Test
    void testEqualsDifferentObjects()
    {
        assertEquals(createDefaultOptions(), createDefaultOptions());
    }

    @Test
    void testEqualsSameObjects()
    {
        SauceConnectOptions options = createDefaultOptions();
        assertEquals(options, options);
    }

    @Test
    void testNotEqualsToNull()
    {
        assertFalse(createDefaultOptions().equals(null));
    }

    @Test
    void testNotEqualsProxy()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setProxy(null);
        assertNotEquals(options, createDefaultOptions());
    }

    @Test
    void testNotEqualsNoSslBumpDomains()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setNoSslBumpDomains(null);
        assertNotEquals(options, createDefaultOptions());
    }

    @Test
    void testNotEqualsSkipProxyHostsPattern()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setSkipProxyHostsPattern(null);
        assertNotEquals(options, createDefaultOptions());
    }

    @Test
    void testNotEqualsRestUrl()
    {
        SauceConnectOptions options = createDefaultOptions();
        options.setRestUrl(null);
        assertNotEquals(options, createDefaultOptions());
    }

    private SauceConnectOptions createDefaultOptions()
    {
        SauceConnectOptions options = new SauceConnectOptions();
        options.setProxy(PROXY);
        options.setNoSslBumpDomains("--no-ssl-bump-domains all");
        options.setSkipProxyHostsPattern("vividus\\.dev");
        options.setRestUrl(SAUCE_LABS_REST_URL);
        options.setCustomArguments("--verbose");
        return options;
    }

    private void verifyTempFiles(MockedStatic<FileUtils> fileUtils, File pacFile, File pidFile)
    {
        verify(pidFile).deleteOnExit();
        fileUtils.verify(() -> {
            FileUtils.writeLines(pidFile, StandardCharsets.UTF_8.toString(), List.of());
            FileUtils.writeLines(pacFile, StandardCharsets.UTF_8.toString(),
                    List.of("function FindProxyForURL(url, host) { if (shExpMatch(host, \"*.miso.saucelabs.com\")"
                            + "|| shExpMatch(host, \"saucelabs.com\")|| shExpMatch(host, \"example.com\")) "
                            + "{return \"DIRECT\";}return \"PROXY test\";}"));
        });
    }
}

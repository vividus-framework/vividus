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

package org.vividus.report.allure.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AllureReportPatchingConsistencyTests
{
    private static final String IS_ENTRY = "isEntry";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static Stream<Arguments> allureSourceContainsText()
    {
        return Stream.of(
                Arguments.of("unknown:`Unknown`"),
                Arguments.of("`failed`,`broken`,`passed`,`skipped`,`unknown`"),
                Arguments.of("--color-status-broken-bg:var(--palette-amber-day-darken-2)"),
                Arguments.of("--color-status-unknown-bg:var(--palette-violet-day-darken-1)"));
    }

    @ParameterizedTest
    @MethodSource("allureSourceContainsText")
    void testAllureSourceContainsText(String text) throws IOException
    {
        String content = findAssetJsContent();
        assertNotNull(content, "Could not find JS asset file from vite-manifest.json");
        assertTrue(content.contains(text),
                "JS asset file does not contain expected text: %s".formatted(text));
    }

    private String findAssetJsContent() throws IOException
    {
        try (InputStream manifestStream = getClass().getClassLoader().getResourceAsStream("vite-manifest.json"))
        {
            assertNotNull(manifestStream, "vite-manifest.json not found in classpath");
            JsonNode manifest = MAPPER.readTree(manifestStream);
            String jsFileName = null;
            for (var entry : manifest.properties())
            {
                JsonNode node = entry.getValue();
                if (node.has(IS_ENTRY) && node.get(IS_ENTRY).asBoolean())
                {
                    jsFileName = node.get("file").asText();
                    break;
                }
            }
            assertNotNull(jsFileName, "No entry JS file found in vite-manifest.json");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsFileName))
            {
                assertNotNull(is, "File %s not found in classpath".formatted(jsFileName));
                return IOUtils.toString(is, StandardCharsets.UTF_8);
            }
        }
    }
}

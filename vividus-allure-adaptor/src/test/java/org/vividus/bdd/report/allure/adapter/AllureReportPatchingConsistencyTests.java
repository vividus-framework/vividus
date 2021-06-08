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

package org.vividus.bdd.report.allure.adapter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AllureReportPatchingConsistencyTests
{
    private static final String APP_JS = "/app.js";
    private static final String STYLES_CSS = "/styles.css";

    static Stream<Arguments> allureSourceContainsText()
    {
        return Stream.of(
                Arguments.of(APP_JS, "\"unknown\":\"Unknown\""),
                Arguments.of(APP_JS, "\"failed\",\"broken\",\"passed\",\"skipped\",\"unknown\""),
                Arguments.of(STYLES_CSS, "#ffd050"),
                Arguments.of(STYLES_CSS, "#d35ebe"),
                Arguments.of(STYLES_CSS, "#fffae6"),
                Arguments.of(STYLES_CSS, "#faebf7"),
                Arguments.of(STYLES_CSS, "#ffeca0"),
                Arguments.of(STYLES_CSS, "#ecb7e2"));
    }

    @ParameterizedTest
    @MethodSource("allureSourceContainsText")
    void testAllureSourceContainsText(String fileName, String text) throws IOException
    {
        assertTrue(readFile(fileName).contains(text));
    }

    private String readFile(String fileName) throws IOException
    {
        try (InputStream is = getClass().getResourceAsStream(fileName))
        {
            assertNotNull(is);
            return IOUtils.toString(is, StandardCharsets.UTF_8);
        }
    }
}

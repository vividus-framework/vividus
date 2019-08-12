/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;

@ExtendWith(MockitoExtension.class)
class LinkUrlSearchUtilsTests
{
    private static final String SOME_URL = "/someUrl";
    private static final String SIMPLE_URL = "http://example.com";
    private static final String OTHER_URL = "http://page.com";
    private static final String PORT = ":8080";
    private static final String SIMPLE_URL_WITH_PATH = SIMPLE_URL + SOME_URL;
    private static final String SIMPLE_URL_WITH_PATH_HTTPS = "https://example.com" + SOME_URL;
    private static final String PART = "#part";

    @Mock
    private WebDriver webDriver;

    static Stream<Arguments> hrefProvider()
    {
        //CHECKSTYLE:OFF
        return Stream.of(
            Arguments.of(null,                 null,                       SIMPLE_URL,                 SIMPLE_URL            ),
            Arguments.of(null,                 SIMPLE_URL,                 null,                       SIMPLE_URL            ),
            Arguments.of(SOME_URL,             SOME_URL,                   SIMPLE_URL_WITH_PATH,       SIMPLE_URL            ),
            Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH_HTTPS, SIMPLE_URL_WITH_PATH,       SIMPLE_URL            ),
            Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH_HTTPS, SIMPLE_URL_WITH_PATH,       SIMPLE_URL + PORT     ),
            Arguments.of(SIMPLE_URL,           SOME_URL,                   SIMPLE_URL,                 OTHER_URL             ),
            Arguments.of(PART,                 PART,                       "http://example.com#part",  SIMPLE_URL            ),
            Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL_WITH_PATH,       SIMPLE_URL_WITH_PATH,       SIMPLE_URL            ),
            Arguments.of("someUrl",            SIMPLE_URL,                 SIMPLE_URL_WITH_PATH,       "http://example.com/" ),
            Arguments.of(SIMPLE_URL_WITH_PATH, SIMPLE_URL,                 SIMPLE_URL_WITH_PATH,       "https://example.com/")
        );
        //CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("hrefProvider")
    void testGetCurrentHrefDifferentScheme(String expected, String expectedUrl, String href, String currentUrl)
    {
        Mockito.lenient().when(webDriver.getCurrentUrl()).thenReturn(currentUrl);
        assertEquals(expected, LinkUrlSearchUtils.getCurrentHref(href, expectedUrl, webDriver));
    }

    @Test
    void testGetCurrentHrefMalformedUrl()
    {
        when(webDriver.getCurrentUrl()).thenReturn("data,;");
        assertThrows(IllegalStateException.class,
            () -> LinkUrlSearchUtils.getCurrentHref(SIMPLE_URL, SIMPLE_URL, webDriver));
    }
}

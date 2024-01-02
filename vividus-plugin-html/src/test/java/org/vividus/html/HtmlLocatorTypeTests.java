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

package org.vividus.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;

class HtmlLocatorTypeTests
{
    private static final String TITLE = "title";
    private static final String HTML = """
            <!DOCTYPE html>
            <html>
            <head>
            <title>Title of the document</title>
            </head>
            <body />
            </html>""";

    @Test
    void shouldGetElementsBySelectorHtmlWithBaseUrl()
    {
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class))
        {
            String baseUri = "base-uri";
            Document document = mock();
            Elements elements = mock();

            jsoup.when(() -> Jsoup.parse(HTML, baseUri)).thenReturn(document);
            when(document.select(TITLE)).thenReturn(elements);

            assertEquals(elements, HtmlLocatorType.CSS_SELECTOR.findElements(baseUri, HTML, TITLE));

            jsoup.verify(() -> Jsoup.parse(HTML, baseUri));
            jsoup.verifyNoMoreInteractions();
        }
    }

    @ParameterizedTest
    @CsvSource({"CSS_SELECTOR, body", "XPATH, //body"})
    void shouldFindElement(HtmlLocatorType locatorType, String locator)
    {
        assertEquals(1, locatorType.findElements(HTML, locator).size());
    }

    @ParameterizedTest
    @CsvSource({"CSS_SELECTOR, CSS selector", "XPATH, XPath"})
    void shouldProvideHumanReadableDescription(HtmlLocatorType locatorType, String expected)
    {
        assertEquals(expected, locatorType.getDescription());
    }
}

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

package org.vividus.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class HtmlUtilsTests
{
    private static final String TITLE = "title";
    private static final String HTML =
            "<!DOCTYPE html>\n"
          + "<html>\n"
          + "<head>\n"
          + "<title>Title of the document</title>\n"
          + "</head>\n"
          + "<body />\n"
          + "</html>";

    @Test
    void shouldGetElementsBySelector()
    {
        assertEquals("Title of the document", HtmlUtils.getElements(HTML, TITLE).get(0).text());
    }

    @Test
    void shouldGetElementsBySelectorHtmlWithBaseUrl()
    {
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class))
        {
            String baseUri = "base-uri";
            Document document = mock(Document.class);
            Elements elements = mock(Elements.class);

            jsoup.when(() -> Jsoup.parse(HTML, baseUri)).thenReturn(document);
            when(document.select(TITLE)).thenReturn(elements);

            assertEquals(elements, HtmlUtils.getElements(baseUri, HTML, TITLE));

            jsoup.verify(() -> Jsoup.parse(HTML, baseUri));
            jsoup.verifyNoMoreInteractions();
        }
    }
}

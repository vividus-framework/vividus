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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class JsoupUtilsTests
{
    @Test
    void shouldGetDocumentFromHtml()
    {
        String html = "<html></html>";
        try (MockedStatic<Jsoup> jsoup = mockStatic(Jsoup.class))
        {
            Document document = mock();
            jsoup.when(() -> Jsoup.parse(html, StringUtils.EMPTY)).thenReturn(document);

            assertEquals(document, JsoupUtils.getDocument(html));
            jsoup.verify(() -> Jsoup.parse(html, StringUtils.EMPTY));
            jsoup.verifyNoMoreInteractions();
        }
    }
}

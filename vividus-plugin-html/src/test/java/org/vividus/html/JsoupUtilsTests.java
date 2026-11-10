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

package org.vividus.html;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

class JsoupUtilsTests
{
    @Test
    void shouldGetDocumentFromHtml()
    {
        var html = """
                <html>
                 <head>
                  <!--
                   The element below is self-closing and it does not follow HTML5 spec, but we need to parse any HTML-s,
                   that's why we relaxed the rules to allow any self-closing tags
                  -->
                  <script src='https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.4.1.min.js' />
                 </head>
                 <body></body>
                </html>""";
        Document document = JsoupUtils.getDocument(html);
        var expected = """
                <html>
                 <head>
                  <!--
                   The element below is self-closing and it does not follow HTML5 spec, but we need to parse any HTML-s,
                   that's why we relaxed the rules to allow any self-closing tags
                  -->
                  <script src="https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.4.1.min.js"></script>
                 </head>
                 <body></body>
                </html>""";
        assertEquals(expected, document.outerHtml());
    }
}

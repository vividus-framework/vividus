/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.web.action.search;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

class AbstractWebElementSearchActionTests
{
    private static final String PAGE = "<main><h1>Видеоматериалы</h1><h2>Videos</h2><p>їжак</p><p>ЇЖАК</p>"
            + "<p>Ёжик</p><p>ЄІЇҐЎ</p></main>";

    @ParameterizedTest
    @CsvSource({
        // Cyrillic text is found whatever the case of the page text and of the locator value is
        "Видеоматериалы, 1",
        "видеоматериалы, 1",
        "ВИДЕОМАТЕРИАЛЫ, 1",
        // Ukrainian letters and Yo
        "їжак, 2",
        "ЁЖИК, 1",
        // every Ukrainian and Belarusian letter that has no Russian counterpart, in both cases
        "єіїґў, 1",
        "ЄІЇҐЎ, 1",
        // Latin still works
        "videos, 1",
        "VIDEOS, 1",
        // Another text is not found
        "нету, 0"
    })
    void shouldFindTextCaseInsensitively(String text, int expectedCount) throws Exception
    {
        String xpath = AbstractWebElementSearchAction.generateCaseInsensitiveXpath(text, "*");
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(PAGE)));
        NodeList nodes = (NodeList) XPathFactory.newInstance().newXPath().evaluate(xpath, document,
                XPathConstants.NODESET);
        assertEquals(expectedCount, nodes.getLength());
    }
}

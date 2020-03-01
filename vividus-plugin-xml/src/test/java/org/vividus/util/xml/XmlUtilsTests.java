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

package org.vividus.util.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.util.ResourceUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class XmlUtilsTests
{
    private static final String XML = "<test><data>value1</data><data>value2</data></test>";
    private static final String XSD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
            + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
            + "  <xs:element name=\"test\" type=\"xs:string\"/>"
            + "</xs:schema>";

    @Test
    void shouldReturnXmlByXpath()
    {
        assertEquals(Optional.of("value1"), XmlUtils.getXmlByXpath(XML, "//data/text()"));
    }

    @Test
    void shouldThrowExceptionInCaseOfInvalidXpath()
    {
        assertThrows(IllegalStateException.class, () -> XmlUtils.getXmlByXpath(XML, "<invalidXpath>"));
    }

    @Test
    void shouldConvertXmlStringToDocument()
    {
        assertThat(XmlUtils.convertToDocument(XML), instanceOf(Document.class));
    }

    @Test
    void shouldThrowExceptionInCaseOfInvalidXmlOnConversion()
    {
        assertThrows(IllegalStateException.class, () -> XmlUtils.convertToDocument("<invalidXml>"));
    }

    @Test
    void shouldValidateXmlAgainstXsd() throws IOException, SAXException
    {
        XmlUtils.validateXmlAgainstXsd("<test>value</test>", XSD);
    }

    @Test
    void shouldThrowExceptionOnValidationOfInvalidXmlAgainstXsd()
    {
        assertThrows(SAXException.class, () -> XmlUtils.validateXmlAgainstXsd(XML, XSD));
    }

    @Test
    void shouldTransformXmlSuccessfully()
    {
        String xslt = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
                + "  <xsl:output method=\"xml\" omit-xml-declaration=\"yes\" />"
                + "  <xsl:template match=\"test\">"
                + "        <xsl:copy>"
                + "           <xsl:text>xslt test</xsl:text>"
                + "        </xsl:copy>"
                + "    </xsl:template>" + "</xsl:stylesheet>";
        XmlUtils.transform(XML, xslt, xml -> assertEquals("<test>xslt test</test>", xml), Assertions::fail);
    }

    @Test
    void shouldProvideExceptionThrownOnXmlTransformation()
    {
        XmlUtils.transform(XML, "<xsl />", xml -> fail(), e -> assertThat(e, instanceOf(TransformerException.class)));
    }

    static Stream<Arguments> checkXmlFormatting()
    {
        // @formatter:off
        return Stream.of(
            Arguments.of(loadResource("non-formatted.xml"), loadResource("formatted.xml")),
            Arguments.of("exception",                                 null)
        );
        // @formatter:on
    }

    private static String loadResource(String resource)
    {
        return ResourceUtils.loadResource(XmlUtilsTests.class, resource).replaceAll("\r\n|\n", System.lineSeparator());
    }

    @ParameterizedTest(name = "{index}: for xml value {0}, formatted result is {1}")
    @MethodSource("checkXmlFormatting")
    void testFormatXml(String xmlString, String expected)
    {
        assertEquals(Optional.ofNullable(expected), XmlUtils.format(xmlString));
    }
}

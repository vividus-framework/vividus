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

package org.vividus.steps.xml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlunit.builder.DiffBuilder;

@ExtendWith(MockitoExtension.class)
class XmlStepsTests
{
    private static final String XML = "<test>data</test>";
    private static final String TEST_XPATH = "//test";
    private static final String XPATH = TEST_XPATH + "/text()";
    private static final String NAME = "name";

    @Mock
    private VariableContext variableContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private XmlSteps xmlValidationSteps;

    @Test
    void shouldSaveDataByXpathIntoScopeVariable() throws XPathExpressionException
    {
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);
        xmlValidationSteps.saveDataByXpath(XPATH, XML, scopes, NAME);
        verify(variableContext).putVariable(scopes, NAME, "data");
    }

    @Test
    void shouldNotSaveDataByXpathIntoScopeVariableIfXmlIsNotWellFormed() throws XPathExpressionException
    {
        xmlValidationSteps.saveDataByXpath(XPATH, NAME, Set.of(VariableScope.STEP), NAME);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldSaveNumberOfElements() throws XPathExpressionException
    {
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);
        xmlValidationSteps.saveNumberOfElements(TEST_XPATH, XML, scopes, NAME);
        verify(variableContext).putVariable(scopes, NAME, 1);
    }

    @Test
    void shouldNotSaveNumberOfElementsIfXmlIsNotWellFormed() throws XPathExpressionException
    {
        xmlValidationSteps.saveNumberOfElements(TEST_XPATH, NAME, Set.of(VariableScope.STEP), NAME);
        verifyNoInteractions(variableContext);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldValidateXmlElementExistenceByXpath() throws SAXException, IOException
    {
        xmlValidationSteps.doesElementExistByXpath(XML, XPATH);
        verify(softAssert).assertThat(eq("XML has element with XPath: " + XPATH), any(Document.class),
                any(Matcher.class));
    }

    @Test
    void shouldNotValidateXmlElementExistenceByXpathIfXmlIsNotWellFormed() throws SAXException, IOException
    {
        xmlValidationSteps.doesElementExistByXpath(NAME, XPATH);
        verify(softAssert, never()).assertThat(any(), any(), any());
    }

    @Test
    void shouldCompareXmls()
    {
        xmlValidationSteps.compareXmls(XML, XML);
    }

    @Test
    void shouldFailComparisonOnDifferentXmls()
    {
        xmlValidationSteps.compareXmls("<test><data>diffValue</data></test>", "<test><data>value</data></test>");
        verify(softAssert).recordFailedAssertion("Expected text value 'value' but was 'diffValue' - comparing "
                + "<data ...>value</data> at /test[1]/data[1]/text()[1] to <data ...>diffValue</data> at "
                + "/test[1]/data[1]/text()[1] (DIFFERENT)" + System.lineSeparator());
    }

    @ParameterizedTest
    @CsvSource({
        "<test>data</test>, invalid_xml",
        "invalid_xml, <test>data</test>"
    })
    void shouldNotCompareXmls(String left, String right)
    {
        try (MockedStatic<DiffBuilder> diffBuilder = Mockito.mockStatic(DiffBuilder.class))
        {
            xmlValidationSteps.compareXmls(left, right);
            diffBuilder.verifyNoInteractions();
        }
    }

    @Test
    void shouldValidateXmlAgainstXsd()
    {
        xmlValidationSteps.validateXmlAgainstXsd(XML, loadXsd());
    }

    @Test
    void shouldProcessXsdValidationException()
    {
        xmlValidationSteps.validateXmlAgainstXsd("<test2>value</test2>", loadXsd());
        verify(softAssert).recordFailedAssertion(any(SAXException.class));
    }

    @Test
    void shouldSaveTransformedXmlToVariable()
    {
        String variableName = "variableName";
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);
        xmlValidationSteps.saveTransformedXml(XML, loadResource("transformation.xsl"), scopes, variableName);
        verify(variableContext).putVariable(scopes, variableName, "<test>xslt test</test>");
    }

    @Test
    void shouldRecordFailedAssertionOnTransformationException()
    {
        xmlValidationSteps.saveTransformedXml(XML, "invalid", null, null);
        verify(softAssert).recordFailedAssertion(any(TransformerException.class));
    }

    @Test
    void shouldRecordFailedAssertionIfXmlIsNotWellFormed()
    {
        xmlValidationSteps.validateXmlIsWellFormed("<test>xslt test<test>");
        verify(softAssert).recordFailedAssertion(any(SAXParseException.class));
    }

    @Test
    void shouldPassIfXMLDocumentIsWellFormed()
    {
        xmlValidationSteps.validateXmlIsWellFormed(XML);
        verify(softAssert).recordPassedAssertion("The XML document is well formed");
    }

    private String loadXsd()
    {
        return loadResource("test.xsd");
    }

    private String loadResource(String resourceName)
    {
        return ResourceUtils.loadResource(getClass(), resourceName);
    }
}

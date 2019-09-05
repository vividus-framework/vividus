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

package org.vividus.bdd.steps.xml;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Set;

import javax.xml.transform.TransformerException;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@ExtendWith(MockitoExtension.class)
class XmlStepsTests
{
    private static final String XML = "<test>data</test>";
    private static final String XPATH = "//test/text()";

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private XmlSteps xmlValidationSteps;

    @Test
    void shouldSaveDataByXpathIntoScopeVariable()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);
        String name = "name";
        xmlValidationSteps.saveDataByXpath(XPATH, XML, scopes, name);
        verify(bddVariableContext).putVariable(scopes, name, "data");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldValidateXmlElementExistenceByXpath()
    {
        Document doc = XmlUtils.convertToDocument(XML);
        softAssert.assertThat(eq("XML has element with XPath: " + XPATH), eq(doc), any(Matcher.class));
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
        verify(bddVariableContext).putVariable(scopes, variableName, "<test>xslt test</test>");
    }

    @Test
    void shouldRecordFailedAssertionOnTransformationException()
    {
        xmlValidationSteps.saveTransformedXml(XML, "invald", null, null);
        verify(softAssert).recordFailedAssertion(any(TransformerException.class));
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

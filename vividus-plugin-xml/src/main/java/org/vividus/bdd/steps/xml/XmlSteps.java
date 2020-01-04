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

package org.vividus.bdd.steps.xml;

import static org.hamcrest.xml.HasXPath.hasXPath;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

public class XmlSteps
{
    @Inject private ISoftAssert softAssert;
    @Inject private IBddVariableContext bddVariableContext;

    /**
     * Save XML data by XPath to the variable
     * @param xpath XPath locator
     * @param xml XML
     * @param scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName Name of variable
     */
    @When("I save data found by xpath `$xpath` in XML `$xml` to $scopes variable `$variableName`")
    public void saveDataByXpath(String xpath, String xml, Set<VariableScope> scopes, String variableName)
    {
        XmlUtils.getXmlByXpath(xml, xpath).ifPresent(
            data -> bddVariableContext.putVariable(scopes, variableName, data));
    }

    /**
     * Performs transformation of XML using XSLT and save the result to variable
     * @param xml XML
     * @param xslt XSLT
     * @param scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName Name of variable
     */
    @When("I transform XML `$xml` with `$xslt` and save result to $scopes variable `$variableName`")
    public void saveTransformedXml(String xml, String xslt, Set<VariableScope> scopes, String variableName)
    {
        XmlUtils.transform(xml, xslt,
            transformedXml -> bddVariableContext.putVariable(scopes, variableName, transformedXml),
            softAssert::recordFailedAssertion);
    }

    /**
     * Checks if xml contains element by XPath
     * @param xml XML
     * @param xpath XPath
     */
    @Then("XML `$xml` contains element by xpath `$xpath`")
    public void doesElementExistByXpath(String xml, String xpath)
    {
        Document doc = XmlUtils.convertToDocument(xml);
        softAssert.assertThat("XML has element with XPath: " + xpath, doc, hasXPath(xpath));
    }

    /**
     * Checks if XML is equal to expected XML
     * @param xml XML
     * @param expectedXml Expected XML
     */
    @Then("XML `$xml` is equal to `$expectedXml`")
    public void compareXmls(String xml, String expectedXml)
    {
        Diff diff = DiffBuilder.compare(expectedXml).withTest(xml)
                .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byNameAndAllAttributes))
                .ignoreWhitespace()
                .checkForSimilar()
                .build();

        Iterable<?> allDifferences = diff.getDifferences();
        if (allDifferences.iterator().hasNext())
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object difference : allDifferences)
            {
                stringBuilder.append(difference).append(System.lineSeparator());
            }
            softAssert.recordFailedAssertion(stringBuilder.toString());
        }
    }

    /**
     * Validates xml against XSD
     * @param xml XML
     * @param xsd XSD
     */
    @Then("XML `$xml` is valid against XSD `$xsd`")
    public void validateXmlAgainstXsd(String xml, String xsd)
    {
        try
        {
            XmlUtils.validateXmlAgainstXsd(xml, xsd);
        }
        catch (SAXException | IOException e)
        {
            softAssert.recordFailedAssertion(e);
        }
    }
}

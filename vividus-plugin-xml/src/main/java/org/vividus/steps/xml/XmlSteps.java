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

import static org.hamcrest.xml.HasXPath.hasXPath;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.xml.XmlUtils;
import org.vividus.variable.VariableScope;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.ElementSelectors;

public class XmlSteps
{
    private final ISoftAssert softAssert;
    private final VariableContext variableContext;

    public XmlSteps(ISoftAssert softAssert, VariableContext variableContext)
    {
        this.softAssert = softAssert;
        this.variableContext = variableContext;
    }

    /**
     * Save XML data by XPath to the variable. The step fails if input XML document is not well formed.
     * @param xpath XPath locator
     * @param xml XML
     * @param scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName Name of variable
     * @throws XPathExpressionException If an XPath expression error has occurred
     */
    @When("I save data found by xpath `$xpath` in XML `$xml` to $scopes variable `$variableName`")
    public void saveDataByXpath(String xpath, String xml, Set<VariableScope> scopes, String variableName)
            throws XPathExpressionException
    {
        if (isXmlWellFormed(xml))
        {
            XmlUtils.getXmlByXpath(xml, xpath)
                    .ifPresent(data -> variableContext.putVariable(scopes, variableName, data));
        }
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
            transformedXml -> variableContext.putVariable(scopes, variableName, transformedXml),
            softAssert::recordFailedAssertion);
    }

    /**
     * Checks if xml contains element by XPath. The step fails if input XML document is not well formed.
     * @param xml XML
     * @param xpath XPath
     * @throws IOException If an I/O error has occurred
     * @throws SAXException If an XML processing error has occurred
     * @deprecated Use combination of the following steps:
     * "When I save number of elements found by xpath `$xpath` in XML `$xml` to $scopes variable `$variableName`",
     * "Then `$variable1` is $comparisonRule `$variable2`"
     */
    @Deprecated(since = "0.6.14", forRemoval = true)
    @Then("XML `$xml` contains element by xpath `$xpath`")
    public void doesElementExistByXpath(String xml, String xpath) throws SAXException, IOException
    {
        getDocument(xml)
                .ifPresent(doc -> softAssert.assertThat("XML has element with XPath: " + xpath, doc, hasXPath(xpath)));
    }

    /**
     * Save number of elements by xpath from XML to a variable. The step fails if input XML document is not well formed.
     * @param xml XML
     * @param xpath XPath locator
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName Name of variable
     * @throws XPathExpressionException If an XPath expression error has occurred
     */
    @When("I save number of elements found by xpath `$xpath` in XML `$xml` to $scopes variable `$variableName`")
    public void saveNumberOfElements(String xpath, String xml, Set<VariableScope> scopes, String variableName)
            throws XPathExpressionException
    {
        if (isXmlWellFormed(xml))
        {
            int numberOfElements = XmlUtils.getNumberOfElements(xml, xpath);
            variableContext.putVariable(scopes, variableName, numberOfElements);
        }
    }

    /**
     * Checks if XML is equal to expected XML. The step fails if any of the input XML documents is not well formed.
     * @param xml XML
     * @param expectedXml Expected XML
     */
    @Then("XML `$xml` is equal to `$expectedXml`")
    public void compareXmls(String xml, String expectedXml)
    {
        if (!isXmlWellFormed(xml) || !isXmlWellFormed(expectedXml))
        {
            return;
        }

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

    /**
     * Validates if the XML document is well formed
     *
     * @param xml The XML document
     */
    @Then("XML `$xml` is well formed")
    public void validateXmlIsWellFormed(String xml)
    {
        if (isXmlWellFormed(xml))
        {
            softAssert.recordPassedAssertion("The XML document is well formed");
        }
    }

    private boolean isXmlWellFormed(String xml)
    {
        return getDocument(xml).isPresent();
    }

    private Optional<Document> getDocument(String xml)
    {
        try
        {
            return Optional.of(XmlUtils.convertToDocument(xml));
        }
        catch (SAXException | IOException e)
        {
            softAssert.recordFailedAssertion(e);
            return Optional.empty();
        }
    }
}

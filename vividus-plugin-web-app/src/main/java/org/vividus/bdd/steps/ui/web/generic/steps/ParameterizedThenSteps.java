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

package org.vividus.bdd.steps.ui.web.generic.steps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.web.CheckboxSteps;
import org.vividus.bdd.steps.ui.web.ElementPattern;
import org.vividus.bdd.steps.ui.web.parameters.RenamedParameters;
import org.vividus.bdd.steps.ui.web.util.FormatUtil;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.util.LocatorUtil;

public class ParameterizedThenSteps
{
    private static final String NAME_OR_ATTRIBUTE_TYPE_EXPECTED = "either name or both attributeType with "
            + "attributeValue should be specified";
    private static final String NAME = "name";
    private static final String ATTRIBUTE_NAME = "attributeName";
    private static final String ATTRIBUTE_VALUE = "attributeValue";
    private static final String STATE = "state";
    private static final String IMAGE_SRC = "imageSrc";
    private static final String BUTTONS = "buttons";
    private static final String EMPTY_BUTTON_NAME = "";
    private static final String TOOLTIP = "tooltip";
    private static final String TEXT = "text";
    private static final String URL = "URL";
    private static final String URL_PART = "urlPart";
    private static final String TEXT_PART = "textPart";
    private static final String ELEMENTS = "elements";
    private static final String RELATIVE_WIDTH = "relativeWidth";

    private IWebUiContext webUiContext;
    private ISearchActions searchActions;
    private IDescriptiveSoftAssert descriptiveSoftAssert;
    private IParameterizedValidations parameterizedValidations;
    private IParameterizedChecks parameterizedChecks;
    private IParameterizedSearchActions parameterizedSearchActions;
    private CheckboxSteps checkboxSteps;

    /**
     * Checks that a <b>number of radio buttons</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * A <b>radio button</b> is an <i><code>&lt;input&gt;</code></i> element with an attribute
     * 'type' = 'radio' and a <b>name</b> for it is a 'text' or any 'attribute value' of
     * it's <i><code>&lt;label&gt;</code></i> element (<i><code>&lt;label&gt;</code></i>
     * with an attribute 'for' = radio button id).
     * </p>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Finds all <b>radio buttons</b> specified by the <b>name</b> parameter;
     * <li>If state parameter is specified, compares an actual radio buttons' 'state' with expected;
     * <li>Compares the number of found buttons with an expected according to the comparison rule.
     * </ul>
     * @param comparisonRule The rule to compare values<br>
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number A number of radio buttons to compare
     * @param parameters A table containing expected parameters
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>name</b></td>
     * <td>
     * <b>state</b></td>
     * </tr>
     * </thead>
     * </table>
     * Where:
     * <ul>
     * <li>name - A name of the <b>radio button</b> (it's label)
     * <li>state - A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * <br>
     * </ul>
     * <b>Example:</b>
     * <pre>
     * &lt;div&gt;
     *  &lt;label&gt;<b>radioGroup</b>&lt;/label&gt;
     *  &lt;div&gt;
     *   &lt;input id="radioButtonId" type="radio"&gt;
     *   &lt;label for="radioButtonId"&gt;<b>ame</b>&lt;/label&gt;
     *  &lt;/div&gt;
     * &lt;/div&gt;
     * </pre>
     */
    @Then("the number of radio buttons with parameters is $comparisonRule '$number': $parameters")
    public void isRadioButtonWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        List<WebElement> foundRadioButtons = new ArrayList<>();
        Parameters row = parameters.getRowsAsParameters(true).get(0);
        String name = row.valueAs(NAME, String.class, null);
        if (name != null)
        {
            List<WebElement> labelElements = searchActions.findElements(getSearchContext(),
                    LocatorUtil.getXPathLocator(ElementPattern.LABEL_PATTERN, name));
            for (WebElement labelElement : labelElements)
            {
                String labelForAtr = labelElement.getAttribute("for");
                SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH,
                        LocatorUtil.getXPath(ElementPattern.RADIO_OPTION_INPUT_PATTERN, labelForAtr))
                                .addFilter(ActionAttributeType.STATE, row.valueAs(STATE, String.class, null));
                foundRadioButtons = searchActions.findElements(getSearchContext(), attributes);
            }
        }
        parameterizedValidations.assertNumber("radio buttons", comparisonRule, number, foundRadioButtons);
    }

    /**
     * Checks that a <b>number of elements</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * The search of elements can be performed with the following restrictions:
     * </p>
     * <ul>
     * <li><b>XPath locator</b> and <b>CSS selector</b> can NOT be used at the same time.</li>
     * <li><b>XPath locator</b> and <b>tag name</b> can NOT be used at the same time.</li>
     * <li><b>CSS selector</b> and <b>tag name</b> can NOT be Used at the same time.</li>
     * </ul>
     * <i>This parameters are competing since there is no exact priority between them: there is no rule which of them
     * is more weighty argument for search. If competing parameters are specified an exception is
     * thrown (UnsupportedOperationException).</i>
     * <ul>
     * <li>Search by <b>Attribute</b> assume specified both attribute <b>name</b> and <b>value</b>. Only attribute name
     * can be specified, but
     * such search is considered too general. <i>The attribute value can not be specified without attribute name:
     * an exception is thrown (UnsupportedOperationException).</i>
     * <p>
     * It's possible to specify several attributes names and attribute values.
     * Additional attributes are sent starting with <b>attributeName</b><i>X</i>, <b>attributeValue</b><i>X</i>
     * (you can set any specific ending).
     * </p></li>
     * </ul>
     * At least one of search-parameters (xpath, css_selector, tag_name, attribute(name and value)) should be specified.
     * All
     * the other parameters can be either specified or not.
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number to compare with
     * @param parameters
     * Table of parameters where:
     * <ul>
     * <li><b>xpath</b> - an XPath locator of an elements</li>
     * <li><b>cssSelector</b> - a CSS selector of an elements</li>
     * <li><b>tagName</b> - type of html tag (for ex. <i>&lt;div&gt;, &lt;img&gt;, &lt;span&gt;</i>)</li>
     * <li><b>attributeName</b> - the name of the attribute (for ex. 'name', 'id')</li>
     * <li><b>attributeValue</b> - the value of the attribute</li>
     * <li><b>visibility</b> - shows if only displayed elements will be found (
     * <i>Possible values:<b> true, false</b></i>).
     * True if not specified</li>
     * <li><b>state</b> - a state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)</li>
     * <li><b>cssProperty</b> - the name of the <i>CSS property</i></li>
     * <li><b>cssValue</b> - the expected value of <i>CSS property</i></li>
     * <li><b>cssValuePart</b> - the expected value part of <i>CSS property</i></li>
     * <li><b>absWidth</b> - an expected width of the element in a percentage (relatively to &lt;body&gt; element)</li>
     * <li><b>relativeWidth</b> - an expected width of the element in a percentage (relatively to parent element)</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>xpath</b></td>
     * <td>
     * <b>attributeName</b></td>
     * <td>
     * <b>attributeValue</b></td>
     * <td>
     * <b>visibility</b></td>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>absWidth</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>//body</td>
     * <td>class</td>
     * <td>Some class value</td>
     * <td>true</td>
     * <td>ENABLED</td>
     * <td>100</td>
     * </tr>
     * </tbody>
     * </table>
    */
    @Then("the number of elements with parameters is $comparisonRule '$number': $parameters")
    public void isElementWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        Parameters namedParameters = new RenamedParameters(parameters.getRowsAsParameters(true).get(0))
                .updateParameterName(RELATIVE_WIDTH, FormatUtil
                        .changeUpperUnderscoreToCamel(ActionAttributeType.RELATIVE_TO_PARENT_WIDTH.toString()));
        SearchInputData searchInputData = new SearchInputData(namedParameters,
                ActionAttributeType.XPATH, ActionAttributeType.CSS_SELECTOR, ActionAttributeType.TAG_NAME,
                ActionAttributeType.STATE, ActionAttributeType.RELATIVE_TO_PARENT_WIDTH);
        List<WebElement> elements = parameterizedSearchActions.findElements(getSearchContext(), searchInputData);
        elements = checkAttribute(elements, searchInputData, comparisonRule, number);
        if (!elements.isEmpty())
        {
            List<WebElement> foundElements = parameterizedSearchActions.filterElementsWithCssProperties(searchInputData,
                    elements);
            parameterizedValidations.assertNumberWithLocationReporting(ELEMENTS, comparisonRule, number, foundElements);
        }
    }

    /**
     * Checks that a <b>number of fields</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * <b>Field</b> is a &lt;input&gt; or &lt;textarea&gt; tags in the table (or a &lt;body&gt; tag if you work with
     * CKE editor - a field to enter and edit text, that is contained in a &lt;frame&gt; as a separate
     * html-document)
     * </p>
     * At least 'name' parameter should be specified since main search is performed by this one. Otherwise an exception
     * is thrown (UnsupportedOperationException). All the other parameters can be either specified or not
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number to compare with
     * @param parameters
     * Table of parameters where:
     * <ul>
     * <li><b>fieldName</b> - any attribute or text value of the field</li>
     * <li><b>text</b> - expected text of the field</li>
     * <li><b>textPart</b> - expected text part of the field</li>
     * <li><b>placeholder</b> - expected text of the &lt;placeholder&gt; attribute of the field</li>
     * <li><b>validationIconSource</b> - the value of 'background-image' CSS property of the field</li>
     * <li><b>state</b> - a state value of the field
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>fieldName</b></td>
     * <td>
     * <b>text</b></td>
     * <td>
     * <b>textPart</b></td>
     * <td>
     * <b>validationIconSource</b></td>
     * <td>
     * <b>placeholder</b></td>
     * <td>
     * <b>state</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>search-form</td>
     * <td>Some text value</td>
     * <td>text</td>
     * <td>none</td>
     * <td>Search...</td>
     * <td>ENABLED</td>
     * </tr>
     * </tbody>
     * </table>
    */
    @Then("the number of fields with parameters is $comparisonRule '$number': $parameters")
    public void isFieldsWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put(TEXT, FormatUtil.changeUpperUnderscoreToCamel(ActionAttributeType.FIELD_TEXT.toString()));
        replacementMap.put(TEXT_PART,
                FormatUtil.changeUpperUnderscoreToCamel(ActionAttributeType.FIELD_TEXT_PART.toString()));
        Parameters row = parameters.getRowsAsParameters(true).get(0);
        Parameters renamedPararameters = new RenamedParameters(row).updateParameterNames(replacementMap);
        ActionAttributeType[] attributeTypes = { ActionAttributeType.FIELD_NAME, ActionAttributeType.STATE,
                ActionAttributeType.FIELD_TEXT_PART, ActionAttributeType.PLACEHOLDER,
                ActionAttributeType.VALIDATION_ICON_SOURCE, ActionAttributeType.FIELD_TEXT };
        SearchInputData searchInputData = new SearchInputData(renamedPararameters, attributeTypes);
        List<WebElement> foundFields = parameterizedSearchActions.findElements(getSearchContext(), searchInputData);
        parameterizedValidations.assertNumber("fields", comparisonRule, number, foundFields);
    }

    /**
     * Checks that a <b>number of buttons</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * This is a replacement of several old steps:
     * <ul>
     * <li>Then a [$state] button with the name '$buttonName' exists</li>
     * <li>Then a button with image with the src '$imageSrc' exists</li>
     * <li>Then a button with the name '$buttonName' does not exist</li>
     * <li>Then a button with the name '$buttonName' exists</li>
     * <li>Then a button with the tooltip '$tooltip' and image with the src '$imageSrc' exists</li>
     * </ul>
     * <p>
     * The following rules should be followed to use this step properly:
     * </p>
     * <ul>
     * <li>Either name, imageSrc or both should be specified. Otherwise UnsupportedOperationException will be
     * thrown (step will be broken)</li>
     * <li>Other parameters can be specified or not.</li>
     * </ul>
     * @param comparisonRule Rule to be applied to the specified <b>number</b>
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param comparisonNumber Expected number of buttons which will be compared using <b>comparisonRule</b> with the
     * actual number
     * @param parameters A table with search parameters:
     * <ul>
     * <li><b>state</b> A state value of the button
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)</li>
     * <li><b>name</b> Value of any attribute (name, title etc.) of the <i>button</i> tag</li>
     * <li><b>imageSrc</b><i>'src'</i> attribute value of the image element inside button tag:
     * button[./img[@src='%s']]</li>
     * <li><b>tooltip</b><i>'title'</i> attribute value of the image</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:30%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>name</b></td>
     * <td>
     * <b>imageSrc</b></td>
     * <td>
     * <b>tooltip</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>ENABLED</td>
     * <td>janrain-social</td>
     * <td></td>
     * <td></td>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * <td>/imageSrc</td>
     * <td>Tooltip text</td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Then("the number of buttons with parameters is $comparisonRule $comparisonNumber: $parameters")
    public void isButtonWithParametersFound(ComparisonRule comparisonRule, int comparisonNumber,
            ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        Parameters row = parameters.getRowsAsParameters(true).get(0);
        String name = row.valueAs(NAME, String.class, null);
        String imageSrc = row.valueAs(IMAGE_SRC, String.class, null);
        if (name != null || imageSrc != null)
        {
            name = name == null ? EMPTY_BUTTON_NAME : name;
            SearchAttributes attributes = new SearchAttributes(ActionAttributeType.BUTTON_NAME, name);
            attributes.addFilter(ActionAttributeType.STATE, row.valueAs(STATE, String.class, null))
                    .addFilter(ActionAttributeType.TOOLTIP, row.valueAs(TOOLTIP, String.class, null));
            if (imageSrc != null)
            {
                attributes.addChildSearchAttributes(new SearchAttributes(ActionAttributeType.IMAGE_SRC,
                        new SearchParameters(imageSrc)));
            }
            List<WebElement> foundButtons = searchActions.findElements(getSearchContext(), attributes);
            parameterizedValidations.assertNumber(BUTTONS, comparisonRule, comparisonNumber, foundButtons);
        }
        else
        {
            descriptiveSoftAssert.recordFailedAssertion("Both name and imageSrc were not specified");
        }
    }

    /**
     * Checks that a <b>number of frames</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li>Finds all <b>frames</b> specified by the
     * mandatory <b>attribute</b> parameter (composed of attribute name and attribute value);</li>
     * <li>If state parameter is specified, compares an actual frame 'state' with expected;</li>
     * <li>Compares the number of found frames with an expected according to the comparison rule.</li>
     * </ul>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number A number of frames to compare
     * @param parameters A table containing expected parameters
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>attributeName</b></td>
     * <td>
     * <b>attributeValue</b></td>
     * </tr>
     * </table>
     * Where:
     * <ul>
     * <li>attributeName An attribute name of the element (mandatory parameter)</li>
     * <li>attributeValue An attribute value of the element (mandatory parameter)</li>
     * <li>state A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)</li>
     * </ul>
     */
    @Then("the number of frames with parameters is $comparisonRule '$number': $parameters")
    public void isFrameWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        Parameters row = parameters.getRowsAsParameters(true).get(0);
        String attributeName = row.valueAs(ATTRIBUTE_NAME, String.class, null);
        String attributeValue = row.valueAs(ATTRIBUTE_VALUE, String.class, null);
        if (attributeName != null && attributeValue != null)
        {
            SearchAttributes attributes = ElementPattern.getFrameSearchAttributes(attributeName, attributeValue)
                    .addFilter(ActionAttributeType.STATE, row.valueAs(STATE, String.class, null));
            List<WebElement> foundFrames = searchActions.findElements(getSearchContext(), attributes);
            parameterizedValidations.assertNumber("frames", comparisonRule, number, foundFrames);
        }
        else
        {
            descriptiveSoftAssert.recordFailedAssertion(
                    String.format("Invalid parameters: '%1$s' attributeName and '%2$s' attributeValue", attributeName,
                            attributeValue));
        }
    }

    /**
     * Checks that a <b>number of scrollbars</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number of scrollbars to compare
     */
    @Then("the number of scrollbars is $comparisonRule '$number'")
    public void isScrollbarFound(ComparisonRule comparisonRule, int number)
    {
        SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH,
                LocatorUtil.getXPath("div[@*[contains(.,'scrollbar')]]"));
        List<WebElement> scrollbars = searchActions.findElements(getSearchContext(), attributes);
        parameterizedValidations.assertNumber("scrollbars", comparisonRule, number, scrollbars);
    }

    /**
     * Checks that a <b>number of drop downs</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * <b>Drop down</b> is a drop down list with the specified options.
     * It is created by the <i>&lt;select&gt;</i> tag. The <i>&lt;option&gt;</i> tags inside the <i>
     * &lt;select&gt;</i> define the available options in the list
     * </p>
     * Example:
     * <pre>
     * &lt;select&gt;
     *  &lt;option&gt;visible text 1&lt;/option&gt;
     *  &lt;option&gt;visible text 2&lt;/option&gt;
     *  &lt;option&gt;visible text 3&lt;/option&gt;
     * &lt;/select&gt;
     * </pre>
     * At least 'name' parameter should be specified since main search is performed by this one. All the other
     * parameters can be either specified or not
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number to compare with
     * @param expectedParameters
     * Table of parameters where:
     * <ul>
     * <li><b>dropDownName</b> - any attribute or text value of the drop down</li>
     * <li><b>selectedText<i>N</i></b> - expected text that is selected on the drop down<br>
     * <b><i>N</i></b> - is any number that can be used (or not) to specify
     * multiple selected text values in a drop down.</li>
     * <li><b>state</b> - a state value of the drop down
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, VISIBLE, NOT_VISIBLE, SINGLE_SELECT,
     * MULTI_SELECT</b>)</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>dropDownName</b></td>
     * <td>
     * <b>selectedText1</b></td>
     * <td>
     * <b>selectedText2</b></td>
     * <td>
     * <b>state</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>value</td>
     * <td>visible text 1</td>
     * <td>visible text 2</td>
     * <td>MULTI_SELECT</td>
     * </tr>
     * </tbody>
     * </table>
     * @see <a href="https://www.w3schools.com/tags/tag_select.asp">
     * <i>HTML &lt;select&gt; Tag</i></a>
     */
    @Then("the number of drop downs with parameters is $comparisonRule '$number': $expectedParameters")
    public void isDropDownWithParametrsFound(ComparisonRule comparisonRule, int number,
            ExamplesTable expectedParameters)
    {
        if (parameterizedChecks.checkIfParametersAreSet(expectedParameters))
        {
            Parameters row = expectedParameters.getRowsAsParameters(true).get(0);
            String name = row.valueAs("dropDownName", String.class, null);
            if (name != null)
            {
                String xpath = LocatorUtil.getXPath(ElementPattern.SELECT_PATTERN, name);
                SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, xpath)
                        .addFilter(ActionAttributeType.DROP_DOWN_STATE, row.valueAs(STATE, String.class, null));
                setDropDownTextFilter(row, attributes);
                List<WebElement> foundDropDowns = searchActions.findElements(getSearchContext(), attributes);
                parameterizedValidations.assertNumber("drop downs", comparisonRule, number, foundDropDowns);
            }
            else
            {
                descriptiveSoftAssert.recordFailedAssertion("No drop down name was specified");
            }
        }
    }

    private void setDropDownTextFilter(Parameters row, SearchAttributes attributes)
    {
        for (Entry<String, String> entry : row.values().entrySet())
        {
            String parameterName = entry.getKey();
            if (parameterName.startsWith("selectedText"))
            {
                attributes.addFilter(ActionAttributeType.DROP_DOWN_TEXT, entry.getValue());
            }
        }
    }

    /**
     * Checks that a <b>number of images</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * An <b>image</b> is an <i><code>&lt;img&gt;</code></i> element with an attribute 'src'
     * </p>
     * <p>
     * At least 'imageSrc', 'imageSrcPart' or 'tooltip' parameter should be specified since main search is performed by
     * this one. Otherwise an exception is thrown (UnsupportedOperationException). All the other parameters can be
     * either specified or not.<br>
     * <i><b>'imageSrc' and 'imageSrcPart' are competing attributes</b>.
     * And If 'imageSrc' and 'imageSrcPart' parameters are given together, an exception is thrown.
     * (UnsupportedOperationException)</i>.
     * </p>
     * @param comparisonRule The rule to compare values<br>
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number A number of images to compare
     * @param parameters A table containing expected parameters
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>imageSrc</b></td>
     * <td>
     * <b>imageSrcPart</b></td>
     * <td>
     * <b>tooltip</b></td>
     * <td>
     * <b>state</b></td>
     * </tr>
     * <tr>
     * <td>value</td>
     * <td>value</td>
     * <td>value</td>
     * <td>ENABLED</td>
     * </tr>
     * </thead>
     * </table>
     * Where:
     * <ul>
     * <li><b>imageSrc</b> - A value of the <b>src</b> attribute
     * <li><b>imageSrcPart</b> - A part of a value of the <b>src</b> attribute
     * <li><b>tooltip</b> - A value of the <b>title</b> attribute
     * <li><b>state</b> A state value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * <br>
     * </ul>
     * <b>Example:</b>
     * <pre>
     * &lt;img src="imageSrc" title="imageTitle"&gt;
     * </pre>
     */
    @Then("the number of images with parameters is $comparisonRule '$number': $parameters")
    public void isImageWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (!parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            return;
        }
        Parameters row = parameters.getRowsAsParameters(true).get(0);
        ActionAttributeType[] attributeTypes = { ActionAttributeType.IMAGE_SRC, ActionAttributeType.IMAGE_SRC_PART,
                ActionAttributeType.TOOLTIP, ActionAttributeType.STATE };
        SearchInputData searchInputData = new SearchInputData(row, attributeTypes);
        List<WebElement> foundImages = parameterizedSearchActions.findElements(getSearchContext(), searchInputData);
        parameterizedValidations.assertNumber("images", comparisonRule, number, foundImages);
    }

    /**
     * Checks that a <b>number of javascript elements</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * <p>
     * JS elements are placed under <i>&lt;script&gt;</i> tag having attribute <i>type</i> = "text/javascript".
     * </p>
     * <p>
     * All the parameters are competing: only one of them can be set at one step usage. Otherwise an exception
     * is thrown (UnsupportedOperationException).
     * </p>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number of scripts to compare
     * @param parameters
     * Table of parameters where:
     * <ul>
     * <li><b>srcPart</b> - Value or a part of the <i>src</i> attribute</li>
     * <li><b>text</b> - Text content of the <i>&lt;script&gt;</i> tag</li>
     * <li><b>textPart</b> - Partial text content of the <i>&lt;script&gt;</i> tag</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:30%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>srcPart</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>https://someSrc.js</td>
     * </tr>
     * </tbody>
     * </table>
     * or
     * <table border="1" style="width:30%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>text</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>{"Some text" : "text"}</td>
     * </tr>
     * </tbody>
     * </table>
     * or
     * <table border="1" style="width:30%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>textPart</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>Some text</td>
     * </tr>
     * </tbody>
     * </table>
    */
    @Then("the number of script elements with parameters is $comparisonRule '$number': $parameters")
    public void isJSFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            Parameters row = parameters.getRowsAsParameters(true).get(0);
            List<WebElement> scripts = List.of();
            for (JSParameterPattern pattern : JSParameterPattern.values())
            {
                String parameterValue = row.valueAs(FormatUtil.changeUpperUnderscoreToCamel(pattern.toString()),
                        String.class, null);
                if (parameterValue != null)
                {
                    String xPath = LocatorUtil.getXPath(pattern.getPattern(), parameterValue);
                    SearchParameters searchParameters = new SearchParameters(xPath).setVisibility(Visibility.ALL);
                    SearchAttributes attributes = new SearchAttributes(ActionAttributeType.XPATH, searchParameters);
                    scripts = searchActions.findElements(getSearchContext(), attributes);
                    break;
                }
            }
            parameterizedValidations.assertNumber("scripts", comparisonRule, number, scripts);
        }
    }

    /**
     * Checks that a <b>number of checkboxes</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * This is a replacement of several old steps:
     * <ul>
     * <li>Then a [$state] checkbox with the attribute '$attributeType'='$attributeValue' exists</li>
     * <li>Then a [$state] checkbox with the name '$checkboxName' exists</li>
     * <li>Then a checkbox with the name '$checkBox' does not exist</li>
     * <li>Then a checkbox with the name '$checkboxName' exists</li>
     * </ul>
     * <p>
     * The following rules should be followed to use this step properly:
     * </p>
     * <ul>
     * <li>Either name or both attributeType with attributeValue should be specified. Otherwise the step will fail with
     * the corresponding message</li>
     * <li>Other parameters can be specified or not.</li>
     * </ul>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number to compare with
     * @param parameters A table with parameters where:
     * <ul>
     * <li><b>state</b> A state value of the button
     * (<i>Possible values: <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b></i>)</li>
     * </ul>
     * <b>Example:</b>
     * <table border="1" style="width:30%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>name</b></td>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>attributeType</b></td>
     * <td>
     * <b>attributeValue</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>c_name</td>
     * <td>enabled</td>
     * <td></td>
     * <td></td>
     * </tr>
     * <tr>
     * <td></td>
     * <td></td>
     * <td>name</td>
     * <td>anonymous user: Administer</td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Then("the number of checkboxes with parameters is $comparisonRule '$number': $parameters")
    public void isCheckboxWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            Parameters row = parameters.getRowAsParameters(0, true);
            String name = row.valueAs(NAME, String.class, null);
            String attributeType = row.valueAs("attributeType", String.class, null);
            String attributeValue = row.valueAs(ATTRIBUTE_VALUE, String.class, null);
            SearchAttributes attributes;
            if (name != null)
            {
                if (attributeType != null || attributeValue != null)
                {
                    descriptiveSoftAssert.recordFailedAssertion(
                            "Competing search parameters provided: " + NAME_OR_ATTRIBUTE_TYPE_EXPECTED);
                    return;
                }
                attributes = new SearchAttributes(ActionAttributeType.CHECKBOX_NAME, name);
            }
            else if (attributeType != null && attributeValue != null)
            {
                String xpath = checkboxSteps.getCheckboxXpathByAttributeAndValue(attributeType, attributeValue);
                attributes = new SearchAttributes(ActionAttributeType.XPATH, xpath);
            }
            else
            {
                descriptiveSoftAssert
                        .recordFailedAssertion("No search parameters provided: " + NAME_OR_ATTRIBUTE_TYPE_EXPECTED);
                return;
            }
            attributes.addFilter(ActionAttributeType.STATE, row.valueAs(STATE, String.class, null));
            List<WebElement> foundElements = searchActions.findElements(getSearchContext(), attributes);
            parameterizedValidations.assertNumber("checkbox", comparisonRule, number, foundElements);
        }
    }

    /**
     * Checks that a <b>number of links</b> specified by parameters exists within
     * the previously specified <b>search context</b>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param number The number to compare with
     * @param parameters A table containing expected parameters
     * <table border="1" style="width:70%">
     * <caption>A table of parameters</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>text</b></td>
     * <td>
     * <b>URL</b></td>
     * <td>
     * <b>tooltip</b></td>
     * <td>
     * <b>urlPart</b></td>
     * </tr>
     * </thead>
     * </table>
     * Where:
     * <ul>
     * <li><b>state</b> - a state value of the link (optional parameter, may be omitted)
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)</li>
     * <li><b>text</b> - an expected text of the link</li>
     * <li><b>URL</b> - the 'href' attribute value of the <b>link</b> (<i>&lt;a&gt;</i> tag).
     * Possible values:
     * <ul>
     * <li>An absolute URL - points to another web site (like href="http://www.example.com/absolute")</li>
     * <li>A relative URL - points to a file within a web site (like href="/relative")</li>
     * </ul>
     * </li>
     * <li><b>tooltip</b> - the 'title' attribute value of the link</li>
     * <li><b>urlPart</b> - a part of the link's 'href' attribute</li>
     * </ul>
     * <p>
     * <b>URL and urlPart parameters are competing, all other parameters may be used in any combination.</b>
     * </p>
     * <b>Examples:</b>
     * <table border="1" style="width:60%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>URL</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>VISIBLE</td>
     * <td>https://www.vividus.org/wiki</td>
     * </tr>
     * </tbody>
     * </table>
     * <table border="1" style="width:50%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>tooltip</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>ENABLED</td>
     * <td>Vividus Framework</td>
     * </tr>
     * </tbody>
     * </table>
     * <table border="1" style="width:25%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>text</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>Vividus</td>
     * </tr>
     * </tbody>
     * </table>
     * <table border="1" style="width:60%">
     * <caption>A table of examples</caption>
     * <thead>
     * <tr>
     * <td>
     * <b>state</b></td>
     * <td>
     * <b>text</b></td>
     * <td>
     * <b>tooltip</b></td>
     * <td>
     * <b>urlPart</b></td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>VISIBLE</td>
     * <td>Vividus</td>
     * <td>Vividus Framework</td>
     * <td>vividus</td>
     * </tr>
     * </tbody>
     * </table>
     */
    @Then("the number of links with parameters is $comparisonRule '$number': $parameters")
    public void isLinksWithParametersFound(ComparisonRule comparisonRule, int number, ExamplesTable parameters)
    {
        if (parameterizedChecks.checkIfParametersAreSet(parameters))
        {
            Map<String, String> replacementMap = new LinkedHashMap<>();
            replacementMap.put(TEXT, FormatUtil.changeUpperUnderscoreToCamel(ActionAttributeType.LINK_TEXT.toString()));
            replacementMap.put(URL, FormatUtil.changeUpperUnderscoreToCamel(ActionAttributeType.LINK_URL.toString()));
            replacementMap.put(URL_PART,
                    FormatUtil.changeUpperUnderscoreToCamel(ActionAttributeType.LINK_URL_PART.toString()));
            Parameters row = parameters.getRowsAsParameters(true).get(0);
            Parameters namedParameters = new RenamedParameters(row).updateParameterNames(replacementMap);
            ActionAttributeType[] attributeTypes = {ActionAttributeType.LINK_TEXT, ActionAttributeType.LINK_URL,
                    ActionAttributeType.TOOLTIP, ActionAttributeType.LINK_URL_PART, ActionAttributeType.STATE};
            SearchInputData searchInputData = new SearchInputData(namedParameters, attributeTypes);
            List<WebElement> foundLinks = parameterizedSearchActions.findElements(getSearchContext(), searchInputData);
            parameterizedValidations.assertNumber("links", comparisonRule, number, foundLinks);
        }
    }

    private enum JSParameterPattern
    {
        SRC_PART("script[contains(@src, %s)]"),
        TEXT("script[text()=%s]"),
        TEXT_PART("script[contains(text(),%s)]");

        private final String pattern;

        JSParameterPattern(String pattern)
        {
            this.pattern = pattern;
        }

        private String getPattern()
        {
            return pattern;
        }
    }

    private List<WebElement> checkAttribute(List<WebElement> elements, SearchInputData searchInputData,
            ComparisonRule comparisonRule, int number)
    {
        List<WebElement> elementsWithAttribute = parameterizedSearchActions
                .findElementsWithAttributes(getSearchContext(), searchInputData,
                elements);
        if (elementsWithAttribute.isEmpty())
        {
            parameterizedValidations.assertNumber(ELEMENTS, comparisonRule, number, elementsWithAttribute);
        }
        return elementsWithAttribute;
    }

    private SearchContext getSearchContext()
    {
        return webUiContext.getSearchContext();
    }

    public void setSearchActions(ISearchActions searchActions)
    {
        this.searchActions = searchActions;
    }

    public void setWebUiContext(IWebUiContext webUiContext)
    {
        this.webUiContext = webUiContext;
    }

    public void setDescriptiveSoftAssert(IDescriptiveSoftAssert descriptiveSoftAssert)
    {
        this.descriptiveSoftAssert = descriptiveSoftAssert;
    }

    public void setParameterizedValidations(IParameterizedValidations parameterizedValidations)
    {
        this.parameterizedValidations = parameterizedValidations;
    }

    public void setParameterizedChecks(IParameterizedChecks parameterizedChecks)
    {
        this.parameterizedChecks = parameterizedChecks;
    }

    public void setParameterizedSearchActions(IParameterizedSearchActions parameterizedSearchActions)
    {
        this.parameterizedSearchActions = parameterizedSearchActions;
    }

    public void setCheckboxSteps(CheckboxSteps checkboxSteps)
    {
        this.checkboxSteps = checkboxSteps;
    }
}

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

package org.vividus.yaml.steps;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class YamlStepsTests
{
    private static final String VARIABLE_NAME = "var";
    private static final Set<VariableScope> VARIABLE_SCOPES = Set.of(VariableScope.SCENARIO);

    private static final String YAML = """
            ---
            a: 1                       # an integer
            b: "12"                   # a string, disambiguated by quotes
            c: 123.0                   # a float
            d: !!float 1234            # also a float via explicit data type prefixed by (!!)
            e: !!str 12345             # a string, disambiguated by explicit type
            f: !!str Yes               # a string via explicit type
            g: Yes                     # a boolean True (yaml1.1), string "Yes" (yaml1.2)
            h: Yes we have No bananas  # a string, "Yes" and "No" disambiguated by context.receipt:\
                 Oz-Ware Purchase Invoice
            date:        2012-08-06
            customer:
                first_name:   Dorothy
                family_name:  Gale

            items:
                - part_no:   A4786
                  descrip:   Water Bucket (Filled)
                  price:     1.47
                  quantity:  4

                - part_no:   E1628
                  descrip:   High Heeled "Ruby" Slippers
                  size:      8
                  price:     133.7
                  quantity:  1

            bill-to:  &id001
                street: |
                        123 Tornado Alley
                        Suite 16
                city:   East Centerville
                state:  KS

            ship-to:  *id001

            specialDelivery:  >
                Follow the Yellow Brick
                Road to the Emerald City.
                Pay no attention to the
                man behind the curtain.

            comments:    ["A", "B"]

            extraAdditions:
                - cat
                - dog
            ...""";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private YamlSteps yamlSteps;

    static Stream<Arguments> yamlValues()
    {
        return Stream.of(
                arguments("a", "1"),
                arguments("b", "12"),
                arguments("c", "123.0"),
                arguments("d", "1234.0"),
                arguments("e", "12345"),
                arguments("f", "Yes"),
                arguments("g", "true"),
                arguments("h", "Yes we have No bananas"),
                arguments("customer.first_name", "Dorothy"),
                arguments("comments[0]", "A")
        );
    }

    @ParameterizedTest
    @MethodSource("yamlValues")
    void shouldSaveValueByYamlPath(String yamlPath, Object value)
    {
        yamlSteps.saveYamlValueToVariable(YAML, yamlPath, VARIABLE_SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPES, VARIABLE_NAME, value);
        verifyNoInteractions(softAssert);
    }

    @ParameterizedTest
    @CsvSource({
            "comments,       array",
            "extraAdditions, array",
            "items,          array",
            "customer,       object",
            "items[1],       object"
    })
    void shouldFailToSaveNonScalarValueByYamlPath(String yamlPath, String actualType)
    {
        yamlSteps.saveYamlValueToVariable(YAML, yamlPath, VARIABLE_SCOPES, VARIABLE_NAME);
        verify(softAssert).recordFailedAssertion("Value of YAML element found by YAML path '" + yamlPath
                + "' must be either null, or boolean, or string, or integer, or float, but found " + actualType);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldFailToSaveNonExistentElementValueByYamlPath()
    {
        yamlSteps.saveYamlValueToVariable(YAML, "non-existent", VARIABLE_SCOPES, VARIABLE_NAME);
        verify(softAssert).recordFailedAssertion("No YAML element is found by YAML path 'non-existent'");
        verifyNoInteractions(variableContext);
    }

    @ParameterizedTest
    @CsvSource({
            "items,          1",
            "items[0],       1",
            "non-existent,   0"
    })
    void shouldSaveNumberOfYamlElements(String yamlPath, int expectedCount)
    {
        yamlSteps.saveNumberOfYamlElements(YAML, yamlPath, VARIABLE_SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(VARIABLE_SCOPES, VARIABLE_NAME, expectedCount);
        verifyNoInteractions(softAssert);
    }
}

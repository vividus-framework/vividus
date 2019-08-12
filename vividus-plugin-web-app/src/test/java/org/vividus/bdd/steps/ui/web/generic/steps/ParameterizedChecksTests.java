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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.ui.web.validation.IDescriptiveSoftAssert;

@ExtendWith(MockitoExtension.class)
class ParameterizedChecksTests
{
    @Mock
    private IDescriptiveSoftAssert descriptiveSoftAssert;

    @InjectMocks
    private ParameterizedChecks parameterizedChecks;

    private ExamplesTable parameters;

    @Test
    void testCheckIfParametersAreSetTrue()
    {
        String table = "|name|state|\n |testName|VISIBLE|";
        parameters = new ExamplesTable(table);
        boolean isTrue = parameterizedChecks.checkIfParametersAreSet(parameters);
        assertTrue(isTrue);
    }

    @Test
    void testCheckIfParametersAreSetFalse()
    {
        String table = "|name|state|\n |testName||";
        parameters = new ExamplesTable(table);
        parameterizedChecks.checkIfParametersAreSet(parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion("Parameter 'state' is empty");
    }

    @Test
    void testCheckIfParametersAreSetNoParameters()
    {
        String table = "|name|state|";
        parameters = new ExamplesTable(table);
        parameterizedChecks.checkIfParametersAreSet(parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion("Parameters were not specified");
    }

    @Test
    void testCheckIfParametersAreSetExtraParameters()
    {
        String table = "|name|state|\n|x|y|\n|z|q|";
        parameters = new ExamplesTable(table);
        parameterizedChecks.checkIfParametersAreSet(parameters);
        verify(descriptiveSoftAssert).recordFailedAssertion("Excess string with parameters is found");
    }
}

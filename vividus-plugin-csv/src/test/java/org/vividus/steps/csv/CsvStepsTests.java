/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.csv;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.csv.CsvReader;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class CsvStepsTests
{
    @Mock
    private CsvReader csvReader;

    @Mock
    private VariableContext variableContext;

    @InjectMocks
    private CsvSteps csvSteps;

    @Test
    void saveCsvStringIntoVariable() throws IOException
    {
        String csvString = "csv";
        String variableName = "name";
        List<Map<String, String>> result = csvReader.readCsvString(csvString);
        csvSteps.saveCsvStringIntoVariable(csvString, Set.of(VariableScope.SCENARIO), variableName);
        verify(variableContext).putVariable(Set.of(VariableScope.SCENARIO), variableName, result);
    }
}

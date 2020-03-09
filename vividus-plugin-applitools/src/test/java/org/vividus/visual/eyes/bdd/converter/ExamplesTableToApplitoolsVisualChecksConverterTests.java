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

package org.vividus.visual.eyes.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.visual.eyes.factory.ApplitoolsVisualCheckFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.model.VisualActionType;

@ExtendWith(MockitoExtension.class)
class ExamplesTableToApplitoolsVisualChecksConverterTests
{
    private static final VisualActionType ESTABLISH = VisualActionType.ESTABLISH;
    private static final String BASELINE = "baseline";
    private static final String BATCH = "batch";
    private static final String EXAMPLES_TABLE = "|action|\n|establish|";

    @Mock private ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory;
    @Mock private ExamplesTableFactory examplesTableFactory;

    @InjectMocks
    private ExamplesTableToApplitoolsVisualChecksConverter converter;

    @BeforeEach
    void beforeEach()
    {
        converter.setExamplesTableFactory(() -> examplesTableFactory);
    }

    @Test
    void shouldConvertExamplesTableIntoApplitoolsVisualChecks()
    {
        ApplitoolsVisualCheck visualCheck =
                spy(new ApplitoolsVisualCheck(BATCH, BASELINE, ESTABLISH));
        List<ApplitoolsVisualCheck> checks = List.of(visualCheck, visualCheck);
        mockExamplesTable(checks);
        when(applitoolsVisualCheckFactory.unite(visualCheck)).thenReturn(visualCheck);
        assertEquals(checks, converter.convertValue(EXAMPLES_TABLE, null));
        verify(applitoolsVisualCheckFactory, times(2)).unite(visualCheck);
        verify(visualCheck, times(2)).buildIgnores();
    }

    private void mockExamplesTable(List<ApplitoolsVisualCheck> checks)
    {
        ExamplesTable examplesTable = mock(ExamplesTable.class);
        when(examplesTableFactory.createExamplesTable(EXAMPLES_TABLE)).thenReturn(examplesTable);
        when(examplesTable.getRowsAs(ApplitoolsVisualCheck.class)).thenReturn(checks);
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> dataProvider()
    {
        return Stream.of(
                Arguments.of(new ApplitoolsVisualCheck(null, BASELINE, ESTABLISH), "batchName"),
                Arguments.of(new ApplitoolsVisualCheck(BATCH, null, ESTABLISH), "baselineName"),
                Arguments.of(new ApplitoolsVisualCheck(BATCH, BATCH, null), "action"));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void shouldThrowExceptionIfMandatoryFieldNotSet(ApplitoolsVisualCheck visualCheck, String fieldName)
    {
        mockExamplesTable(List.of(visualCheck));
        IllegalArgumentException iae =
                assertThrows(IllegalArgumentException.class, () -> converter.convertValue(EXAMPLES_TABLE, null));
        assertEquals(fieldName + " should be set", iae.getMessage());
    }
}

/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.converter;

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

    @Mock private ApplitoolsVisualCheckFactory applitoolsVisualCheckFactory;

    @InjectMocks
    private ExamplesTableToApplitoolsVisualChecksConverter converter;

    @Test
    void shouldConvertExamplesTableIntoApplitoolsVisualChecks()
    {
        ApplitoolsVisualCheck visualCheck =
                spy(new ApplitoolsVisualCheck(BATCH, BASELINE, ESTABLISH));
        List<ApplitoolsVisualCheck> checks = List.of(visualCheck, visualCheck);
        ExamplesTable examplesTable = mockExamplesTable(checks);
        when(applitoolsVisualCheckFactory.unite(visualCheck)).thenReturn(visualCheck);
        assertEquals(checks, converter.convertValue(examplesTable, null));
        verify(applitoolsVisualCheckFactory, times(2)).unite(visualCheck);
    }

    private ExamplesTable mockExamplesTable(List<ApplitoolsVisualCheck> checks)
    {
        ExamplesTable examplesTable = mock(ExamplesTable.class);
        when(examplesTable.getRowsAs(ApplitoolsVisualCheck.class)).thenReturn(checks);
        return examplesTable;
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
        ExamplesTable examplesTable = mockExamplesTable(List.of(visualCheck));
        IllegalArgumentException iae =
                assertThrows(IllegalArgumentException.class, () -> converter.convertValue(examplesTable, null));
        assertEquals(fieldName + " should be set", iae.getMessage());
    }
}

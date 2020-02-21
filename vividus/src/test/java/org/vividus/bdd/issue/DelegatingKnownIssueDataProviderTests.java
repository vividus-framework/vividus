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

package org.vividus.bdd.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.softassert.issue.IKnownIssueDataProvider;

@ExtendWith(MockitoExtension.class)
class DelegatingKnownIssueDataProviderTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private DelegatingKnownIssueDataProvider provider;

    @Test
    void shouldReturnValueFromSingleDataProvider()
    {
        IKnownIssueDataProvider dataProvider = mock(IKnownIssueDataProvider.class);
        when(dataProvider.getData()).thenReturn(Optional.of(VALUE));
        provider.setKnownIssueDataProviders(Map.of(KEY, dataProvider));
        assertEquals(Optional.of(VALUE), provider.getData(KEY));
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void shouldReturnVariableValueWhenNoMatchedSingleDataProviders()
    {
        IKnownIssueDataProvider dataProvider = mock(IKnownIssueDataProvider.class);
        provider.setKnownIssueDataProviders(Map.of("key2", dataProvider));
        when(bddVariableContext.getVariable(KEY)).thenReturn(VALUE);
        assertEquals(Optional.of(VALUE), provider.getData(KEY));
    }

    @Test
    void shouldReturnVariableValueWhenNoDataProviders()
    {
        provider.setKnownIssueDataProviders(Map.of());
        when(bddVariableContext.getVariable(KEY)).thenReturn(VALUE);
        assertEquals(Optional.of(VALUE), provider.getData(KEY));
    }

    @Test
    void shouldReturnEmptyValueWhenNoDataProvidersAndNoVariableFound()
    {
        provider.setKnownIssueDataProviders(Map.of());
        assertEquals(Optional.empty(), provider.getData(KEY));
    }
}

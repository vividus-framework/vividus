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

package org.vividus.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;

@ExtendWith(MockitoExtension.class)
class KnownIssueVariableDataProviderTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock private VariableContext variableContext;
    @InjectMocks private KnownIssueVariableDataProvider provider;

    @Test
    void shouldReturnVariableValueWhenNoMatchedSingleDataProviders()
    {
        when(variableContext.getVariable(KEY)).thenReturn(VALUE);
        var actual = provider.getData(KEY);
        assertEquals(Optional.of(VALUE), actual);
    }
}

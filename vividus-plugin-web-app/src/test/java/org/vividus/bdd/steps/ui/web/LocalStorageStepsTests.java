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

package org.vividus.bdd.steps.ui.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.storage.ILocalStorageManager;

@ExtendWith(MockitoExtension.class)
class LocalStorageStepsTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    @Mock
    private IBddVariableContext bddVariableContext;
    @Mock
    private ILocalStorageManager localStorageManager;
    @Mock
    private ISoftAssert softAssert;
    @InjectMocks
    private LocalStorageSteps localStorageSteps;

    @Test
    void shouldSaveLocalStorageItemIntoScopeVariable()
    {
        when(localStorageManager.getItem(KEY)).thenReturn(VALUE);
        Set<VariableScope> scopes = Set.of(VariableScope.STEP);

        localStorageSteps.saveLocalStorageItemByKey(KEY, scopes, KEY);

        verify(bddVariableContext).putVariable(scopes, KEY, VALUE);
    }

    @Test
    void shouldVerifyThatLocalStorageItemDoesNotExists()
    {
        when(localStorageManager.getItem(KEY)).thenReturn(VALUE);

        localStorageSteps.checkLocalStorageItemDoesNotExist(KEY);

        verify(softAssert).assertThat(eq("Local storage item with key: key"), eq(VALUE),
                argThat(m -> "null".equals(m.toString())));
    }
}

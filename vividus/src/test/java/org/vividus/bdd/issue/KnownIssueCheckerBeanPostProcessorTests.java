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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.vividus.softassert.issue.IKnownIssueDataProvider;

@ExtendWith(MockitoExtension.class)
class KnownIssueCheckerBeanPostProcessorTests
{
    private static final String NAME = "name";

    private static final String ISSUE_CHECKER = "issueChecker";

    private static final String BEAN_NAME = "knownIssueChecker-Integration";

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private KnownIssueCheckerBeanPostProcessor knownIssueCheckerBeanPostProcessor;

    @Test
    void testPostProcessAfterInitializationNotKnownIssueChecker()
    {
        Object bean = Mockito.mock(Object.class);
        assertEquals(bean, knownIssueCheckerBeanPostProcessor.postProcessAfterInitialization(bean, "bean"));
    }

    @Test
    void testPostProcessAfterInitializationKnownIssueChecker()
    {
        DelegatingKnownIssueDataProvider dataProvider = Mockito.mock(DelegatingKnownIssueDataProvider.class);
        Map<String, IKnownIssueDataProvider> map = new HashMap<>();
        when(applicationContext.getBeanNamesForType(Map.class)).thenReturn(new String[] { NAME, BEAN_NAME });
        when(applicationContext.getBean(BEAN_NAME, Map.class)).thenReturn(map);
        assertEquals(dataProvider,
                knownIssueCheckerBeanPostProcessor.postProcessAfterInitialization(dataProvider, ISSUE_CHECKER));
        verify(dataProvider).setKnownIssueDataProviders(map);
    }

    @Test
    void testPostProcessAfterInitializationKnownIssueCheckerNoProviders()
    {
        DelegatingKnownIssueDataProvider dataProvider = Mockito.mock(DelegatingKnownIssueDataProvider.class);
        Map<String, IKnownIssueDataProvider> map = new HashMap<>();
        when(applicationContext.getBeanNamesForType(Map.class)).thenReturn(new String[] { NAME });
        assertEquals(dataProvider,
                knownIssueCheckerBeanPostProcessor.postProcessAfterInitialization(dataProvider, ISSUE_CHECKER));
        verify(dataProvider).setKnownIssueDataProviders(map);
    }
}

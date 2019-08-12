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

package org.vividus.bdd.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.vividus.softassert.issue.KnownIssueChecker;

@ExtendWith(MockitoExtension.class)
class KnownIssueCheckerBeanPostProcessorTests
{
    private static final String NAME = "name";

    private static final String ISSUE_CHECKER = "issueChecker";

    private static final String BEAN_NAME = "knownIssueChecker-Integration";

    @Mock
    private ApplicationContext applicationContetxt;

    @InjectMocks
    private KnownIssueCheckerBeanPostProcessor knownIssueCheckerBeanPostProvessor;

    @Test
    void testPostProcessAfterInitializationNotKnownIssueChecker()
    {
        Object bean = Mockito.mock(Object.class);
        assertEquals(bean, knownIssueCheckerBeanPostProvessor.postProcessAfterInitialization(bean, "bean"));
    }

    @Test
    void testPostProcessAfterInitializationKnownIssueChecker()
    {
        KnownIssueChecker issueChecker = Mockito.mock(KnownIssueChecker.class);
        Map<String, IKnownIssueDataProvider> map = new HashMap<>();
        Mockito.when(applicationContetxt.getBeanNamesForType(Map.class)).thenReturn(new String[] { NAME, BEAN_NAME });
        Mockito.when(applicationContetxt.getBean(BEAN_NAME, Map.class)).thenReturn(map);
        assertEquals(issueChecker,
                knownIssueCheckerBeanPostProvessor.postProcessAfterInitialization(issueChecker, ISSUE_CHECKER));
        Mockito.verify(issueChecker).setKnownIssueDataProviders(map);
    }

    @Test
    void testPostProcessAfterInitializationKnownIssueCheckerNoProviders()
    {
        KnownIssueChecker issueChecker = Mockito.mock(KnownIssueChecker.class);
        Map<String, IKnownIssueDataProvider> map = new HashMap<>();
        Mockito.when(applicationContetxt.getBeanNamesForType(Map.class)).thenReturn(new String[] { NAME });
        assertEquals(issueChecker,
                knownIssueCheckerBeanPostProvessor.postProcessAfterInitialization(issueChecker, ISSUE_CHECKER));
        Mockito.verify(issueChecker).setKnownIssueDataProviders(map);
    }
}

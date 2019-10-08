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

package org.vividus.reportportal.config.confition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.utils.properties.ListenerProperty;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class ReportPortalEnableConditionTests
{
    private static final String RP_ENABLE_PROPERTY = ListenerProperty.ENABLE.getPropertyName();

    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    void testMatches(boolean rpEnabled)
    {
        System.setProperty(RP_ENABLE_PROPERTY, String.valueOf(rpEnabled));
        Environment environment = mock(Environment.class);
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty(RP_ENABLE_PROPERTY, boolean.class)).thenReturn(rpEnabled);
        ReportPortalEnableCondition condition = new ReportPortalEnableCondition();
        assertEquals(rpEnabled, condition.matches(context, metadata));
        verifyNoInteractions(metadata);
    }
}

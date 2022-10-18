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

package org.vividus.reportportal.config.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

class AttachmentPublishingConditionTests
{
    private static final String ATTACHMENT_PROPERTY = "rp.publish-attachments";

    @ParameterizedTest
    @ValueSource(strings = { "true", "false" })
    void shouldEnableOrDisablePublishing(boolean attachmentsEnabled)
    {
        System.setProperty(ATTACHMENT_PROPERTY, Boolean.toString(attachmentsEnabled));
        var environment = mock(Environment.class);
        var context = mock(ConditionContext.class);
        var metadata = mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty(ATTACHMENT_PROPERTY, Boolean.class)).thenReturn(attachmentsEnabled);
        var condition = new AttachmentPublishingPropertyCondition();
        assertEquals(attachmentsEnabled, condition.matches(context, metadata));
        verifyNoInteractions(metadata);
    }
}

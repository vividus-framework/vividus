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

package org.vividus.publishing;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.AttachmentPublisher;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DiffAttachmentPublisherTests
{
    @Mock private AttachmentPublisher attachmentPublisher;

    @InjectMocks private DiffAttachmentPublisher diffAttachmentPublisher;

    @ParameterizedTest
    @CsvSource({ "10, 0, '1\n2\n3',   '4\n3\n2\n', '--- variable\n+++ variable\n@@ -1,3 +1,3 @@\n-1\n-2\n+4\n 3\n+2'",
                 "6, 1,  '1\n2\n3',   '4\n3\n2\n', '--- variable\n+++ variable\n@@ -1,3 +1,3 @@\n-1\n-2\n+4\n 3\n+2'",
                 "6, 1,  '4\n3\n2\n', '1\n2\n3',   '--- variable\n+++ variable\n@@ -1,3 +1,3 @@\n-4\n-3\n+1\n 2\n+3'"
    })
    void shouldPublishAttachment(int textLengthDiffThreshold, int expectedPublisherInvocations,
        String left, String right, String udiff)
    {
        diffAttachmentPublisher.setTextLengthDiffThreshold(textLengthDiffThreshold);
        diffAttachmentPublisher.publishDiff(left, right);
        verify(attachmentPublisher, times(expectedPublisherInvocations)).publishAttachment("/templates/udiff.ftl",
            Map.of("udiff", udiff), "Comparison result");
    }
}

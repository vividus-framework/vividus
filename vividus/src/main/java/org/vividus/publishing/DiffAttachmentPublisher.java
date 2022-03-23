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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import org.vividus.reporter.event.AttachmentPublisher;

public class DiffAttachmentPublisher
{
    private static final String VARIABLE = "variable";
    private static final int CONTEXT_SIZE = 10;
    private final AttachmentPublisher attachmentPublisher;
    private int textLengthDiffThreshold;

    public DiffAttachmentPublisher(AttachmentPublisher attachmentPublisher)
    {
        this.attachmentPublisher = attachmentPublisher;
    }

    public void publishDiff(Object expected, Object actual)
    {
        String expectedString = String.valueOf(expected);
        String actualString = String.valueOf(actual);
        if (expectedString.length() < textLengthDiffThreshold && actualString.length() < textLengthDiffThreshold)
        {
            return;
        }
        List<String> expectedLines = toLines(expectedString);
        List<String> actualLines = toLines(actualString);
        Patch<String> diff = DiffUtils.diff(expectedLines, actualLines);
        String uDiff = UnifiedDiffUtils.generateUnifiedDiff(VARIABLE, VARIABLE, expectedLines, diff, CONTEXT_SIZE)
                                       .stream()
                                       .collect(Collectors.joining("\n"));
        attachmentPublisher.publishAttachment("/templates/udiff.ftl", Map.of("udiff", uDiff), "Comparison result");
    }

    private List<String> toLines(String toConvert)
    {
        return toConvert.lines().collect(Collectors.toList());
    }

    public void setTextLengthDiffThreshold(int textLengthDiffThreshold)
    {
        this.textLengthDiffThreshold = textLengthDiffThreshold;
    }
}

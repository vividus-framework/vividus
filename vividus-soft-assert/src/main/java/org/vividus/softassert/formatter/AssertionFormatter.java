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

package org.vividus.softassert.formatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.vividus.softassert.model.KnownIssue;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.softassert.util.StackTraceFilter;

public class AssertionFormatter implements IAssertionFormatter
{
    private static final char DOT_CHAR = '.';
    private StackTraceFilter stackTraceFilter;

    @Override
    public String getMessage(String description, KnownIssue issue)
    {
        StringBuilder message = new StringBuilder(issue.isPotentiallyKnown() ? "Potentially known" : "Known")
                .append(" issue: ")
                .append(issue.getIdentifier());

        issue.getDescription().ifPresent(desc -> message.append(" - ").append(desc));

        message.append(" (Type: ")
               .append(issue.getType())
               .append(DOT_CHAR);

        issue.getStatus().ifPresent(status -> message.append(" Status: ").append(status));

        issue.getResolution().ifPresent(resolution -> message.append(DOT_CHAR)
                .append(" Resolution: ").append(resolution));

        message.append("). ").append(description);
        return message.toString();
    }

    @Override
    public String getPassedVerificationMessage(int assertionsCount)
    {
        return String.format("Passed verification: %1$d of %1$d assertions passed.", assertionsCount);
    }

    @Override
    public String getErrorsMessage(List<SoftAssertionError> errors, boolean includeStackTraceInformation)
    {
        Writer writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer))
        {
            int index = 1;
            for (SoftAssertionError error : errors)
            {
                if (index == 1 || !includeStackTraceInformation)
                {
                    printWriter.println();
                }
                printWriter.print(index++);
                printWriter.print(") ");

                AssertionError assertionError = error.getError();
                if (includeStackTraceInformation)
                {
                    if (stackTraceFilter != null)
                    {
                        stackTraceFilter.printFilteredStackTrace(assertionError, printWriter);
                    }
                    else
                    {
                        assertionError.printStackTrace(printWriter);
                    }
                }
                else
                {
                    printWriter.print(assertionError.getMessage());
                }
            }
        }
        return writer.toString();
    }

    @Override
    public String getFailedVerificationMessage(List<SoftAssertionError> errors, int assertionsCount)
    {
        StringBuilder stringBuilder = new StringBuilder("Failed verification: ")
                .append(getAssertionInfo(errors.size(), assertionsCount));
        Set<String> knownIssues = new LinkedHashSet<>();
        Set<String> potentiallyKnownIssues = new LinkedHashSet<>();
        for (SoftAssertionError error : errors)
        {
            KnownIssue knownIssue = error.getKnownIssue();
            if (knownIssue == null)
            {
                continue;
            }
            Set<String> targetCollection = knownIssue.isPotentiallyKnown() ? potentiallyKnownIssues : knownIssues;
            targetCollection.add(knownIssue.getIdentifier());
        }
        if (!knownIssues.isEmpty())
        {
            stringBuilder.append(" Known issues: ").append(knownIssues).append('.');
        }
        else
        {
            stringBuilder.append(" Known issues are not found.");
        }
        if (!potentiallyKnownIssues.isEmpty())
        {
            stringBuilder.append(" Potentially known issues: ").append(potentiallyKnownIssues).append('.');
        }
        return stringBuilder.toString();
    }

    /**
     * Returns current assertion info: "X of Y assertions failed"
     *
     * @param failedAssertionsCount Number of failed assertions
     * @param assertionsCount       Total number of assertions
     * @return String "X of Y assertions failed"
     */
    private static String getAssertionInfo(int failedAssertionsCount, int assertionsCount)
    {
        String format = failedAssertionsCount != 0 ? "%1$d of %2$d assertions failed."
                : "%2$d of %2$d assertions passed.";
        return String.format(format, failedAssertionsCount, assertionsCount);
    }

    public void setStackTraceFilter(StackTraceFilter stackTraceFilter)
    {
        this.stackTraceFilter = stackTraceFilter;
    }
}

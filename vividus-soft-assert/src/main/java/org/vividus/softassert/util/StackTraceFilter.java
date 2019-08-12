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

package org.vividus.softassert.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for StackTrace filtering utilities
 */
public class StackTraceFilter
{
    private boolean enabled;
    private List<String> inclusions = new ArrayList<>();
    private List<String> exclusions = new ArrayList<>();

    /**
     * Returns exception stack trace string
     * @param error Throwable
     * @param printWriter Writer
     */
    public void printFilteredStackTrace(Throwable error, PrintWriter printWriter)
    {
        if (enabled)
        {
            printWriter.println(error);
            for (StackTraceElement stackTraceElement : error.getStackTrace())
            {
                String className = stackTraceElement.getClassName();
                boolean toInclude = inclusions.isEmpty() || startsWithAny(className, inclusions);
                boolean toExclude = !exclusions.isEmpty() && startsWithAny(className, exclusions);
                if (toInclude && !toExclude)
                {
                    printWriter.println("\tat " + stackTraceElement);
                }
            }

            Throwable cause = error.getCause();
            if (cause != null)
            {
                printWriter.print("Caused by: ");
                printFilteredStackTrace(cause, printWriter);
            }
        }
        else
        {
            error.printStackTrace(printWriter);
        }
    }

    private static boolean startsWithAny(String str, List<String> prefixes)
    {
        return prefixes.stream().anyMatch(str::startsWith);
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void setInclusions(List<String> inclusions)
    {
        this.inclusions = inclusions;
    }

    public void setExclusions(List<String> exclusions)
    {
        this.exclusions = exclusions;
    }
}

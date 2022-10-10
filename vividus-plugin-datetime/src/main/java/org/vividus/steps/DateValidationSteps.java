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

package org.vividus.steps;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.DateUtils;

public class DateValidationSteps
{
    private static final DateTimeFormatter[] FORMATS = { DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ISO_DATE };

    private final DateUtils dateUtils;
    private ISoftAssert softAssert;

    @Inject
    public DateValidationSteps(DateUtils dateUtils)
    {
        this.dateUtils = dateUtils;
    }

    /**
     * <p>Checks that the difference between given date and current is less than specified number of seconds.
     * The date should be in ISO-8601 format.</p>
     * <p>Note that ISO-8601 is not fully supported, if the step fails to parse date, try to use
     * {@link DateValidationSteps#doesDateConformRule(String, String, ComparisonRule, long)} step,
     * where you can define custom format.</p>
     * @param date           A date text string
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param seconds        The expected number of seconds between the current instant and the provided date.
     */
    @Then("the date '$date' is $comparisonRule current for $seconds seconds")
    public void doesDateConformRule(String date, ComparisonRule comparisonRule, long seconds)
    {
        doesDateConformRule(date, comparisonRule, seconds, DateTimeFormatter.ISO_DATE_TIME);
    }

    /**
     * <p>Checks that the difference between given date and current is less than specified number of seconds.</p>
     * @param date           A date text string
     * @param format         A date format that can be described using standard Java format for DateTimeFormatter
     * (https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html - Patterns for Formatting and
     * Parsing)
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param seconds        The expected number of seconds between the current instant and the provided date.
     */
    @Then(value = "the date '$date' in the format '$format' is $comparisonRule current for $seconds seconds",
            priority = 1)
    public void doesDateConformRule(String date, String format, ComparisonRule comparisonRule, long seconds)
    {
        try
        {
            doesDateConformRule(date, comparisonRule, seconds, DateTimeFormatter.ofPattern(format));
        }
        catch (IllegalArgumentException e)
        {
            softAssert.recordFailedAssertion(e);
        }
    }

    /**
     * Compares two dates according to the given comparison rule. Dates should be in ISO-8601 format.
     * @param date1          The first date text string
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param date2          The second date text string
     */
    @Then("the date '$date1' is $comparisonRule the date '$date2'")
    public void compareDates(String date1, ComparisonRule comparisonRule, String date2)
    {
        for (int i = 0; i < FORMATS.length; i++)
        {
            DateTimeFormatter format = FORMATS[i];
            try
            {
                ZonedDateTime parsedDate1 = dateUtils.parseDateTime(date1, format);
                ZonedDateTime parsedDate2 = dateUtils.parseDateTime(date2, format);
                softAssert.assertThat("Compare dates", parsedDate1, comparisonRule.getComparisonRule(parsedDate2));
                break;
            }
            catch (DateTimeParseException e)
            {
                if (i == FORMATS.length - 1)
                {
                    softAssert.recordFailedAssertion(e);
                }
            }
        }
    }

    private void doesDateConformRule(String date, ComparisonRule comparisonRule, long seconds,
            DateTimeFormatter formatter)
    {
        try
        {
            long timeDifferenceInSeconds =
                    Instant.now().getEpochSecond() - dateUtils.parseDateTime(date, formatter).toEpochSecond();
            softAssert.assertThat(String.format("The difference between %s and the current date", date),
                    timeDifferenceInSeconds, comparisonRule.getComparisonRule(seconds));
        }
        catch (DateTimeParseException e)
        {
            softAssert.recordFailedAssertion(e);
        }
    }

    public void setSoftAssert(ISoftAssert softAssert)
    {
        this.softAssert = softAssert;
    }
}

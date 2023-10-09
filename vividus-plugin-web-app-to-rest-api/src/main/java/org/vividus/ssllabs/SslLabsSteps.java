/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ssllabs;

import java.util.Optional;

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;

public class SslLabsSteps
{
    private final SslLabsClient sslLabsClient;
    private final ISoftAssert softAssert;

    public SslLabsSteps(SslLabsClient sslLabsClient, ISoftAssert softAssert)
    {
        this.sslLabsClient = sslLabsClient;
        this.softAssert = softAssert;
    }

    /**
     * Performs SSL scanning using www.ssllabs.com and compares received grade value with expected one.
     * @param url The URL for SSL scanning and grading.
     * @param comparisonRule The rule to compare values. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param gradeName The name of grade
     * (<i>The possible values:<b>'A+','A','A-','B','C','D','E','F','T','M'</b></i>).
     */
    @Then("SSL rating for URL `$url` is $comparisonRule `$gradeName`")
    public void compareGrades(String url, ComparisonRule comparisonRule, Grade gradeName)
    {
        Optional<Grade> actualGrade = sslLabsClient.performSslScan(url);

        actualGrade.ifPresentOrElse(
                grade -> softAssert.assertThat(
                        String.format("The SSL rating for %s '%s' is %s '%s'", url, grade.getGradeName(),
                                comparisonRule, gradeName.getGradeName()),
                        grade.getGradeValue(), comparisonRule.getComparisonRule(gradeName.getGradeValue())),
                () -> softAssert.recordFailedAssertion(String.format("SSL Scan for URL '%s' is failed", url)));
    }
}

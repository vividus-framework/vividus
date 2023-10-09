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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;

@ExtendWith(MockitoExtension.class)
class SslLabsStepsTests
{
    private static final String URL = "www.tested_site.com";
    private static final String SSL_SCAN_FOR_URL_FAILED = "SSL Scan for URL '%s' is failed";
    private static final String SSL_RATING = "The SSL rating for %s '%s' is %s '%s'";

    @Mock private SslLabsClient sslLabsClient;
    @Mock private ISoftAssert softAssert;

    @InjectMocks private SslLabsSteps sslLabsSteps;

    static Stream<Arguments> buildGrades()
    {
        return Stream.of(
            Arguments.of(Grade.A_PLUS, ComparisonRule.LESS_THAN_OR_EQUAL_TO, Grade.A_PLUS, true),
            Arguments.of(Grade.A, ComparisonRule.GREATER_THAN_OR_EQUAL_TO, Grade.A, true),
            Arguments.of(Grade.A_MINUS, ComparisonRule.LESS_THAN, Grade.A, true),
            Arguments.of(Grade.B, ComparisonRule.GREATER_THAN, Grade.T, true),
            Arguments.of(Grade.C, ComparisonRule.EQUAL_TO, Grade.C, true),
            Arguments.of(Grade.D, ComparisonRule.LESS_THAN_OR_EQUAL_TO, Grade.M, false),
            Arguments.of(Grade.E, ComparisonRule.GREATER_THAN_OR_EQUAL_TO, Grade.F, true),
            Arguments.of(Grade.F, ComparisonRule.LESS_THAN, Grade.B, true),
            Arguments.of(Grade.T, ComparisonRule.GREATER_THAN, Grade.C, false),
            Arguments.of(Grade.M, ComparisonRule.EQUAL_TO, Grade.A_MINUS, false),
            Arguments.of(Grade.B, ComparisonRule.LESS_THAN_OR_EQUAL_TO, Grade.A_PLUS, true),
            Arguments.of(Grade.C, ComparisonRule.GREATER_THAN_OR_EQUAL_TO, Grade.D, true)
        );
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("buildGrades")
    void shouldCompareGrades(Grade sslGrade, ComparisonRule comparisonRule, Grade expectedGrade,
            boolean isAssertionPassed)
    {
        when(sslLabsClient.performSslScan(URL)).thenReturn(Optional.of(sslGrade));
        sslLabsSteps.compareGrades(URL, comparisonRule, expectedGrade);
        verify(sslLabsClient).performSslScan(URL);
        String description = String.format(SSL_RATING, URL, sslGrade.getGradeName(), comparisonRule,
                expectedGrade.getGradeName());
        ArgumentCaptor<Matcher<Integer>> argumentCaptor = ArgumentCaptor.forClass(Matcher.class);
        verify(softAssert).assertThat(eq(description), eq(sslGrade.getGradeValue()), argumentCaptor.capture());
        Function<Matcher<Integer>, Matcher<Integer>> function = isAssertionPassed ? Is::is : Matchers::not;
        assertThat(sslGrade.getGradeValue(), function.apply(argumentCaptor.getValue()));
    }

    @Test
    void shouldRecordFailedAssertionIfScanNotPerformed()
    {
        when(sslLabsClient.performSslScan(URL)).thenReturn(Optional.empty());
        sslLabsSteps.compareGrades(URL, ComparisonRule.EQUAL_TO, Grade.C);
        verify(softAssert, never()).assertThat(any(), any(), any());
        verify(softAssert).recordFailedAssertion(String.format(SSL_SCAN_FOR_URL_FAILED, URL));
    }
}

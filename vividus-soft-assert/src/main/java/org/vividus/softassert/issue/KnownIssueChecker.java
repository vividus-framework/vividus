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

package org.vividus.softassert.issue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.vividus.softassert.issue.IIssueStateProvider.IssueState;
import org.vividus.softassert.model.KnownIssue;

public class KnownIssueChecker implements IKnownIssueChecker
{
    private final IKnownIssueProvider knownIssueProvider;
    private final KnownIssueDataProvider knownIssueDataProvider;
    private final Optional<IIssueStateProvider> issueStateProvider;

    private ITestInfoProvider testInfoProvider;
    private boolean detectPotentiallyKnownIssues;

    public KnownIssueChecker(IKnownIssueProvider knownIssueProvider, KnownIssueDataProvider knownIssueDataProvider,
            Optional<IIssueStateProvider> issueStateProvider)
    {
        this.knownIssueProvider = knownIssueProvider;
        this.knownIssueDataProvider = knownIssueDataProvider;
        this.issueStateProvider = issueStateProvider;
    }

    @Override
    public KnownIssue getKnownIssue(String failedAssertion)
    {
        TestInfo testInfo = testInfoProvider != null ? testInfoProvider.getTestInfo() : null;
        CandidateIssue candidateIssue = new CandidateIssue(testInfo);
        for (Entry<String, ? extends KnownIssueIdentifier> knownIssueEntry : knownIssueProvider
                .getKnownIssueIdentifiers().entrySet())
        {
            KnownIssueIdentifier knownIssueIdentifier = knownIssueEntry.getValue();
            if (knownIssueIdentifier.getAssertionCompiledPattern().matcher(failedAssertion).matches() && candidateIssue
                    .isProperCandidate(knownIssueEntry.getKey(), knownIssueIdentifier))
            {
                break;
            }
        }
        Optional.ofNullable(candidateIssue.issue).ifPresent(this::setState);
        return candidateIssue.issue;
    }

    private void setState(KnownIssue knownIssue)
    {
        issueStateProvider.ifPresent(stateProider ->
        {
            IssueState issueState = stateProider.getIssueState(knownIssue.getBts(), knownIssue.getIdentifier());
            knownIssue.setFixed(issueState.isFixed());
            knownIssue.setIssueDetails(issueState.getDetails());
        });
    }

    public void setTestInfoProvider(ITestInfoProvider testInfoProvider)
    {
        this.testInfoProvider = testInfoProvider;
    }

    public void setDetectPotentiallyKnownIssues(boolean detectPotentiallyKnownIssues)
    {
        this.detectPotentiallyKnownIssues = detectPotentiallyKnownIssues;
    }

    private class CandidateIssue
    {
        private static final int ALL_PATTERNS = 3;

        private int bestPatternsMatched;
        private int currentPatternsMatched;
        private final TestInfo testInfo;
        private KnownIssue issue;

        CandidateIssue(TestInfo testInfo)
        {
            this.testInfo = testInfo;
        }

        boolean isProperCandidate(String candidateId, KnownIssueIdentifier candidate)
        {
            resetCurrentPatternsMatched();
            if (!doAllDataPatternsMatch(candidate.getDynamicCompiledPatterns()) || !doAllDataPatternsMatch(
                    candidate.getRuntimeDataPatterns()))
            {
                return false;
            }
            boolean potentiallyKnown = isPotentiallyKnown(candidate);
            if (potentiallyKnown && !detectPotentiallyKnownIssues)
            {
                return false;
            }
            if (potentiallyKnown)
            {
                resetCurrentPatternsMatched();
            }
            if (issue == null || bestPatternsMatched < currentPatternsMatched)
            {
                bestPatternsMatched = currentPatternsMatched;
                issue = new KnownIssue(candidateId, candidate, potentiallyKnown);
                return bestPatternsMatched == ALL_PATTERNS;
            }
            return false;
        }

        private boolean doAllDataPatternsMatch(Map<String, Pattern> dataPatterns)
        {
            return dataPatterns.entrySet().stream().allMatch(entry -> {
                String key = entry.getKey();
                Pattern pattern = entry.getValue();
                return knownIssueDataProvider.getData(key)
                            .map(pattern::matcher)
                            .map(Matcher::matches)
                            .orElse(false);
            });
        }

        boolean isPotentiallyKnown(KnownIssueIdentifier knownIssueIdentifier)
        {
            return testInfo != null && (
                    isPotentiallyKnown(testInfo.getTestSuite(), knownIssueIdentifier.getTestSuiteCompiledPattern())
                            || isPotentiallyKnown(testInfo.getTestCase(),
                            knownIssueIdentifier.getTestCaseCompiledPattern())
                            || testInfo.getTestSteps()
                                    .stream()
                                    .allMatch(s ->
                                            isPotentiallyKnown(s, knownIssueIdentifier.getTestStepCompiledPattern())));
        }

        boolean isPotentiallyKnown(String testInfo, Pattern pattern)
        {
            if (testInfo != null && pattern != null)
            {
                if (!pattern.matcher(testInfo).matches())
                {
                    return true;
                }
                ++currentPatternsMatched;
            }
            return false;
        }

        void resetCurrentPatternsMatched()
        {
            currentPatternsMatched = 0;
        }
    }
}

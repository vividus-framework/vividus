/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.SoftAssert;
import org.vividus.softassert.model.KnownIssue;

public class DescriptiveSoftAssert extends SoftAssert implements IDescriptiveSoftAssert
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SoftAssert.class);

    @Override
    public <T> boolean assertThat(String businessDescription, String systemDescription, T actual,
            Matcher<? super T> matcher)
    {
        boolean isMatches = matcher.matches(actual);
        if (!isMatches)
        {
            String assertionDescription = getAssertionDescriptionString(actual, matcher);
            return recordAssertion(format(businessDescription, assertionDescription),
                    format(systemDescription, assertionDescription), isMatches);
        }
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        String matchedString = description.toString();
        return recordAssertion(businessDescription + StringUtils.SPACE + matchedString,
                systemDescription + StringUtils.SPACE + matchedString, isMatches);
    }

    private boolean recordAssertion(String businessDescription, String systemDescription, boolean passed)
    {
        if (passed)
        {
            recordPassedAssertion(businessDescription);
            LOGGER.debug(PASS, systemDescription);
        }
        else
        {
            KnownIssue knownIssue = getKnownIssue(businessDescription);
            boolean isBusinessKnown = knownIssue != null;
            if (!isBusinessKnown)
            {
                knownIssue = getKnownIssue(systemDescription);
            }
            String businessAssertionDescription = getKnownIssueMessage(knownIssue, businessDescription);
            String systemAssertionDescription = getKnownIssueMessage(knownIssue, systemDescription);
            LOGGER.error(FAIL, businessAssertionDescription);
            LOGGER.debug(FAIL, systemAssertionDescription);
            String message = knownIssue == null ? businessAssertionDescription : systemAssertionDescription;
            recordAssertionError(knownIssue, message, null);
        }
        return passed;
    }
}

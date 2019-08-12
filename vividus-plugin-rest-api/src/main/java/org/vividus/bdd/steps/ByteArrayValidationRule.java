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

package org.vividus.bdd.steps;

import org.hamcrest.Matchers;
import org.vividus.softassert.ISoftAssert;

public enum ByteArrayValidationRule
{
    IS_EQUAL_TO
    {
        @Override
        public void assertMatchesRule(ISoftAssert softAssert, byte[] expected, byte[] actual)
        {
            if (softAssert.assertEquals("Arrays size", expected.length, actual.length))
            {
                if (Matchers.equalTo(expected).matches(actual))
                {
                    softAssert.recordPassedAssertion(ByteArrayValidationRule.ARRAYS_ARE_EQUAL);
                }
                else
                {
                    for (int index = 0; index < expected.length; index++)
                    {
                        if (expected[index] != actual[index])
                        {
                            softAssert.recordFailedAssertion(
                                    String.format("First mismatch at index %d expected %d but was %d",
                                            index, expected[index], actual[index]));
                            break;
                        }
                    }

                }
            }
        }
    },
    IS_NOT_EQUAL_TO
    {
        @Override
        public void assertMatchesRule(ISoftAssert softAssert, byte[] expected, byte[] actual)
        {
            if (Matchers.not(Matchers.equalTo(expected)).matches(actual))
            {
                softAssert.recordPassedAssertion("Expected and actual arrays are not equal");
            }
            else
            {
                softAssert.recordFailedAssertion(ByteArrayValidationRule.ARRAYS_ARE_EQUAL);
            }
        }
    };

    private static final String ARRAYS_ARE_EQUAL = "Expected and actual arrays are equal";

    public abstract void assertMatchesRule(ISoftAssert softAssert, byte[] expected, byte[] actual);
}

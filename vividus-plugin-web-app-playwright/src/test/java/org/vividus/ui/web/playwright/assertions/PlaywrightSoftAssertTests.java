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

package org.vividus.ui.web.playwright.assertions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.vividus.softassert.ISoftAssert;

@ExtendWith(MockitoExtension.class)
class PlaywrightSoftAssertTests
{
    @Mock private ISoftAssert softAssert;
    @InjectMocks private PlaywrightSoftAssert playwrightSoftAssert;

    @Test
    void shouldRecordSoftAssertionErrorOnFailedAssertion()
    {
        var messageOnFailure = "No text is found";
        var exceptionMessage = "Locator expected to contain text: cat\nReceived: dog\n";
        var assertionFailedError = new AssertionFailedError(exceptionMessage);
        playwrightSoftAssert.runAssertion(messageOnFailure, () -> {
            throw assertionFailedError;
        });
        verify(softAssert).recordFailedAssertion(messageOnFailure + ". " + exceptionMessage, assertionFailedError);
    }

    @Test
    void shouldDoNothingOnSuccessfulAssertion()
    {
        playwrightSoftAssert.runAssertion("any", () -> { });
        verifyNoInteractions(softAssert);
    }
}

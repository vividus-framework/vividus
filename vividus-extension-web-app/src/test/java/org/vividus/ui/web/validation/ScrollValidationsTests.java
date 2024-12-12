/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.ViewportPresence;
import org.vividus.ui.web.action.ScrollActions;

@ExtendWith(MockitoExtension.class)
class ScrollValidationsTests
{
    private static final String ELEMENT_IN_VIEWPORT = "Element is in viewport";
    private static final String ELEMENT_NOT_IN_VIEWPORT = "Element is not in viewport";

    @Mock private ScrollActions<Element> scrollActions;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private ScrollValidations<Element> validations;

    static Stream<Arguments> elementViewportPresenceDataset()
    {
        return Stream.of(
            Arguments.of(ViewportPresence.IS, true,
                    (Consumer<ISoftAssert>) s -> s.recordPassedAssertion(ELEMENT_IN_VIEWPORT)),
            Arguments.of(ViewportPresence.IS, false,
                    (Consumer<ISoftAssert>) s -> s.recordFailedAssertion(ELEMENT_NOT_IN_VIEWPORT)),
            Arguments.of(ViewportPresence.IS_NOT, true,
                    (Consumer<ISoftAssert>) s -> s.recordFailedAssertion(ELEMENT_IN_VIEWPORT)),
            Arguments.of(ViewportPresence.IS_NOT, false,
                    (Consumer<ISoftAssert>) s -> s.recordPassedAssertion(ELEMENT_NOT_IN_VIEWPORT))
        );
    }

    @MethodSource("elementViewportPresenceDataset")
    @ParameterizedTest
    void shouldAssertElementPositionAgainstViewport(ViewportPresence presence, boolean inViewport,
            Consumer<ISoftAssert> verifier)
    {
        Element element = mock();

        when(scrollActions.isElementInViewport(element)).thenReturn(inViewport);

        validations.assertElementPositionAgainstViewport(element, presence);

        verifier.accept(verify(softAssert));
    }

    private static final class Element
    {
    }
}

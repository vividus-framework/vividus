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

package org.vividus.steps.ui.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.DirectionScroller;

@ExtendWith(MockitoExtension.class)
class ScrollDirectionTests
{
    @Mock
    private DirectionScroller scroller;

    private static Stream<Arguments> scrollDirectionProvider()
    {
        return Stream.of(
                Arguments.of(ScrollDirection.TOP, (Consumer<DirectionScroller>) DirectionScroller::scrollToTop),
                Arguments.of(ScrollDirection.BOTTOM, (Consumer<DirectionScroller>) DirectionScroller::scrollToBottom),
                Arguments.of(ScrollDirection.LEFT, (Consumer<DirectionScroller>) DirectionScroller::scrollToLeft),
                Arguments.of(ScrollDirection.RIGHT, (Consumer<DirectionScroller>) DirectionScroller::scrollToRight));
    }

    @ParameterizedTest
    @MethodSource("scrollDirectionProvider")
    void shouldScrollInCorrectDirection(ScrollDirection direction, Consumer<DirectionScroller> scrollerAction)
    {
        direction.scroll(scroller);
        scrollerAction.accept(verify(scroller));
        verifyNoMoreInteractions(scroller);
    }
}

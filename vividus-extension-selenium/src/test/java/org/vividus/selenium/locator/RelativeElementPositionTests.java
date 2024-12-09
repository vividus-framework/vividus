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

package org.vividus.selenium.locator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;

@ExtendWith(MockitoExtension.class)
class RelativeElementPositionTests
{
    @Mock private RelativeLocator.RelativeBy relativeBy;
    @Mock private WebElement webElement;

    static Stream<Arguments> getSimpleRelativePositions()
    {
        return Stream.of(
                Arguments.of(RelativeElementPosition.ABOVE),
                Arguments.of(RelativeElementPosition.BELOW),
                Arguments.of(RelativeElementPosition.TO_LEFT_OF),
                Arguments.of(RelativeElementPosition.TO_RIGHT_OF)
        );
    }

    @ParameterizedTest
    @MethodSource("getSimpleRelativePositions")
    void testException(RelativeElementPosition relativeElementPosition)
    {
        final int maxDistance = 100;
        assertThrows(UnsupportedOperationException.class,
                () -> relativeElementPosition.apply(relativeBy, webElement, maxDistance));
    }

    @Test
    void testAbove()
    {
        RelativeElementPosition.ABOVE.apply(relativeBy, webElement);
        Mockito.verify(relativeBy).above(webElement);
    }

    @Test
    void testBelow()
    {
        RelativeElementPosition.BELOW.apply(relativeBy, webElement);
        Mockito.verify(relativeBy).below(webElement);
    }

    @Test
    void testToLeftOf()
    {
        RelativeElementPosition.TO_LEFT_OF.apply(relativeBy, webElement);
        Mockito.verify(relativeBy).toLeftOf(webElement);
    }

    @Test
    void testToRightOf()
    {
        RelativeElementPosition.TO_RIGHT_OF.apply(relativeBy, webElement);
        Mockito.verify(relativeBy).toRightOf(webElement);
    }

    @Test
    void testNear()
    {
        RelativeElementPosition.NEAR.apply(relativeBy, webElement);
        Mockito.verify(relativeBy).near(webElement);
    }

    @Test
    void testNearWithDistance()
    {
        final int maxDistance = 10;
        RelativeElementPosition.NEAR.apply(relativeBy, webElement, maxDistance);
        Mockito.verify(relativeBy).near(webElement, maxDistance);
    }
}

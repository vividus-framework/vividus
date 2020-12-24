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

package org.vividus.ui.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

class DropDownStateTests
{
    static Stream<Arguments> dataForGetExpectedConditionTest()
    {
        return Stream.of(
            Arguments.of(DropDownState.ENABLED, "element to be clickable: .*"),
            Arguments.of(DropDownState.DISABLED, "condition to not be valid: .*"),
            Arguments.of(DropDownState.SELECTED, "element (.*) to be selected"),
            Arguments.of(DropDownState.NOT_SELECTED, "element (.*) to not be selected"),
            Arguments.of(DropDownState.NOT_VISIBLE, "invisibility of .*"),
            Arguments.of(DropDownState.VISIBLE, "visibility of .*"),
            Arguments.of(DropDownState.MULTI_SELECT, "An element (.*) is multiple select"),
            Arguments.of(DropDownState.SINGLE_SELECT, "An element (.*) is single select")
        );
    }

    static Stream<Arguments> dataForGetExpectedConditionWithSearchCriteriaWithVariousSelectStateTest()
    {
        return Stream.of(
            Arguments.of(true, DropDownState.MULTI_SELECT),
            Arguments.of(false, DropDownState.SINGLE_SELECT)
        );
    }

    @ParameterizedTest
    @MethodSource("dataForGetExpectedConditionTest")
    void testGetExpectedCondition(DropDownState state, String expectedConditionPattern)
    {
        WebElement webElement = mock(WebElement.class);
        ExpectedCondition<?> expectedCondition = state.getExpectedCondition(webElement);
        assertTrue(expectedCondition.toString().matches(expectedConditionPattern));
    }
}

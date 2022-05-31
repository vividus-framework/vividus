/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.visual.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VisualCheckTests
{
    @Test
    void shouldHaveDefaultParameters()
    {
        var visualCheck = new VisualCheck();
        Assertions.assertAll(
            () -> assertEquals(OptionalDouble.empty(), visualCheck.getAcceptableDiffPercentage()),
            () -> assertEquals(OptionalDouble.empty(), visualCheck.getRequiredDiffPercentage()),
            () -> assertEquals(Map.of(), visualCheck.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), visualCheck.getScreenshotParameters()));
    }

    @Test
    void shouldVerifySetters()
    {
        var visualCheck = new VisualCheck();
        var diff = OptionalDouble.of(1);
        visualCheck.setRequiredDiffPercentage(diff);
        visualCheck.setAcceptableDiffPercentage(diff);
        Assertions.assertAll(
            () -> assertEquals(diff, visualCheck.getAcceptableDiffPercentage()),
            () -> assertEquals(diff, visualCheck.getRequiredDiffPercentage()));
    }
}

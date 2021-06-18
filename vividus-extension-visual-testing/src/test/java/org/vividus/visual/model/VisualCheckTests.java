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

package org.vividus.visual.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class VisualCheckTests
{
    @Test
    void shouldHaveDefaultParameters()
    {
        VisualCheck visualCheck = new VisualCheck();
        Assertions.assertAll(
            () -> assertEquals(OptionalInt.empty(), visualCheck.getAcceptableDiffPercentage()),
            () -> assertEquals(OptionalInt.empty(), visualCheck.getRequiredDiffPercentage()),
            () -> assertEquals(Map.of(), visualCheck.getElementsToIgnore()),
            () -> assertEquals(Optional.empty(), visualCheck.getScreenshotConfiguration()));
    }

    @Test
    void shouldVerifySetters()
    {
        VisualCheck visualCheck = new VisualCheck();
        OptionalInt diff = OptionalInt.of(1);
        visualCheck.setRequiredDiffPercentage(diff);
        visualCheck.setAcceptableDiffPercentage(diff);
        Assertions.assertAll(
            () -> assertEquals(diff, visualCheck.getAcceptableDiffPercentage()),
            () -> assertEquals(diff, visualCheck.getRequiredDiffPercentage()));
    }
}

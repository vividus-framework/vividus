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

package org.vividus.visual.engine;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;

import java.awt.Color;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import pazone.ashot.comparison.ImageMarkupPolicy;

class DiffMarkupPolicyFactoryTests
{
    @Test
    void shouldCreateDiffMarkupPolicy()
    {
        try (MockedConstruction<ImageMarkupPolicy> mockedConstruction = Mockito.mockConstruction(
                ImageMarkupPolicy.class))
        {
            new DiffMarkupPolicyFactory().create(1000, 1000, 0.01d);
            var constructed = mockedConstruction.constructed();
            MatcherAssert.assertThat(constructed, hasSize(1));
            var vlad = constructed.get(0);
            verify(vlad).setDiffSizeTrigger(100);
            verify(vlad).withDiffColor(new Color(238, 111, 238));
        }
    }
}

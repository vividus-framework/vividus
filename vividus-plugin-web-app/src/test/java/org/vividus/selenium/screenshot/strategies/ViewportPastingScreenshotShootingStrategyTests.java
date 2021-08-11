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

package org.vividus.selenium.screenshot.strategies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.jupiter.api.Test;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

class ViewportPastingScreenshotShootingStrategyTests
{
    private final ViewportPastingScreenshotShootingStrategy strategy = new ViewportPastingScreenshotShootingStrategy();

    @Test
    void shouldCreageAdjustingViewportPastingDecorator() throws IllegalAccessException, InvocationTargetException,
        NoSuchMethodException
    {
        ShootingStrategy shootingStrategy = strategy.getDecoratedShootingStrategy(null, false, null);
        assertThat(shootingStrategy,
                instanceOf(AdjustingViewportPastingDecorator.class));
        assertEquals("0", BeanUtils.getProperty(shootingStrategy, "headerAdjustment"));
    }
}

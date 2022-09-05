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

package org.vividus.ui.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

class MobileAppScreenshotParametersFactoryTests
{
    private static final String SIMPLE = "simple";
    private static final int TEN = 10;

    private final MobileAppScreenshotParametersFactory factory = new MobileAppScreenshotParametersFactory();

    static Stream<Arguments> args()
    {
        return Stream.of(
                    arguments(Optional.of(SIMPLE), Optional.empty()),
                    arguments(Optional.empty(),    Optional.of(SIMPLE))
                );
    }

    @ParameterizedTest
    @MethodSource("args")
    void shouldCreateScreenshotConfiguration(Optional<String> defaultStrategy, Optional<String> userStrategy)
    {
        var defaultConfiguration = new ScreenshotConfiguration();
        defaultConfiguration.setShootingStrategy(defaultStrategy);
        factory.setShootingStrategy(SIMPLE);
        factory.setIgnoreStrategies(Map.of());
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(SIMPLE, defaultConfiguration)));

        var configuration = new ScreenshotConfiguration();
        configuration.setShootingStrategy(userStrategy);
        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
                IgnoreStrategy.ELEMENT, Set.of(),
                IgnoreStrategy.AREA, Set.of()
        );
        var parameters = factory.create(Optional.of(configuration), null, ignores);
        assertEquals(Optional.of(SIMPLE), parameters.getShootingStrategy());
    }

    @Test
    void shouldCreateScreenshotConfigurationWithIgnores()
    {
        factory.setIgnoreStrategies(Map.of(IgnoreStrategy.ELEMENT, Set.of(), IgnoreStrategy.AREA, Set.of()));
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));

        var locator = mock(Locator.class);
        var ignores = Map.of(
            IgnoreStrategy.ELEMENT, Set.of(locator),
            IgnoreStrategy.AREA, Set.of(locator)
        );
        var parameters = factory.create(Optional.empty(), null, ignores);
        assertEquals(ignores, parameters.getIgnoreStrategies());
    }
}

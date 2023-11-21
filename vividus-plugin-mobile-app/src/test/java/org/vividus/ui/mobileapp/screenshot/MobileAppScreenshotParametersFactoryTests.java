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

package org.vividus.ui.mobileapp.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.selenium.locator.Locator;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.util.property.PropertyMappedCollection;

class MobileAppScreenshotParametersFactoryTests
{
    private static final String VIEWPORT = "VIEWPORT";

    private final MobileAppScreenshotParametersFactory factory = new MobileAppScreenshotParametersFactory();

    @ParameterizedTest
    @CsvSource({
            "VIEWPORT,",
            ",VIEWPORT"
    })
    void shouldCreateScreenshotConfiguration(String defaultStrategy, String userStrategy)
    {
        var defaultConfiguration = new ScreenshotConfiguration();
        defaultConfiguration.setShootingStrategy(Optional.ofNullable(defaultStrategy));
        factory.setShootingStrategy(VIEWPORT);
        factory.setIgnoreStrategies(Map.of());
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(VIEWPORT, defaultConfiguration)));

        var configuration = new ScreenshotConfiguration();
        configuration.setShootingStrategy(Optional.ofNullable(userStrategy));
        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
                IgnoreStrategy.ELEMENT, Set.of(),
                IgnoreStrategy.AREA, Set.of()
        );
        var parameters = factory.create(Optional.of(configuration), null, ignores);
        assertEquals(Optional.of(VIEWPORT), parameters.getShootingStrategy());
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

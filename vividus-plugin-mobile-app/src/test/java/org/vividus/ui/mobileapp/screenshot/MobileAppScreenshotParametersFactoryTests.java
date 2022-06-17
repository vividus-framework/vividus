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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class MobileAppScreenshotParametersFactoryTests
{
    private static final String SIMPLE = "simple";
    private static final int TEN = 10;

    @InjectMocks private MobileAppScreenshotParametersFactory factory;

    static Stream<Arguments> args()
    {
        return Stream.of(
                    arguments(TEN, Optional.of(SIMPLE), 0,   Optional.empty()),
                    arguments(0,   Optional.empty(),    TEN, Optional.of(SIMPLE))
                );
    }

    @ParameterizedTest
    @MethodSource("args")
    void shouldCreateScreenshotConfiguration(int defaultFooter, Optional<String> defaultStrategy, int userFooter,
            Optional<String> userStrategy)
    {
        ScreenshotConfiguration defaultConfiguration = new ScreenshotConfiguration();
        defaultConfiguration.setNativeFooterToCut(defaultFooter);
        defaultConfiguration.setShootingStrategy(defaultStrategy);
        factory.setShootingStrategy(SIMPLE);
        factory.setIgnoreStrategies(Map.of());
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(SIMPLE, defaultConfiguration)));

        ScreenshotConfiguration parameters = new ScreenshotConfiguration();
        parameters.setNativeFooterToCut(userFooter);
        parameters.setShootingStrategy(userStrategy);
        Optional<ScreenshotParameters> createdConfiguration = factory.create(Optional.of(parameters));
        assertTrue(createdConfiguration.isPresent());
        ScreenshotParameters configuration = createdConfiguration.get();
        assertEquals(Optional.of(SIMPLE), configuration.getShootingStrategy());
        assertEquals(TEN, configuration.getNativeFooterToCut());
    }

    @Test
    void shouldCreateScreenshotConfigurationWithIgnores()
    {
        ScreenshotConfiguration defaultConfiguration = new ScreenshotConfiguration();
        factory.setShootingStrategy(SIMPLE);
        factory.setIgnoreStrategies(Map.of(IgnoreStrategy.ELEMENT, Set.of(), IgnoreStrategy.AREA, Set.of()));
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(SIMPLE, defaultConfiguration)));

        Locator locator = mock(Locator.class);
        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
            IgnoreStrategy.ELEMENT, Set.of(locator),
            IgnoreStrategy.AREA, Set.of(locator)
        );
        Optional<ScreenshotParameters> createdConfiguration = factory.create(ignores);
        assertTrue(createdConfiguration.isPresent());

        ScreenshotParameters configuration = createdConfiguration.get();
        assertEquals(ignores, configuration.getIgnoreStrategies());
    }
}

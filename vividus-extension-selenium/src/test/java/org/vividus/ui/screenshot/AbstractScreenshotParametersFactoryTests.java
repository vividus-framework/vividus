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

package org.vividus.ui.screenshot;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(TestLoggerFactoryExtension.class)
class AbstractScreenshotParametersFactoryTests
{
    private static final String DEFAULT = "default";
    private static final String IGNORES_TABLE = "ignores table";
    private static final LoggingEvent WARNING_MESSAGE = warn("The passing of elements and areas to ignore through {}"
            + " is deprecated, please use screenshot configuration instead", IGNORES_TABLE);

    private final TestScreenshotParametersFactory factory = new TestScreenshotParametersFactory();

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AbstractScreenshotParametersFactory.class);

    @Test
    void shouldUseDefaultConfiguration()
    {
        var cutBottom = 5;

        factory.setScreenshotConfigurations(
                new PropertyMappedCollection<>(Map.of(DEFAULT, createConfiguration(cutBottom))));
        factory.setShootingStrategy(DEFAULT);
        factory.setIgnoreStrategies(createEmptyIgnores());

        var parameters = factory.create(Optional.empty(), null, createEmptyIgnores());
        assertEquals(cutBottom, parameters.getCutBottom());
        assertEquals(1, parameters.getCutTop());
        assertEquals(2, parameters.getCutLeft());
        assertEquals(3, parameters.getCutRight());
    }

    @Test
    void shouldMergeUserDefinedAndDefaultConfigurations()
    {
        factory.setScreenshotConfigurations(
                new PropertyMappedCollection<>(Map.of(DEFAULT, createConfiguration(5))));
        factory.setShootingStrategy(DEFAULT);
        factory.setIgnoreStrategies(createEmptyIgnores());

        var parameters = factory.create(Optional.of(createConfiguration(10)), null, createEmptyIgnores());
        assertEquals(15, parameters.getCutBottom());
    }

    @Test
    void shouldReturnCustomConfigurationWhenDefaultIsMissing()
    {
        var cutBottom = 5;

        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of()));
        factory.setShootingStrategy(DEFAULT);
        factory.setIgnoreStrategies(createEmptyIgnores());

        var parameters = factory.create(Optional.of(createConfiguration(cutBottom)), null,
                createEmptyIgnores());
        assertEquals(cutBottom, parameters.getCutBottom());
        assertEquals(1, parameters.getCutTop());
    }

    @Test
    void shouldFailOnInvalidCutSize()
    {
        var thrown = assertThrows(IllegalArgumentException.class, () -> factory.ensureValidCutSize(-1, "header"));
        assertEquals("The header to cut must be greater than or equal to zero", thrown.getMessage());
    }

    @Test
    void shouldCreateParametersFromUserDefinedConfiguration()
    {
        var stepElementLocator = mock(Locator.class);
        var stepAreaLocator = mock(Locator.class);

        var configuration = new ScreenshotConfiguration();
        configuration.setElementsToIgnore(Set.of(stepElementLocator));
        configuration.setAreasToIgnore(Set.of(stepAreaLocator));
        configuration.setShootingStrategy(Optional.of(DEFAULT));
        configuration.setCutBottom(1);

        var globalElementLocator = mock(Locator.class);
        var globalAreaLocator = mock(Locator.class);

        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));
        factory.setIgnoreStrategies(Map.of(
                IgnoreStrategy.ELEMENT, Set.of(globalElementLocator),
                IgnoreStrategy.AREA, Set.of(globalAreaLocator)
        ));

        ScreenshotParameters parameters = factory.create(Optional.of(configuration), null, createEmptyIgnores());

        assertEquals(Optional.of(DEFAULT), parameters.getShootingStrategy());
        assertEquals(Map.of(
                IgnoreStrategy.ELEMENT, Set.of(stepElementLocator, globalElementLocator),
                IgnoreStrategy.AREA, Set.of(stepAreaLocator, globalAreaLocator)
            ),
            parameters.getIgnoreStrategies()
        );
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void shouldFailIfBothSourcesAreNotEmpty()
    {
        var locator = mock(Locator.class);

        var screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setAreasToIgnore(Set.of(locator));
        screenshotConfiguration.setElementsToIgnore(Set.of(locator));

        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));

        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
                IgnoreStrategy.AREA, Set.of(locator),
                IgnoreStrategy.ELEMENT, Set.of(locator)
        );
        var configuration = Optional.of(screenshotConfiguration);
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> factory.create(configuration, IGNORES_TABLE, ignores));
        assertEquals("The elements and areas to ignore must be passed either through screenshot configuration"
                        + " or ignores table", thrown.getMessage());
    }

    @Test
    void shouldPatchIgnores()
    {
        var locator = mock(Locator.class);
        var screenshotConfiguration = new ScreenshotConfiguration();

        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));
        factory.setIgnoreStrategies(createEmptyIgnores());

        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
                IgnoreStrategy.AREA, Set.of(locator),
                IgnoreStrategy.ELEMENT, Set.of(locator)
        );

        ScreenshotParameters parameters = factory.create(Optional.of(screenshotConfiguration), IGNORES_TABLE, ignores);

        assertEquals(ignores, parameters.getIgnoreStrategies());
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(WARNING_MESSAGE, WARNING_MESSAGE)));
    }

    @Test
    void shouldReturnEmptyParametersWhenNoDefaultConfigurationIsAvailable()
    {
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));

        Optional<ScreenshotParameters> parameters = factory.create();

        assertEquals(Optional.empty(), parameters);
    }

    @Test
    void shouldCreateParametersFromDefaultConfiguration()
    {
        var cutBottom = 5;

        factory.setScreenshotConfigurations(
                new PropertyMappedCollection<>(Map.of(DEFAULT, createConfiguration(cutBottom))));
        factory.setShootingStrategy(DEFAULT);
        factory.setIgnoreStrategies(createEmptyIgnores());

        var parameters = factory.create();
        assertTrue(parameters.isPresent());
    }

    private Map<IgnoreStrategy, Set<Locator>> createEmptyIgnores()
    {
        return Map.of(
                IgnoreStrategy.ELEMENT, Set.of(),
                IgnoreStrategy.AREA, Set.of()
        );
    }

    private ScreenshotConfiguration createConfiguration(int cutBottom)
    {
        var screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setCutTop(1);
        screenshotConfiguration.setCutBottom(cutBottom);
        screenshotConfiguration.setCutLeft(2);
        screenshotConfiguration.setCutRight(3);
        return screenshotConfiguration;
    }

    private static final class TestScreenshotParametersFactory
            extends AbstractScreenshotParametersFactory<ScreenshotConfiguration, ScreenshotParameters>
    {
        @Override
        protected ScreenshotConfiguration createScreenshotConfiguration()
        {
            return new ScreenshotConfiguration();
        }

        @Override
        protected ScreenshotParameters createScreenshotParameters()
        {
            return new ScreenshotParameters();
        }

        @Override
        protected BinaryOperator<ScreenshotConfiguration> getConfigurationMerger()
        {
            return (currentConfig, defaultConfig) -> {
                currentConfig.setCutBottom(currentConfig.getCutBottom() + defaultConfig.getCutBottom());
                return currentConfig;
            };
        }

        @Override
        protected void configure(ScreenshotConfiguration config, ScreenshotParameters parameters)
        {
            // Do nothing
        }
    }
}

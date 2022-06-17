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

package org.vividus.ui.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;

import org.junit.jupiter.api.Test;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.search.Locator;
import org.vividus.util.property.PropertyMappedCollection;

class AbstractScreenshotParametersFactoryTests
{
    private static final String DEFAULT = "default";
    private final TestScreenshotParametersFactory factory = new TestScreenshotParametersFactory();

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseDefaultConfig()
    {
        PropertyMappedCollection<ScreenshotConfiguration> screenshotConfigurations = mock(
                PropertyMappedCollection.class);
        factory.setScreenshotConfigurations(screenshotConfigurations);
        factory.setShootingStrategy(DEFAULT);
        int nativeFooterToCut = 5;
        when(screenshotConfigurations.getNullable(DEFAULT))
                .thenReturn(Optional.of(createParametersWith(nativeFooterToCut)));
        BinaryOperator<ScreenshotConfiguration> merger = mock(BinaryOperator.class);
        Optional<ScreenshotConfiguration> configuration = factory.getScreenshotConfiguration(Optional.empty(), merger);
        assertEquals(nativeFooterToCut, configuration.get().getNativeFooterToCut());
        verifyNoInteractions(merger);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMergeUserDefinedAndDefaultConfiguration()
    {
        PropertyMappedCollection<ScreenshotConfiguration> screenshotConfigurations = mock(
                PropertyMappedCollection.class);
        factory.setScreenshotConfigurations(screenshotConfigurations);
        factory.setShootingStrategy(DEFAULT);
        int nativeFooterToCut = 5;
        when(screenshotConfigurations.getNullable(DEFAULT))
                .thenReturn(Optional.of(createParametersWith(nativeFooterToCut)));
        Optional<ScreenshotConfiguration> configuration = factory
                .getScreenshotConfiguration(Optional.of(createParametersWith(10)), (u, d) ->
                {
                    u.setNativeFooterToCut(u.getNativeFooterToCut() + d.getNativeFooterToCut());
                    return u;
                });
        assertEquals(15, configuration.get().getNativeFooterToCut());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnCustomConfigWhenDefaultIsMissing()
    {
        PropertyMappedCollection<ScreenshotConfiguration> screenshotConfigurations = mock(
                PropertyMappedCollection.class);
        factory.setScreenshotConfigurations(screenshotConfigurations);
        factory.setShootingStrategy(DEFAULT);
        when(screenshotConfigurations.getNullable(DEFAULT))
                .thenReturn(Optional.empty());
        BinaryOperator<ScreenshotConfiguration> merger = mock(BinaryOperator.class);
        Optional<ScreenshotConfiguration> configuration = Optional.of(createParametersWith(10));
        assertSame(configuration, factory.getScreenshotConfiguration(configuration, merger));
        verifyNoInteractions(merger);
    }

    @Test
    void shouldFailOnInvalidCutSize()
    {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> factory.ensureValidCutSize(-1, "header"));
        assertEquals("The header to cut must be greater than or equal to zero", thrown.getMessage());
    }

    @Test
    void shouldCreateConfigurationWithBaseParameters()
    {
        Locator stepElementLocator = mock(Locator.class);
        Locator stepAreaLocator = mock(Locator.class);

        ScreenshotConfiguration parameters = new ScreenshotConfiguration();
        parameters.setElementsToIgnore(Set.of(stepElementLocator));
        parameters.setAreasToIgnore(Set.of(stepAreaLocator));
        parameters.setShootingStrategy(Optional.of(DEFAULT));
        parameters.setNativeFooterToCut(1);

        Locator commonElementLocator = mock(Locator.class);
        Locator commonAreaLocator = mock(Locator.class);

        factory.setIgnoreStrategies(Map.of(
            IgnoreStrategy.ELEMENT, Set.of(commonElementLocator),
            IgnoreStrategy.AREA, Set.of(commonAreaLocator)
        ));

        ScreenshotParameters configuration = factory.createWithBaseConfiguration(parameters);

        assertEquals(Optional.of(DEFAULT), configuration.getShootingStrategy());
        assertEquals(1, configuration.getNativeFooterToCut());
        assertEquals(Map.of(
            IgnoreStrategy.ELEMENT, Set.of(stepElementLocator, commonElementLocator),
            IgnoreStrategy.AREA, Set.of(stepAreaLocator, commonAreaLocator)
        ), configuration.getIgnoreStrategies());
    }

    private ScreenshotConfiguration createParametersWith(int nativeFooterToCut)
    {
        ScreenshotConfiguration screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setNativeFooterToCut(nativeFooterToCut);
        return screenshotConfiguration;
    }

    private static final class TestScreenshotParametersFactory
            extends AbstractScreenshotParametersFactory<ScreenshotConfiguration, ScreenshotParameters>
    {
        @Override
        public Optional<ScreenshotParameters> create(Optional<ScreenshotConfiguration> screenshotConfiguration)
        {
            return Optional.empty();
        }

        @Override
        public Optional<ScreenshotParameters> create(Map<IgnoreStrategy, Set<Locator>> ignores)
        {
            return Optional.empty();
        }

        @Override
        protected ScreenshotParameters createScreenshotParameters()
        {
            return new ScreenshotParameters();
        }
    }
}

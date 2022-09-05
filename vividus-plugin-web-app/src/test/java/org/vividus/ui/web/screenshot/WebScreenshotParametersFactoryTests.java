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

package org.vividus.ui.web.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.CoordsProviderType;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class WebScreenshotParametersFactoryTests
{
    private static final String SIMPLE = "simple";
    private static final String IGNORES_TABLE = "ignores table";

    @Mock private Locator locator;
    @Mock private WebElement scrollableElement;

    @Mock private ISearchActions searchActions;
    @InjectMocks private WebScreenshotParametersFactory factory;

    private Map<IgnoreStrategy, Set<Locator>> createEmptyIgnores()
    {
        return Map.of(
                IgnoreStrategy.ELEMENT, Set.of(),
                IgnoreStrategy.AREA, Set.of()
        );
    }

    @Test
    void shouldCreateScreenshotConfiguration()
    {
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(
                new PropertyMappedCollection<>(Map.of(SIMPLE, new WebScreenshotConfiguration())));
        factory.setIgnoreStrategies(Map.of());

        var userConfiguration = new WebScreenshotConfiguration();
        userConfiguration.setShootingStrategy(Optional.of(SIMPLE));
        userConfiguration.setNativeHeaderToCut(11);
        userConfiguration.setNativeFooterToCut(12);
        userConfiguration.setWebHeaderToCut(13);
        userConfiguration.setWebFooterToCut(14);
        userConfiguration.setScrollableElement(Optional.of(locator));
        when(searchActions.findElement(locator)).thenReturn(Optional.of(scrollableElement));
        userConfiguration.setCoordsProvider("WEB_DRIVER");
        userConfiguration.setScrollTimeout("PT1S");

        var parameters = factory.create(Optional.of(userConfiguration), IGNORES_TABLE, createEmptyIgnores());

        assertEquals(Optional.of(SIMPLE), parameters.getShootingStrategy());
        assertEquals(11, parameters.getNativeHeaderToCut());
        assertEquals(12, parameters.getNativeFooterToCut());
        assertEquals(13, parameters.getWebCutOptions().getWebHeaderToCut());
        assertEquals(14, parameters.getWebCutOptions().getWebFooterToCut());
        assertEquals(Optional.of(scrollableElement), parameters.getScrollableElement());
        assertEquals(CoordsProviderType.WEB_DRIVER, parameters.getCoordsProvider());
        assertEquals(Duration.ofSeconds(1), parameters.getScrollTimeout());
    }

    @Test
    void shouldCreateScreenshotConfigurationWithIgnores()
    {
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(new HashMap<>()));
        factory.setIgnoreStrategies(Map.of(IgnoreStrategy.ELEMENT, Set.of(), IgnoreStrategy.AREA, Set.of()));

        var locator = mock(Locator.class);
        var ignores = Map.of(
                IgnoreStrategy.ELEMENT, Set.of(locator),
                IgnoreStrategy.AREA, Set.of(locator)
        );
        var parameters = factory.create(Optional.empty(), IGNORES_TABLE, ignores);
        assertEquals(ignores, parameters.getIgnoreStrategies());
    }

    @Test
    void shouldFailIfElementByLocatorDoesNotExist()
    {
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of()));
        factory.setIgnoreStrategies(Map.of());

        var webScreenshotConfiguration = new WebScreenshotConfiguration();
        webScreenshotConfiguration.setScrollableElement(Optional.of(locator));
        when(searchActions.findElement(locator)).thenReturn(Optional.empty());
        var configuration = Optional.of(webScreenshotConfiguration);
        Map<IgnoreStrategy, Set<Locator>> ignores = createEmptyIgnores();

        var thrown = assertThrows(ScreenshotPrecondtionMismatchException.class,
                () -> factory.create(configuration, IGNORES_TABLE, ignores));
        assertEquals("Scrollable element does not exist", thrown.getMessage());
    }

    @Test
    void shouldFailOnInvalidFooterCutSize()
    {
        var config = new WebScreenshotConfiguration();
        var screenshotParameters =  new WebScreenshotParameters();
        config.setNativeFooterToCut(-1);
        var thrown = assertThrows(IllegalArgumentException.class, () -> factory.configure(config,
            screenshotParameters));
        assertEquals("The native footer to cut must be greater than or equal to zero", thrown.getMessage());
    }
}

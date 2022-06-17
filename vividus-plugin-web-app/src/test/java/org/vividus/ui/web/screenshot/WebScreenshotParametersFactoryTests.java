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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
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
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class WebScreenshotParametersFactoryTests
{
    private static final String SIMPLE = "simple";
    private static final int TEN = 10;

    @Mock private Locator locator;
    @Mock private WebElement scrollableElement;

    @Mock private ISearchActions searchActions;
    @InjectMocks private WebScreenshotParametersFactory factory;

    @Test
    void shouldCreateScreenshotConfiguration()
    {
        WebScreenshotConfiguration defaultConfiguration = new WebScreenshotConfiguration();
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(SIMPLE, defaultConfiguration)));
        factory.setIgnoreStrategies(Map.of());

        WebScreenshotConfiguration userConfiguration = new WebScreenshotConfiguration();
        userConfiguration.setShootingStrategy(Optional.of(SIMPLE));
        userConfiguration.setNativeHeaderToCut(TEN + 1);
        userConfiguration.setNativeFooterToCut(TEN + 2);
        userConfiguration.setWebHeaderToCut(TEN + 3);
        userConfiguration.setWebFooterToCut(TEN + 4);
        userConfiguration.setScrollableElement(Optional.of(locator));
        when(searchActions.findElement(locator)).thenReturn(Optional.of(scrollableElement));
        userConfiguration.setCoordsProvider("WEB_DRIVER");
        userConfiguration.setScrollTimeout("PT1S");

        Optional<ScreenshotParameters> createdParameters = factory.create(Optional.of(userConfiguration));
        assertTrue(createdParameters.isPresent());
        WebScreenshotParameters configuration = (WebScreenshotParameters) createdParameters.get();

        assertEquals(Optional.of(SIMPLE), configuration.getShootingStrategy());
        assertEquals(TEN + 1, configuration.getNativeHeaderToCut());
        assertEquals(TEN + 2, configuration.getNativeFooterToCut());
        WebCutOptions webCutOptions = configuration.getWebCutOptions();
        assertEquals(TEN + 3, webCutOptions.getWebHeaderToCut());
        assertEquals(TEN + 4, webCutOptions.getWebFooterToCut());
        assertEquals(Optional.of(scrollableElement), configuration.getScrollableElement());
        assertEquals(CoordsProviderType.WEB_DRIVER, configuration.getCoordsProvider());
        assertEquals(Duration.ofSeconds(1), configuration.getScrollTimeout());
    }

    @Test
    void shouldCreateScreenshotConfigurationWithIgnores()
    {
        WebScreenshotConfiguration defaultConfiguration = new WebScreenshotConfiguration();
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of(SIMPLE, defaultConfiguration)));
        factory.setIgnoreStrategies(Map.of(IgnoreStrategy.ELEMENT, Set.of(), IgnoreStrategy.AREA, Set.of()));

        Locator locator = mock(Locator.class);
        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
            IgnoreStrategy.ELEMENT, Set.of(locator),
            IgnoreStrategy.AREA, Set.of(locator)
        );
        Optional<ScreenshotParameters> createdParameters = factory.create(ignores);
        assertTrue(createdParameters.isPresent());
        WebScreenshotParameters configuration = (WebScreenshotParameters) createdParameters.get();
        assertEquals(ignores, configuration.getIgnoreStrategies());
    }

    @Test
    void shouldFailIfElementByLocatorDoesNotExist()
    {
        factory.setShootingStrategy(SIMPLE);
        factory.setScreenshotConfigurations(new PropertyMappedCollection<>(Map.of()));
        factory.setIgnoreStrategies(Map.of());

        WebScreenshotConfiguration userParameters = new WebScreenshotConfiguration();
        userParameters.setScrollableElement(Optional.of(locator));
        when(searchActions.findElement(locator)).thenReturn(Optional.empty());
        Optional<WebScreenshotConfiguration> parameters = Optional.of(userParameters);

        ScreenshotPrecondtionMismatchException thrown = assertThrows(ScreenshotPrecondtionMismatchException.class,
            () -> factory.create(parameters));
        assertEquals("Scrollable element does not exist", thrown.getMessage());
    }
}

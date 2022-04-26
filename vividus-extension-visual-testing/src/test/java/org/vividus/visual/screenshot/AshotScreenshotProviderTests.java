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

package org.vividus.visual.screenshot;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotDebugger;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.util.ResourceUtils;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class AshotScreenshotProviderTests
{
    private static final String BASELINE = "baseline";

    @Mock
    private Locator aLocator;
    @Mock
    private Locator bLocator;
    @Mock
    private Locator elementLocator;
    @Mock
    private Locator areaLocator;

    @Mock
    private AshotScreenshotTaker<?> screenshotTaker;
    @Mock(lenient = true)
    private ISearchActions searchActions;
    @Mock
    private ScreenshotDebugger screenshotDebugger;
    @Mock
    private CoordsProvider coordsProvider;
    @Mock
    private IWebDriverProvider webDriverProvider;

    @InjectMocks
    private AshotScreenshotProvider screenshotProvider;

    private static Map<IgnoreStrategy, Set<Locator>> createMap(IgnoreStrategy key1,
            Set<Locator> value1, IgnoreStrategy key2, Set<Locator> value2)
    {
        Map<IgnoreStrategy, Set<Locator>> map = new LinkedHashMap<>(2);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    @Test
    void shouldTakeScreenshot()
    {
        SearchContext searchContext = mock(SearchContext.class);
        VisualCheck visualCheck = mockSearchContext(searchContext);
        Screenshot screenshot = mock(Screenshot.class);
        when(searchActions.findElements(aLocator)).thenReturn(List.of());
        when(screenshotTaker.takeAshotScreenshot(searchContext, Optional.empty())).thenReturn(screenshot);
        screenshotProvider.setIgnoreStrategies(Map.of(IgnoreStrategy.AREA, Set.of(aLocator)));
        assertSame(screenshot, screenshotProvider.take(visualCheck));
        verifyNoInteractions(screenshotDebugger);
    }

    private VisualCheck mockSearchContext(SearchContext searchContext)
    {
        VisualCheck visualCheck = new VisualCheck(BASELINE, VisualActionType.ESTABLISH);
        visualCheck.setSearchContext(searchContext);
        return visualCheck;
    }

    @Test
    void shouldTakeScreenshotAndProcessIgnoredElements() throws IOException
    {
        Map<IgnoreStrategy, Set<Locator>> strategies = createMap(IgnoreStrategy.ELEMENT,
                Set.of(elementLocator, aLocator), IgnoreStrategy.AREA, Set.of(bLocator));
        Map<IgnoreStrategy, Set<Locator>> stepLevelStrategies = Map.of(IgnoreStrategy.ELEMENT, Set.of(aLocator),
                IgnoreStrategy.AREA, Set.of(bLocator, areaLocator));

        SearchContext searchContext = mock(SearchContext.class);
        VisualCheck visualCheck = mockSearchContext(searchContext);
        visualCheck.setElementsToIgnore(stepLevelStrategies);
        screenshotProvider.setIgnoreStrategies(strategies);
        Screenshot screenshot = new Screenshot(loadImage("original"));
        WebDriver driver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(driver);
        WebElement element = mock(WebElement.class);
        when(searchActions.findElements(elementLocator)).thenReturn(List.of(element));
        when(coordsProvider.ofElement(driver, element)).thenReturn(new Coords(704, 89, 272, 201));
        WebElement area = mock(WebElement.class);
        when(searchActions.findElements(areaLocator)).thenReturn(List.of(area));
        when(coordsProvider.ofElement(driver, area)).thenReturn(new Coords(270, 311, 1139, 52));
        when(screenshotTaker.takeAshotScreenshot(searchContext, Optional.empty())).thenReturn(screenshot);

        Screenshot actual = screenshotProvider.take(visualCheck);

        verifyScreenshot(screenshot, actual);
    }

    private void verifyScreenshot(Screenshot screenshot, Screenshot actual) throws IOException
    {
        assertSame(actual, screenshot);
        BufferedImage afterCropping = loadImage("after_cropping");
        assertThat(actual.getImage(), ImageTool.equalImage(afterCropping));
        verify(searchActions).findElements(aLocator);
        verify(searchActions).findElements(bLocator);
        verify(searchActions).findElements(elementLocator);
        verify(searchActions).findElements(areaLocator);
        BufferedImage elementCropped = loadImage("element_cropped");
        InOrder ordered = Mockito.inOrder(screenshotDebugger);
        ordered.verify(screenshotDebugger).debug(eq(AshotScreenshotProvider.class), eq("cropped_by_ELEMENT"),
                equalTo(elementCropped));
        ordered.verify(screenshotDebugger).debug(eq(AshotScreenshotProvider.class), eq("cropped_by_AREA"),
                equalTo(afterCropping));
    }

    private BufferedImage equalTo(BufferedImage expected)
    {
        return argThat(actual -> ImageTool.equalImage(expected).matches(actual));
    }

    private BufferedImage loadImage(String fileName) throws IOException
    {
        return ImageIO.read(ResourceUtils.loadFile(AshotScreenshotProviderTests.class, "/" + fileName + ".png"));
    }
}

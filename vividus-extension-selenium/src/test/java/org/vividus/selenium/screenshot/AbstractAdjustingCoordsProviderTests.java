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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;

@ExtendWith(MockitoExtension.class)
class AbstractAdjustingCoordsProviderTests
{
    @Mock private IUiContext uiContext;

    @Test
    void shouldAdjustCoordsToTheCurrentSearchContext()
    {
        WebElement contextElement = mock(WebElement.class);
        Coords webElementCoords = new Coords(5, 15, 150, 30);
        Coords searchContextCoords = new Coords(10, 10, 100, 50);
        when(uiContext.getSearchContext()).thenReturn(contextElement);
        Coords coords = new TestCoordsProvider(uiContext,
                () -> searchContextCoords).adjustToSearchContext(webElementCoords);
        assertAll(() -> assertEquals(0, coords.getX()),
                  () -> assertEquals(5, coords.getY()),
                  () -> assertEquals(100, coords.getWidth()),
                  () -> assertEquals(30, coords.getHeight()));
    }

    @Test
    void shouldReturnAsIsIfTheContextIsNotSet()
    {
        Coords webElementCoords = new Coords(5, 15, 150, 30);
        assertSame(webElementCoords, new TestCoordsProvider(uiContext, () ->
        {
            throw new IllegalStateException("will not run");
        }).adjustToSearchContext(webElementCoords));
    }

    @Test
    void shouldExposeUIContext()
    {
        assertSame(uiContext, new TestCoordsProvider(uiContext, null).getUiContext());
    }

    private static final class TestCoordsProvider extends AbstractAdjustingCoordsProvider
    {
        private static final long serialVersionUID = 6120548730279509334L;

        private final transient Supplier<Coords> coordsProvider;

        private TestCoordsProvider(IUiContext uiContext, Supplier<Coords> coordsProvider)
        {
            super(uiContext);
            this.coordsProvider = coordsProvider;
        }

        @Override
        protected Coords getCoords(WebElement element)
        {
            return coordsProvider.get();
        }
    }
}

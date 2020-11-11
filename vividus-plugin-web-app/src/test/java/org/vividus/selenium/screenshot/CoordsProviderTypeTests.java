/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.WebJavascriptActions;

import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

@ExtendWith(MockitoExtension.class)
class CoordsProviderTypeTests
{
    @Mock
    private WebJavascriptActions javascriptActions;

    private static Stream<Arguments> coordsProviderSource()
    {
        return Stream.of(
          Arguments.of(CoordsProviderType.CEILING, CeilingJsCoordsProvider.class),
          Arguments.of(CoordsProviderType.WEB_DRIVER, WebDriverCoordsProvider.class)
        );
    }

    @MethodSource("coordsProviderSource")
    @ParameterizedTest
    void shouldReturnCoordsProviderType(CoordsProviderType type, Class<? extends CoordsProvider> expectedClazz)
    {
        assertThat(type.create(javascriptActions), is(instanceOf(expectedClazz)));
    }
}

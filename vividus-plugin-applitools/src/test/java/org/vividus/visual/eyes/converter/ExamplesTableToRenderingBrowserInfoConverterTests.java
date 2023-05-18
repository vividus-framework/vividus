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

package org.vividus.visual.eyes.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.visualgrid.model.ChromeEmulationInfo;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceName;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExamplesTableToRenderingBrowserInfoConverterTests
{
    private final ExamplesTableToRenderingBrowserInfoConverter converter
        = new ExamplesTableToRenderingBrowserInfoConverter();

    @Test
    void shouldConvertParameters()
    {
        // CHECKSTYLE:OFF
        String table = "|profile                |browser|viewportSize|deviceName|screenOrientation|" + System.lineSeparator()
                     + "|Desktop                |CHROME |820x640     |          |                 |" + System.lineSeparator()
                     + "|chrome_mobile_emulation|       |            |iPhone X  |PORTRAIT         |" + System.lineSeparator()
                     + "|iOS                    |       |            |iPhone XS |LANDSCAPE        |" + System.lineSeparator();
        // CHECKSTYLE:ON

        List<IRenderingBrowserInfo> infos = List.of(converter.convertValue(createTable(table), null));
        assertThat(infos, hasSize(3));

        assertAll(() ->
        {
            DesktopBrowserInfo desktopInfo = (DesktopBrowserInfo) infos.get(0);
            assertAll(
                () -> assertEquals(RectangleSize.parse("820x640"), desktopInfo.getRenderBrowserInfo().getDeviceSize()),
                () -> assertEquals(BrowserType.CHROME, desktopInfo.getRenderBrowserInfo().getBrowserType())
            );
        }, () ->
        {
            ChromeEmulationInfo emulationInfo = (ChromeEmulationInfo) infos.get(1);
            assertAll(
                () -> assertEquals(DeviceName.iPhone_X.getName(), emulationInfo.getDeviceName()),
                () -> assertEquals(ScreenOrientation.PORTRAIT, emulationInfo.getScreenOrientation())
            );
        }, () ->
        {
            IosDeviceInfo iosInfo = (IosDeviceInfo) infos.get(2);
            assertAll(
                () -> assertEquals(IosDeviceName.iPhone_XS.getName(), iosInfo.getDeviceName()),
                () -> assertEquals(ScreenOrientation.LANDSCAPE, iosInfo.getScreenOrientation())
            );
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
        "DESKTOP                | viewportSize, browser",
        "IOS                    | deviceName, screenOrientation, version",
        "CHROME_MOBILE_EMULATION| deviceName, screenOrientation"
    }, delimiter = '|')
    void shouldFailConversionOnUnknownParameters(String profile, String supportedOptions)
    {
        String table = "|profile|option1|option2|empty|" + System.lineSeparator()
                     + "|%s     |value1 |value2 |     |" + System.lineSeparator();

        ExamplesTable examplesTable = createTable(String.format(table, profile));
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> converter.convertValue(examplesTable, null));
        assertEquals(String.format("The %s profile supports only [%s] options, but got [option1, option2]", profile,
                supportedOptions), thrown.getMessage());
    }

    @Test
    void shouldFailOnUnknownDevice()
    {
        String table = "|profile|deviceName|" + System.lineSeparator()
                     + "|iOS    |Nokia 3310|";
        ExamplesTable examplesTable = createTable(table);
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> converter.convertValue(examplesTable, null));
        assertEquals("Unknown device name: Nokia 3310", thrown.getMessage());
    }

    private static ExamplesTable createTable(String table)
    {
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(new FluentEnumConverter());
        return new ExamplesTableFactory(new Keywords(), null, parameterConverters, new ParameterControls(),
                new TableParsers(parameterConverters), new TableTransformers()).createExamplesTable(table);
    }
}

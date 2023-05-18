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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.BrowserType;
import com.applitools.eyes.visualgrid.model.ChromeEmulationInfo;
import com.applitools.eyes.visualgrid.model.DesktopBrowserInfo;
import com.applitools.eyes.visualgrid.model.DeviceName;
import com.applitools.eyes.visualgrid.model.IRenderingBrowserInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceInfo;
import com.applitools.eyes.visualgrid.model.IosDeviceName;
import com.applitools.eyes.visualgrid.model.IosVersion;
import com.applitools.eyes.visualgrid.model.ScreenOrientation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;

public class ExamplesTableToRenderingBrowserInfoConverter
        extends AbstractParameterConverter<ExamplesTable, IRenderingBrowserInfo[]>
{
    private static final String PROFILE = "profile";
    private static final String SCREEN_ORIENTATION = "screenOrientation";
    private static final String DEVICE_NAME = "deviceName";

    @Override
    public IRenderingBrowserInfo[] convertValue(ExamplesTable table, Type type)
    {
        return table.getRowsAsParameters(true).stream()
                .map(params -> {
                    Profile profile = params.valueAs(PROFILE, Profile.class);
                    checkUnsupportedKeys(profile, params);
                    return profile.create(params);
                })
                .toArray(IRenderingBrowserInfo[]::new);
    }

    private static void checkUnsupportedKeys(Profile profile, Parameters params)
    {
        List<String> supportedOptions = profile.getSupportedOptions();
        List<String> unsupportedOptions = params.values().entrySet()
                .stream()
                .filter(e -> !supportedOptions.contains(e.getKey()) && StringUtils.isNoneBlank(e.getValue())
                        && !PROFILE.equals(e.getKey()))
                .map(Entry::getKey)
                .collect(Collectors.toList());

        Validate.isTrue(unsupportedOptions.isEmpty(), "The %s profile supports only %s options, but got %s",
                profile, supportedOptions, unsupportedOptions);
    }

    private enum Profile
    {
        DESKTOP
        {
            private static final String VIEWPORT_SIZE = "viewportSize";
            private static final String BROWSER = "browser";

            @Override
            IRenderingBrowserInfo create(Parameters params)
            {
                return new DesktopBrowserInfo(
                    RectangleSize.parse(params.valueAs(VIEWPORT_SIZE, String.class)),
                    params.valueAs(BROWSER, BrowserType.class)
                );
            }

            @Override
            List<String> getSupportedOptions()
            {
                return List.of(VIEWPORT_SIZE, BROWSER);
            }
        },
        IOS
        {
            private static final String VERSION = "version";

            @Override
            IRenderingBrowserInfo create(Parameters params)
            {
                return new IosDeviceInfo(
                    getDeviceName(params, IosDeviceName::fromName),
                    getScreenOrientation(params),
                    params.valueAs(VERSION, IosVersion.class, null)
                );
            }

            @Override
            List<String> getSupportedOptions()
            {
                return List.of(DEVICE_NAME, SCREEN_ORIENTATION, VERSION);
            }
        },
        CHROME_MOBILE_EMULATION
        {
            @Override
            IRenderingBrowserInfo create(Parameters params)
            {
                return new ChromeEmulationInfo(
                    getDeviceName(params, DeviceName::fromName),
                    getScreenOrientation(params)
                );
            }

            @Override
            List<String> getSupportedOptions()
            {
                return List.of(DEVICE_NAME, SCREEN_ORIENTATION);
            }
        };

        abstract IRenderingBrowserInfo create(Parameters params);

        abstract List<String> getSupportedOptions();

        private static <T> T getDeviceName(Parameters params, Function<String, T> converter)
        {
            String deviceNameAsString = params.valueAs(DEVICE_NAME, String.class);
            T deviceName = converter.apply(deviceNameAsString);
            Validate.isTrue(deviceName != null, "Unknown device name: %s", deviceNameAsString);
            return deviceName;
        }

        private static ScreenOrientation getScreenOrientation(Parameters params)
        {
            return params.valueAs(SCREEN_ORIENTATION, ScreenOrientation.class, ScreenOrientation.PORTRAIT);
        }
    }
}

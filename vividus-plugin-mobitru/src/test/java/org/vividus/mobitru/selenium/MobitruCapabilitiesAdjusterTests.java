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

package org.vividus.mobitru.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.exception.MobitruOperationException;

@ExtendWith(MockitoExtension.class)
class MobitruCapabilitiesAdjusterTests
{
    private static final String UDID = "Z3CV103D2DO";
    private static final String STEAM_APK = "steam.apk";
    private static final String APPIUM_UDID = "appium:udid";

    @Mock private MobitruFacade mobitruFacade;

    @InjectMocks private MobitruCapabilitiesAdjuster mobitruCapabilitiesConfigurer;

    @Test
    void shouldTakeDeviceInstallAnAppAndSetUdidToTheCapabilities() throws MobitruOperationException
    {
        mobitruCapabilitiesConfigurer.setAppFileName(STEAM_APK);
        var capabilities = mock(DesiredCapabilities.class);
        when(mobitruFacade.takeDevice(capabilities)).thenReturn(UDID);
        var ordered = Mockito.inOrder(mobitruFacade);
        assertEquals(Map.of(APPIUM_UDID, UDID), mobitruCapabilitiesConfigurer.getExtraCapabilities(capabilities));
        ordered.verify(mobitruFacade).takeDevice(capabilities);
        ordered.verify(mobitruFacade).installApp(UDID, STEAM_APK);
        verify(mobitruFacade, never()).returnDevice(UDID);
    }

    @ValueSource(strings = { APPIUM_UDID, "udid"})
    @ParameterizedTest
    void shouldNotReturnUdidIfItsAlreadyPresentedInCapabilities(String udidCapabilityName)
        throws MobitruOperationException
    {
        mobitruCapabilitiesConfigurer.setAppFileName(STEAM_APK);
        var capabilities = new DesiredCapabilities(Map.of(udidCapabilityName, UDID));
        when(mobitruFacade.takeDevice(capabilities)).thenReturn(UDID);
        var ordered = Mockito.inOrder(mobitruFacade);
        assertEquals(Map.of(), mobitruCapabilitiesConfigurer.getExtraCapabilities(capabilities));
        ordered.verify(mobitruFacade).takeDevice(capabilities);
        ordered.verify(mobitruFacade).installApp(UDID, STEAM_APK);
        verify(mobitruFacade, never()).returnDevice(UDID);
    }

    @Test
    void shouldWrapExceptionAndStopDeviceUsage() throws MobitruOperationException
    {
        mobitruCapabilitiesConfigurer.setAppFileName(STEAM_APK);
        var capabilities = new DesiredCapabilities();
        var exception = new MobitruOperationException(UDID);
        when(mobitruFacade.takeDevice(capabilities)).thenReturn(UDID);
        doThrow(exception).when(mobitruFacade).installApp(UDID, STEAM_APK);
        var iae = assertThrows(IllegalStateException.class, () -> mobitruCapabilitiesConfigurer.adjust(capabilities));
        verify(mobitruFacade).returnDevice(UDID);
        assertEquals(exception, iae.getCause());
    }

    @Test
    void shouldNotStopDeviceUsageIfDeviceIsUnknown() throws MobitruOperationException
    {
        var capabilities = new DesiredCapabilities();
        var exception = new MobitruOperationException(UDID);
        when(mobitruFacade.takeDevice(capabilities)).thenThrow(exception);
        var iae = assertThrows(IllegalStateException.class,
            () -> mobitruCapabilitiesConfigurer.adjust(capabilities));
        verify(mobitruFacade, never()).returnDevice(UDID);
        assertEquals(exception, iae.getCause());
    }

    @Test
    void shouldRethrowAnExceptionIfItsHappensDuringStoppage() throws MobitruOperationException
    {
        mobitruCapabilitiesConfigurer.setAppFileName(STEAM_APK);
        var capabilities = new DesiredCapabilities();
        var exception = new MobitruOperationException(UDID);
        when(mobitruFacade.takeDevice(capabilities)).thenReturn(UDID);
        doThrow(exception).when(mobitruFacade).installApp(UDID, STEAM_APK);
        var secondException = new MobitruOperationException(UDID);
        doThrow(secondException).when(mobitruFacade).returnDevice(UDID);
        var uie = assertThrows(IllegalStateException.class,
            () -> mobitruCapabilitiesConfigurer.adjust(capabilities));
        assertSame(secondException, uie.getCause());
    }
}

/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

public class MobitruSessionInfoLocalStorage implements MobitruSessionInfoStorage
{
    private static final Object KEY = MobitruSessionInfo.class;

    private final TestContext testContext;

    public MobitruSessionInfoLocalStorage(TestContext testContext)
    {
        this.testContext = testContext;
    }

    @Override
    public Optional<String> getDeviceId()
    {
        return Optional.ofNullable(getMobitruSessionInfo().getDeviceId());
    }

    @Override
    public void saveDeviceId(String deviceId)
    {
        getMobitruSessionInfo().setDeviceId(deviceId);
    }

    @Subscribe
    public void onAfterSessionStop(AfterWebDriverQuitEvent event)
    {
        testContext.remove(KEY);
    }

    private MobitruSessionInfo getMobitruSessionInfo()
    {
        return testContext.get(KEY, MobitruSessionInfo::new);
    }

    private static final class MobitruSessionInfo
    {
        private String deviceId;

        private String getDeviceId()
        {
            return deviceId;
        }

        private void setDeviceId(String deviceId)
        {
            this.deviceId = deviceId;
        }
    }
}

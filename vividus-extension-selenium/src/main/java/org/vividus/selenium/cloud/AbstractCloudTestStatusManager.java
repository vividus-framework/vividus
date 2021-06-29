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

package org.vividus.selenium.cloud;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.BeforeWebDriverQuitEvent;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.softassert.model.SoftAssertionError;
import org.vividus.testcontext.TestContext;

public abstract class AbstractCloudTestStatusManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCloudTestStatusManager.class);

    private static final Object KEY = CloudTestStatus.class;

    private final CloudTestStatusMapping testStatusMapping;
    private final IWebDriverProvider webDriverProvider;
    private final TestContext testContext;

    protected AbstractCloudTestStatusManager(CloudTestStatusMapping testStatusMapping,
            IWebDriverProvider webDriverProvider, TestContext testContext)
    {
        this.testStatusMapping = testStatusMapping;
        this.webDriverProvider = webDriverProvider;
        this.testContext = testContext;
    }

    @Subscribe
    public final void setCloudTestStatusToFailure(AssertionFailedEvent event)
    {
        if (webDriverProvider.isWebDriverInitialized() && !getCloudTestStatus().isFailed())
        {
            SoftAssertionError error = event.getSoftAssertionError();
            if (!error.isKnownIssue() || error.getKnownIssue().isFixed())
            {
                getCloudTestStatus().fail();
            }
        }
    }

    @SuppressWarnings("IllegalCatchExtended")
    @Subscribe
    public final void updateCloudTestStatus(BeforeWebDriverQuitEvent event)
    {
        String status = getCloudTestStatus().isFailed() ? testStatusMapping.getFailed() : testStatusMapping.getPassed();
        try
        {
            updateCloudTestStatus(status);
        }
        catch (Exception e)
        {
            LOGGER.atError().addArgument(status)
                            .setCause(e)
                            .log("Unable to update cloud test status to {}");
        }
        finally
        {
            testContext.remove(KEY);
        }
    }

    private CloudTestStatus getCloudTestStatus()
    {
        return testContext.get(KEY, CloudTestStatus::new);
    }

    protected abstract void updateCloudTestStatus(String status) throws UpdateCloudTestStatusException;

    protected String getSessionId()
    {
        return webDriverProvider.getUnwrapped(RemoteWebDriver.class).getSessionId().toString();
    }

    private final class CloudTestStatus
    {
        private boolean failed;

        public boolean isFailed()
        {
            return failed;
        }

        public void fail()
        {
            this.failed = true;
        }
    }

    public static final class UpdateCloudTestStatusException extends Exception
    {
        private static final long serialVersionUID = -4333639036297482018L;

        public UpdateCloudTestStatusException(Throwable cause)
        {
            super(cause);
        }
    }
}

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

package org.vividus.bdd.mobileapp.steps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IGenericWebDriverManager;

public class IOSInstrumentsSteps
{
    private static final String PROFILE_NAME = "profileName";

    private IWebDriverProvider webDriverProvider;
    private IGenericWebDriverManager webDriverManager;

    public IOSInstrumentsSteps(IWebDriverProvider webDriverProvider, IGenericWebDriverManager webDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
    }

    /**
     * Start recording metrics of specified IOS instrument.
     * @param instrument instrument to record data
     */
    @When("I start recording '$instrument' metrics")
    public void startRecordingOfInstrument(String instrument)
    {
        checkIOS();
        Map<String, Object> args = new HashMap<>();
        args.put("pid", "current");
        args.put(PROFILE_NAME, instrument);

        getWebDriver().executeScript("mobile: startPerfRecord", args);
    }

    /**
     * Stop recording metrics of specified IOS instrument and save results to <b>path</b>
     * @param instrument instrument to record data
     * @param path Path to the location for saving the screenshot
     * @throws IOException If an input or output exception occurred
     */
    @When("I stop recording '$instrument' metrics and save data to '$path'")
    public void stopRecordingInstrumentData(String instrument, String path) throws IOException
    {
        checkIOS();
        Map<String, Object> args = new HashMap<>();
        args.put(PROFILE_NAME, instrument);

        File traceZip = new File(path);
        String b64Zip = (String) getWebDriver().executeScript("mobile: stopPerfRecord", args);
        byte[] bytesZip = Base64.getMimeDecoder().decode(b64Zip);

        try (FileOutputStream stream = new FileOutputStream(traceZip))
        {
            stream.write(bytesZip);
        }
    }

    private void checkIOS()
    {
        if (!webDriverManager.isIOS())
        {
            throw new IllegalStateException("Step is only supported on IOS devices");
        }
    }

    private RemoteWebDriver getWebDriver()
    {
        return webDriverProvider.getUnwrapped(RemoteWebDriver.class);
    }
}

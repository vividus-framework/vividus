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

package org.vividus.mobileapp.steps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.vividus.selenium.manager.IGenericWebDriverManager;
import org.vividus.ui.action.JavascriptActions;

public class IOSPerformanceSteps
{
    private static final String PROFILE_NAME = "profileName";

    private final JavascriptActions javascriptActions;
    private final IGenericWebDriverManager webDriverManager;

    public IOSPerformanceSteps(JavascriptActions javascriptActions, IGenericWebDriverManager webDriverManager)
    {
        this.javascriptActions = javascriptActions;
        this.webDriverManager = webDriverManager;
    }

    /**
     * Start recording metrics of specified Xcode instrument.
     * <br>
     * List of some useful instruments: Time Profiler, Leaks, File Activity, etc.
     * <br>
     * See <a href="https://appiumpro.com/editions/12-capturing-performance-data-for-native-ios-apps">
     * Capturing Performance Data for Native iOS Apps</a> and
     * <a href="https://developer.apple.com/videos/play/wwdc2019/411/">Getting Started with Instruments</a>
     * for more info
     * <br>
     * @param instrument instrument to record data
     */
    @When("I start recording `$instrument` metrics")
    public void startRecordingOfInstrument(String instrument)
    {
        checkIOS();
        Map<String, Object> args = Map.of("pid", "current", PROFILE_NAME, instrument);

        javascriptActions.executeScript("mobile: startPerfRecord", args);
    }

    /**
     * Stop recording metrics of specified IOS instrument and save results to <b>path</b>
     * <br>
     * List of some useful instruments: Time Profiler, Leaks, File Activity, etc.
     * <br>
     * Result will be an archive with *.trace file which could be opened by Xcode.
     * @param instrument instrument to record data
     * @param path path to the file to save an archive with collected data
     * @throws IOException If an input or output exception occurred
     */
    @When("I stop recording `$instrument` metrics and save results to file `$path`")
    public void stopRecordingInstrumentData(String instrument, Path path) throws IOException
    {
        checkIOS();
        Map<String, Object> args = Map.of(PROFILE_NAME, instrument);

        String b64Zip = (String) javascriptActions.executeScript("mobile: stopPerfRecord", args);
        byte[] bytesZip = Base64.getMimeDecoder().decode(b64Zip);

        Files.write(path, bytesZip);
    }

    private void checkIOS()
    {
        Validate.isTrue(webDriverManager.isIOS(), "The functionality is not supported for non-IOS applications");
    }
}

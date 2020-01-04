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

package org.vividus.selenium;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WebDriverConfiguration
{
    private Optional<String> driverExecutablePath = Optional.empty();
    private Optional<String> binaryPath = Optional.empty();
    private String[] commandLineArguments = {};
    private Map<String, Object> experimentalOptions = new HashMap<>();

    public Optional<String> getDriverExecutablePath()
    {
        return driverExecutablePath;
    }

    public void setDriverExecutablePath(Optional<String> driverExecutablePath)
    {
        this.driverExecutablePath = driverExecutablePath;
    }

    public Optional<String> getBinaryPath()
    {
        return binaryPath;
    }

    public void setBinaryPath(Optional<String> binaryPath)
    {
        this.binaryPath = binaryPath;
    }

    public String[] getCommandLineArguments()
    {
        return copy(commandLineArguments);
    }

    public void setCommandLineArguments(String[] commandLineArguments)
    {
        this.commandLineArguments = copy(commandLineArguments);
    }

    public Map<String, Object> getExperimentalOptions()
    {
        return experimentalOptions;
    }

    public void setExperimentalOptions(Map<String, Object> experimentalOptions)
    {
        this.experimentalOptions = experimentalOptions;
    }

    private String[] copy(String[] toCopy)
    {
        return Arrays.copyOf(toCopy, toCopy.length);
    }
}

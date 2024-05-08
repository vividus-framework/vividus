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

package org.vividus.steps.ui.web;

import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.variable.VariableScope;

@TakeScreenshotOnFailure
public class ChromeExperimentalSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ChromeExperimentalSteps.class);

    private static final String CHROME_DOWNLOADS_PAGE = "chrome://downloads";

    private final WebJavascriptActions javascriptActions;
    private final IWebDriverManager webDriverManager;
    private final ISoftAssert softAssert;
    private final VariableContext variableContext;
    private final IWaitActions waitActions;
    private final PageSteps pageSteps;
    private final WindowSteps windowSteps;

    private Duration fileDownloadTimeout;

    public ChromeExperimentalSteps(WebJavascriptActions javascriptActions, IWebDriverManager webDriverManager,
                                   ISoftAssert softAssert, VariableContext variableContext, IWaitActions waitActions,
                                   PageSteps pageSteps, WindowSteps windowSteps)
    {
        this.javascriptActions = javascriptActions;
        this.webDriverManager = webDriverManager;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
        this.waitActions = waitActions;
        this.pageSteps = pageSteps;
        this.windowSteps = windowSteps;
    }

    /**
     * <b>Warning!</b> This step can be used only for <i>desktop/chrome</i> profile. <br>
     * Downloads the file from the website and saves its content to the specified variable.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Switch the browser to the download manager page;
     * <li>Search for the latest downloaded file with a name that matches the provided regex;
     * <li>Wait until the file download is complete;
     * <li>Store file content into a variable with the specific scope;
     * <li>Close the download manager page and return to the previously opened page;
     * </ul>
     * @param regex Regular expression to filter downloaded files by name.
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     * scopes.<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName The variable name to store the path to the temporary file with the file content.
     */
    @When("I download file with name matching `$regex` from browser downloads"
            + " and save its content to $scopes variable `$variableName`")
    public void downloadFile(String regex, Set<VariableScope> scopes, String variableName)
    {
        Validate.isTrue(webDriverManager.isBrowserAnyOf(Browser.CHROME),
                "The step is supported only on Chrome browser.");
        pageSteps.openPageInNewTab(CHROME_DOWNLOADS_PAGE);
        try
        {
            Optional<String> filePathOpt = searchForFile(regex);
            if (filePathOpt.isEmpty())
            {
                List<String> downloadedFiles = javascriptActions
                        .executeScriptFromResource(ChromeExperimentalSteps.class, "chrome-download-files-list.js");
                String errorMessage = downloadedFiles.isEmpty() ? "There are no files on the browser downloads page"
                        : "Unable to find any file matching regex [" + regex
                                + "] among files on the browser downloads page: " + String.join(", ", downloadedFiles);
                softAssert.recordFailedAssertion(errorMessage);
                return;
            }
            String filePath = filePathOpt.get();
            LOGGER.atInfo().addArgument(() -> FilenameUtils.getName(filePath))
                    .log("Waiting for the {} file to download");
            if (!isFileDownloadComplete(filePath))
            {
                return;
            }
            LOGGER.info("Download for the {} file is completed", filePath);
            String content = getFileContent(filePath);
            variableContext.putVariable(scopes, variableName, Base64.getDecoder().decode(content));
        }
        finally
        {
            windowSteps.closeCurrentTab();
        }
    }

    private String getFileContent(String filePath)
    {
        WebElement input = (WebElement) javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class,
                "chrome-download-create-input.js");
        input.sendKeys(filePath);

        String content = javascriptActions.executeAsyncScriptFromResource(ChromeExperimentalSteps.class,
                "chrome-download-get-file-content.js", input);
        return StringUtils.substringAfter(content, "base64,");
    }

    private boolean isFileDownloadComplete(String filePath)
    {
        return waitActions.wait(javascriptActions, fileDownloadTimeout,
                js -> js.executeScriptFromResource(ChromeExperimentalSteps.class, "chrome-download-wait.js", filePath),
                true).isWaitPassed();
    }

    private Optional<String> searchForFile(String fileNameRegex)
    {
        String filePath = javascriptActions.executeScriptFromResource(ChromeExperimentalSteps.class,
                "chrome-download-search-file.js", fileNameRegex);
        return Optional.ofNullable(filePath);
    }

    public void setFileDownloadTimeout(Duration fileDownloadTimeout)
    {
        this.fileDownloadTimeout = fileDownloadTimeout;
    }
}

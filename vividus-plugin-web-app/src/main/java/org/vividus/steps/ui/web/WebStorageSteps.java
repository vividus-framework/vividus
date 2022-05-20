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

package org.vividus.steps.ui.web;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.storage.StorageType;
import org.vividus.ui.web.storage.WebStorageManager;
import org.vividus.variable.VariableScope;

public class WebStorageSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WebStorageSteps.class);

    private final WebStorageManager webStorageManager;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public WebStorageSteps(WebStorageManager webStorageManager, VariableContext variableContext, ISoftAssert softAssert)
    {
        this.webStorageManager = webStorageManager;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Saves the value of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API">web storage</a>
     * item to the variable.
     *
     * @param storageType  One of the web storage mechanisms: either "local" or "session".
     * @param key          The name of the key to retrieve the value of.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to store the value of the web storage item.
     */
    @When("I save $storageType storage item with key `$key` to $scopes variable `$variableName`")
    public void saveWebStorageItemToVariable(StorageType storageType, String key, Set<VariableScope> scopes,
            String variableName)
    {
        variableContext.putVariable(scopes, variableName, webStorageManager.getStorage(storageType).getItem(key));
    }

    /**
     * Adds the item with the specified key-value pair to the
     * <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API">web storage</a>, or updates that key's
     * value if it already exists.
     *
     * @param storageType One of the web storage mechanisms: either "local" or "session".
     * @param key         The name of the key to create/update.
     * @param value       The value to give the key that is creating/updating.
     */
    @When("I set $storageType storage item with key `$key` and value `$value`")
    public void setWebStorageItem(StorageType storageType, String key, String value)
    {
        LOGGER.info("Setting {} storage item with key '{}' and value '{}", storageType, key, value);
        webStorageManager.getStorage(storageType).setItem(key, value);
    }

    /**
     * Validates the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API">web storage</a>
     * item with the specified key exists.
     *
     * @param storageType One of the web storage mechanisms: either "local" or "session".
     * @param key         The name of the key to check presence.
     */
    @Then("$storageType storage item with key `$key` exists")
    public void assertWebStorageItemExists(StorageType storageType, String key)
    {
        assertWebStorageItem(storageType, key, notNullValue());
    }

    /**
     * Validates the <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API">web storage</a>
     * item with the specified key does not exist.
     *
     * @param storageType One of the web storage mechanisms: either "local" or "session".
     * @param key         The name of the key to check absence.
     */
    @Then("$storageType storage item with key `$key` does not exist")
    public void assertWebStorageItemDoesNotExist(StorageType storageType, String key)
    {
        assertWebStorageItem(storageType, key, nullValue());
    }

    private void assertWebStorageItem(StorageType storageType, String key, Matcher<Object> matcher)
    {
        softAssert.assertThat(String.format("%s storage item with key '%s'", storageType, key),
                webStorageManager.getStorage(storageType).getItem(key), matcher);
    }
}

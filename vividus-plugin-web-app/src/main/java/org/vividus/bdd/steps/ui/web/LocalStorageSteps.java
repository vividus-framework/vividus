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

package org.vividus.bdd.steps.ui.web;

import java.util.Set;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.storage.ILocalStorageManager;

public class LocalStorageSteps
{
    @Inject private ILocalStorageManager localStorageManager;
    @Inject private IBddVariableContext bddVariableContext;
    @Inject private ISoftAssert softAssert;

    /**
     * Saves local storage item to scope variable;
     * @param key of local storage item
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variable to save
     */
    @When("I save local storage item with key `$key` to $scopes variable `$variable`")
    public void saveLocalStorageItemByKey(String key, Set<VariableScope> scopes, String variable)
    {
        bddVariableContext.putVariable(scopes, variable, localStorageManager.getItem(key));
    }

    /**
     * Asserts that local storage item with key does not exist
     * @param key of local storage item
     */
    @Then("local storage item with `$key` does not exist")
    public void checkLocalStorageItemDoesNotExist(String key)
    {
        softAssert.assertThat("Local storage item with key: " + key,
                localStorageManager.getItem(key), Matchers.nullValue());
    }
}

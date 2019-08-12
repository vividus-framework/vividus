/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.report.allure;

import java.util.List;

import org.vividus.bdd.report.allure.model.ScenarioExecutionStage;
import org.vividus.bdd.report.allure.model.StoryExecutionStage;

import io.qameta.allure.model.Label;

public interface IAllureRunContext
{
    List<Label> createNewStoryLabels(boolean givenStory);

    List<Label> getCurrentStoryLabels();

    List<Label> getRootStoryLabels();

    void resetCurrentStoryLabels(boolean givenStory);

    void initExecutionStages();

    void resetExecutionStages();

    StoryExecutionStage getStoryExecutionStage();

    void setStoryExecutionStage(StoryExecutionStage stage);

    ScenarioExecutionStage getScenarioExecutionStage();

    void setScenarioExecutionStage(ScenarioExecutionStage stage);

    void resetScenarioExecutionStage();
}

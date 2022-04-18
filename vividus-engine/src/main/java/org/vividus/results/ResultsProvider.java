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

package org.vividus.results;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.ExitCode;
import org.vividus.results.model.Failure;
import org.vividus.results.model.Statistic;

public interface ResultsProvider
{
    ExitCode calculateExitCode();

    Map<ExecutableEntity, Statistic> getStatistics();

    Optional<List<Failure>> getFailures();

    /**
     * Duration of the test run excluding time taken by @BeforeStories and @AfterStories hooks.
     *
     * @return duration of the test run
     */
    Duration getDuration();
}

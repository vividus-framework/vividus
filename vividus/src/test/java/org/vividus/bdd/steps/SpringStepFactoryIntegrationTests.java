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

package org.vividus.bdd.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration("classpath:/org/vividus/bdd/test-spring.xml")
class SpringStepFactoryIntegrationTests
{
    @Autowired
    private SpringStepFactory springStepFactory;

    @Test
    void customStepsInitializationTest()
    {
        List<Class<?>> list = springStepFactory.stepsTypes();
        assertEquals(2, list.size());
        assertThat(list, Matchers.containsInAnyOrder(DefaultSteps.class, SuperSteps.class));
    }

    static class DefaultSteps
    {
    }

    static class SuperSteps
    {
    }

    static class ShouldNotAppearSteps
    {
    }
}

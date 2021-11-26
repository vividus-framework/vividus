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

package org.vividus;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.vividus.log.AllureLogAppender;
import org.vividus.report.allure.AllureStoryReporter;

public class ContextStartedEventListener implements ApplicationListener<ContextStartedEvent>
{
    @Override
    public void onApplicationEvent(ContextStartedEvent event)
    {
        ApplicationContext applicationContext = event.getApplicationContext();

        AllureLogAppender allureLogAppender = AllureLogAppender.getInstance();
        allureLogAppender.setAllureStoryReporter(applicationContext.getBean(AllureStoryReporter.class));
    }
}

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

package org.vividus.report.allure.notification;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.util.property.IPropertyMapper;

import guru.qa.allure.notifications.clients.Notification;
import guru.qa.allure.notifications.config.Config;
import guru.qa.allure.notifications.config.enums.Language;

public class NotificationsSender
{
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsSender.class);

    private final IPropertyMapper propertyMapper;

    public NotificationsSender(IPropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
    }

    public void sendNotifications(File reportDirectory)
    {
        parseConfiguration().ifPresent(config -> {
            config.getBase().setAllureFolder(reportDirectory.getAbsolutePath());
            config.getBase().setLanguage(Language.en);
            config.getBase().setEnableChart(true);
            Notification.send(config);
        });
    }

    private Optional<Config> parseConfiguration()
    {
        try
        {
            return propertyMapper.readValue("notifications.", Config.class);
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to parse notifications configuration", e);
            return Optional.empty();
        }
    }
}

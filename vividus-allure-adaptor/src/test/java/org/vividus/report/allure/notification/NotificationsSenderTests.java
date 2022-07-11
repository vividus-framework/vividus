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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.property.IPropertyMapper;

import guru.qa.allure.notifications.clients.Notification;
import guru.qa.allure.notifications.config.Config;
import guru.qa.allure.notifications.config.base.Base;
import guru.qa.allure.notifications.config.enums.Language;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NotificationsSenderTests
{
    private static final String PROPERTY_PREFIX = "notifications.";

    @Mock private File reportDirectory;
    @Mock private IPropertyMapper propertyMapper;
    @InjectMocks private NotificationsSender notificationsSender;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(NotificationsSender.class);

    @Test
    void shouldLogErrorOnConfigParseException() throws IOException
    {
        var ioException = new IOException();
        when(propertyMapper.readValue(PROPERTY_PREFIX, Config.class)).thenThrow(ioException);
        notificationsSender.sendNotifications(reportDirectory);
        assertThat(logger.getLoggingEvents(),
                is(List.of(error(ioException, "Unable to parse notifications configuration"))));
        verifyNoInteractions(reportDirectory);
    }

    @Test
    void shouldSendNotificationSuccessfully() throws IOException
    {
        testNotificationSending((config, notification) ->
        {
            notificationsSender.sendNotifications(reportDirectory);
            notification.verify(() -> Notification.send(config));
            assertThat(logger.getLoggingEvents(), is(List.of()));
        });
    }

    void testNotificationSending(BiConsumer<Config, MockedStatic<Notification>> test) throws IOException
    {
        var absolutePath = "/absolute/path";

        when(reportDirectory.getAbsolutePath()).thenReturn(absolutePath);

        var base = new Base();
        var config = mock(Config.class);
        when(config.getBase()).thenReturn(base);

        when(propertyMapper.readValue(PROPERTY_PREFIX, Config.class)).thenReturn(Optional.of(config));
        try (MockedStatic<Notification> notification = mockStatic(Notification.class))
        {
            test.accept(config, notification);
        }
        assertEquals(absolutePath, base.getAllureFolder());
        assertEquals(Language.en, base.getLanguage());
        assertTrue(base.getEnableChart());
    }
}

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

package org.vividus.spring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.event.ContextStartedEvent;

class StartContextListenerTests
{
    @Test
    void testOnApplicationEvent(@TempDir Path tempDir) throws IOException
    {
        Path path = tempDir.resolve("to-be-cleaned-up");
        Files.createDirectories(path);
        File file = path.toFile();
        StartContextListener startContextListener = new StartContextListener();
        startContextListener.setCleanableDirectories(List.of(file));
        startContextListener.onApplicationEvent(mock(ContextStartedEvent.class));
        assertFalse(file.exists());
    }
}

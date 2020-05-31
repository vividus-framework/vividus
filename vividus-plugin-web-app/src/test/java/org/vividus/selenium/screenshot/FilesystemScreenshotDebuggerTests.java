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

package org.vividus.selenium.screenshot;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.IIOException;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith(TestLoggerFactoryExtension.class)
class FilesystemScreenshotDebuggerTests
{
    private static final String SUFFIX = "suffix";
    private final FilesystemScreenshotDebugger filesystemScreenshotDebugger = new FilesystemScreenshotDebugger();
    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(FilesystemScreenshotDebugger.class);

    @Test
    void shouldNotSaveDebugScreenshotIfFolderNotSpecified()
    {
        filesystemScreenshotDebugger.setDebugScreenshotsLocation(Optional.empty());
        filesystemScreenshotDebugger.debug(FilesystemScreenshotDebuggerTests.class, "", null);
        assertThat(testLogger.getLoggingEvents(), empty());
    }

    @Test
    void shouldCleanUpFolder(@TempDir File debugFolder) throws IOException
    {
        filesystemScreenshotDebugger.setDebugScreenshotsLocation(Optional.of(debugFolder));
        assertThat(debugFolder.list(), is(arrayWithSize(0)));
        assertTrue(new File(debugFolder, "temp.txt").createNewFile());
        assertThat(debugFolder.list(), is(arrayWithSize(1)));
        filesystemScreenshotDebugger.cleanUp();
        assertThat(debugFolder.list(), is(arrayWithSize(0)));
    }

    @Test
    void shouldLogExceptionIfCleanUpFails(@TempDir File debugFolder)
    {
        File invalidFolder = new File(debugFolder, "/invalid");
        filesystemScreenshotDebugger.setDebugScreenshotsLocation(Optional.of(invalidFolder));
        filesystemScreenshotDebugger.cleanUp();
        assertThat(testLogger.getLoggingEvents(), is(List.of(debug("Unable to clean-up folder {}", invalidFolder))));
    }

    @Test
    void shouldSaveScreenshotIntoDebugFolder(@TempDir File debugFolder)
    {
        filesystemScreenshotDebugger.setDebugScreenshotsLocation(Optional.of(debugFolder));
        filesystemScreenshotDebugger.debug(FilesystemScreenshotDebuggerTests.class, SUFFIX,
                new BufferedImage(10, 10, 5));
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        assertThat(loggingEvents, Matchers.hasSize(1));
        LoggingEvent loggingEvent = loggingEvents.get(0);
        String message = loggingEvent.getMessage();
        assertEquals("Debug screenshot saved to {}", message);
        assertEquals(Level.DEBUG, loggingEvent.getLevel());
        assertThat(loggingEvent.getArguments().get(0).toString(), stringContainsInOrder(List.of(debugFolder.toString(),
                "FilesystemScreenshotDebuggerTests_suffix.png")));
        assertEquals(1, debugFolder.listFiles().length);
    }

    @Test
    void shouldLogIOException()
    {
        File path = new File("route_66");
        filesystemScreenshotDebugger.setDebugScreenshotsLocation(Optional.of(path));
        filesystemScreenshotDebugger.debug(FilesystemScreenshotDebuggerTests.class, "",
                new BufferedImage(10, 10, 5));
        List<LoggingEvent> loggingEvents = testLogger.getLoggingEvents();
        LoggingEvent loggingEvent = loggingEvents.get(0);
        String message = loggingEvent.getMessage();
        assertThat(loggingEvents, Matchers.hasSize(1));
        assertEquals(Level.DEBUG, loggingEvent.getLevel());
        assertEquals("Unable to save debug screenshot to {}", message);
        assertThat(loggingEvent.getArguments().get(0).toString(), stringContainsInOrder(List.of(path.toString(),
                "FilesystemScreenshotDebuggerTests_.png")));
        assertThat(loggingEvent.getThrowable().get(), is(instanceOf(IIOException.class)));
    }
}

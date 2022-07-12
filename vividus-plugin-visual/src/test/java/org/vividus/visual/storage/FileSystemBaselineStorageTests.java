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

package org.vividus.visual.storage;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.hamcrest.Matchers;
import org.hamcrest.io.FileMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.vividus.resource.ResourceLoadException;
import org.vividus.util.ResourceUtils;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@ExtendWith(TestLoggerFactoryExtension.class)
class FileSystemBaselineStorageTests
{
    private static final String BASELINE = "baseline";
    private static final File BASELINES_FOLDER = new File("./baselines");
    private static final String DEFAULT_EXTENSION = ".png";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FileSystemBaselineStorage.class);
    private final FileSystemBaselineStorage fileSystemBaselineStorage = new FileSystemBaselineStorage();

    @Test
    void shouldLoadBaselineFromFileSystem() throws IOException
    {
        fileSystemBaselineStorage.setBaselinesFolder(BASELINES_FOLDER);
        BufferedImage baseline = loadBaseline();
        assertThat(fileSystemBaselineStorage.getBaseline(BASELINE).get().getImage(), ImageTool.equalImage(baseline));
    }

    @Test
    void shouldReturnEmptyImageForMissingBaseline(@TempDir File baselineFolder) throws IOException
    {
        fileSystemBaselineStorage.setBaselinesFolder(baselineFolder);
        String baselineName = "missing_baseline";
        assertEquals(fileSystemBaselineStorage.getBaseline(baselineName), Optional.empty());
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Unable to find a baseline at the path: {}",
                baselineFolder.toPath().resolve(baselineName + DEFAULT_EXTENSION).toFile()))));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNotExistingFolder()
    {
        fileSystemBaselineStorage.setBaselinesFolder(new File("no_such_folder"));
        assertThrows(IllegalArgumentException.class, () -> fileSystemBaselineStorage.getBaseline(BASELINE));
    }

    @Test
    void shouldThrowExceptionWhenBaselineNotLoaded() throws IOException
    {
        fileSystemBaselineStorage.setBaselinesFolder(BASELINES_FOLDER);
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () -> fileSystemBaselineStorage.getBaseline("corrupted_image"));
        assertThat(exception.getMessage(), Matchers.matchesRegex("The baseline at the path "
                + "'.+[\\\\/]baselines[\\\\/]corrupted_image.png' is broken or has unsupported format"));
    }

    @Test
    void shouldSaveBaselineIntoFolder(@TempDir File folder) throws IOException
    {
        fileSystemBaselineStorage.setBaselinesFolder(folder);
        Screenshot screenshot = mock(Screenshot.class);
        BufferedImage baseline = loadBaseline();
        when(screenshot.getImage()).thenReturn(baseline);
        fileSystemBaselineStorage.saveBaseline(screenshot, BASELINE);
        File baselineFile = new File(folder, BASELINE + DEFAULT_EXTENSION);
        assertThat(baselineFile, FileMatchers.anExistingFile());
        assertThat(logger.getLoggingEvents(), is(List.of(info("Baseline saved to: {}",
                baselineFile.getAbsolutePath()))));
        assertThat(ImageIO.read(baselineFile), ImageTool.equalImage(baseline));
    }

    private BufferedImage loadBaseline()
    {
        try
        {
            return ImageIO.read(
                    ResourceUtils.loadFile(FileSystemBaselineStorageTests.class, "/baselines/baseline.png"));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}

/*
 * Copyright 2019-2025 the original author or authors.
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.resource.ResourceLoadException;
import org.vividus.util.ResourceUtils;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@ExtendWith(TestLoggerFactoryExtension.class)
class FileSystemBaselineStorageTests
{
    private static final String BASELINE = "baseline";
    private static final String BASELINE_PATH = "/path1/path2/" + BASELINE;
    private static final File BASELINES_FOLDER = new File("./baselines");
    private static final String DEFAULT_EXTENSION = ".png";
    private static final String SAVE_BASELINE_LOG_MESSAGE = "Baseline saved to: {}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FileSystemBaselineStorage.class);

    private FileSystemBaselineStorage createStorage(File baseline, File delta)
    {
        return new FileSystemBaselineStorage(baseline, delta);
    }

    @Test
    void shouldLoadBaselineFromFileSystem() throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(BASELINES_FOLDER, null);
        BufferedImage baseline = loadBaseline();
        assertThat(storage.getBaseline(BASELINE).get().getImage(), ImageTool.equalImage(baseline));
    }

    @Test
    void shouldReturnEmptyImageForMissingBaseline(@TempDir File baselineFolder) throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(baselineFolder, null);
        String baselineName = "missing_baseline";
        assertEquals(storage.getBaseline(baselineName), Optional.empty());
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Unable to find a baseline at the path: {}",
                baselineFolder.toPath().resolve(baselineName + DEFAULT_EXTENSION).toFile()))));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNotExistingFolder()
    {
        FileSystemBaselineStorage storage = createStorage(new File("no_such_folder"), null);
        assertThrows(IllegalArgumentException.class, () -> storage.getBaseline(BASELINE));
    }

    @Test
    void shouldThrowExceptionWhenBaselineNotLoaded() throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(BASELINES_FOLDER, null);
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () -> storage.getBaseline("corrupted_image"));
        assertThat(exception.getMessage(), Matchers.matchesRegex("The baseline at the path "
                + "'.+[\\\\/]baselines[\\\\/]corrupted_image.png' is broken or has unsupported format"));
    }

    @ParameterizedTest
    @ValueSource(strings = { BASELINE, BASELINE_PATH })
    void shouldSaveBaselineIntoFolder(String baseline, @TempDir File folder) throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(folder, null);
        Screenshot screenshot = mock(Screenshot.class);
        BufferedImage baselineImage = loadBaseline();
        when(screenshot.getImage()).thenReturn(baselineImage);
        storage.saveBaseline(screenshot, baseline);
        File baselineFile = new File(folder, baseline + DEFAULT_EXTENSION);
        assertThat(baselineFile, FileMatchers.anExistingFile());
        assertThat(logger.getLoggingEvents(), is(List.of(info(SAVE_BASELINE_LOG_MESSAGE,
                baselineFile.getAbsolutePath()))));
        assertThat(ImageIO.read(baselineFile), ImageTool.equalImage(baselineImage));
        logger.clear();

        storage.saveBaseline(screenshot, baseline);
        assertThat(ImageIO.read(baselineFile), ImageTool.equalImage(baselineImage));
        assertThat(logger.getLoggingEvents(), is(List.of(info(SAVE_BASELINE_LOG_MESSAGE,
                baselineFile.getAbsolutePath()))));
    }

    @ParameterizedTest
    @ValueSource(strings = { BASELINE, BASELINE_PATH })
    void shouldSaveBaselineIntoDeltaFolderWhenConfigured(String baseline, @TempDir File baselinesFolder,
            @TempDir File deltaFolder) throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(baselinesFolder, deltaFolder);
        Screenshot screenshot = mock(Screenshot.class);
        BufferedImage baselineImage = loadBaseline();
        when(screenshot.getImage()).thenReturn(baselineImage);
        storage.saveDelta(screenshot, baseline);

        File baselineFileInDelta = new File(deltaFolder, baseline + DEFAULT_EXTENSION);
        File baselineFileInBaselines = new File(baselinesFolder, baseline + DEFAULT_EXTENSION);

        assertThat(baselineFileInDelta, FileMatchers.anExistingFile());
        assertThat(baselineFileInBaselines, Matchers.not(FileMatchers.anExistingFile()));
        assertThat(ImageIO.read(baselineFileInDelta), ImageTool.equalImage(baselineImage));
        assertThat(logger.getLoggingEvents(), is(List.of(info(SAVE_BASELINE_LOG_MESSAGE,
                baselineFileInDelta.getAbsolutePath()))));
    }

    @Test
    void shouldSaveBaselineIntoBaselineFolderWhenDeltaIsNotConfigured(@TempDir File baselinesFolder) throws IOException
    {
        FileSystemBaselineStorage storage = createStorage(baselinesFolder, null);
        Screenshot screenshot = mock(Screenshot.class);
        BufferedImage baselineImage = loadBaseline();
        when(screenshot.getImage()).thenReturn(baselineImage);
        storage.saveDelta(screenshot, BASELINE);

        File baselineFileInBaselines = new File(baselinesFolder, BASELINE + DEFAULT_EXTENSION);

        assertThat(ImageIO.read(baselineFileInBaselines), ImageTool.equalImage(baselineImage));
        assertThat(logger.getLoggingEvents(), is(List.of(info(SAVE_BASELINE_LOG_MESSAGE,
                baselineFileInBaselines.getAbsolutePath()))));
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

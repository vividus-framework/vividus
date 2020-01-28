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

package org.vividus.visual.engine;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.vividus.bdd.resource.ResourceLoadException;
import org.vividus.util.ResourceUtils;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith(TestLoggerFactoryExtension.class)
class FileSystemBaselineRepositoryTests
{
    private static final String BASELINE = "baseline";
    private static final File BASELINES_FOLDER = new File("./baselines");
    private static final String DEFAULT_EXTENSION = ".png";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FileSystemBaselineRepository.class);
    private final FileSystemBaselineRepository fileSystemBaselineRepository = new FileSystemBaselineRepository();

    @Test
    void shouldLoadBaselineFromFileSystem() throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(BASELINES_FOLDER);
        BufferedImage baseline = loadBaseline();
        assertThat(fileSystemBaselineRepository.getBaseline(BASELINE).get().getImage(), ImageTool.equalImage(baseline));
    }

    @Test
    void shouldReturnEmptyImageForMissingBaseline(@TempDir File baselineFolder) throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(baselineFolder);
        String baselineName = "missing_baseline";
        assertEquals(fileSystemBaselineRepository.getBaseline(baselineName), Optional.empty());
        assertThat(logger.getLoggingEvents(), is(List.of(warn("Unable to find a baseline at the path: {}",
                baselineFolder.toPath().resolve(baselineName + DEFAULT_EXTENSION).toFile()))));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNotExistingFoler()
    {
        fileSystemBaselineRepository.setBaselinesFolder(new File("no_such_folder"));
        assertThrows(IllegalArgumentException.class, () -> fileSystemBaselineRepository.getBaseline(BASELINE));
    }

    @Test
    void shouldThrowExceptionWhenBaselineNotLoaded()
    {
        fileSystemBaselineRepository.setBaselinesFolder(BASELINES_FOLDER);
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () -> fileSystemBaselineRepository.getBaseline("corrupted_image").get().getImage());
        assertThat(exception.getMessage(), Matchers.matchesRegex("The baseline at the path "
                + "'.+[\\\\/]baselines[\\\\/]corrupted_image.png' is broken or has unsupported format"));
    }

    @Test
    void shouldSaveBaselineIntoFolder(@TempDir File folder) throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(folder);
        Screenshot screenshot = mock(Screenshot.class);
        BufferedImage baseline = loadBaseline();
        when(screenshot.getImage()).thenReturn(baseline);
        fileSystemBaselineRepository.saveBaseline(screenshot, BASELINE);
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
                    ResourceUtils.loadFile(FileSystemBaselineRepositoryTests.class, "/baselines/baseline.png"));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}

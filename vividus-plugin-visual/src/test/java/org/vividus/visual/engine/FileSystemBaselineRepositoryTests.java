/*
 * Copyright 2019 the original author or authors.
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith(TestLoggerFactoryExtension.class)
@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
class FileSystemBaselineRepositoryTests
{
    private static final String BASELINE = "BASELINE";

    private static final File BASELINES_FOLDER = new File("./baselines");

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FileSystemBaselineRepository.class);
    private final BufferedImage baseline = loadBaseline();
    private final FileSystemBaselineRepository fileSystemBaselineRepository = new FileSystemBaselineRepository();

    @Test
    void shouldLoadBaselineFromFileSystem() throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(BASELINES_FOLDER);
        fileSystemBaselineRepository.init();
        assertThat(fileSystemBaselineRepository.getBaseline("baseline").get().getImage(),
                ImageTool.equalImage(baseline));
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

    @Test
    void shouldReturnEmptyImageForMissingBaseline() throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(BASELINES_FOLDER);
        fileSystemBaselineRepository.init();
        assertEquals(fileSystemBaselineRepository.getBaseline("missing_baseline"), Optional.empty());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionForNotExistingFoler()
    {
        fileSystemBaselineRepository.setBaselinesFolder(new File("no_such_folder"));
        assertThrows(IllegalArgumentException.class, fileSystemBaselineRepository::init);
    }

    @Test
    void shouldThrowExceptionWhenBaselineNotLoaded()
    {
        fileSystemBaselineRepository.setBaselinesFolder(BASELINES_FOLDER);
        fileSystemBaselineRepository.init();
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () -> fileSystemBaselineRepository.getBaseline("corrupted_image").get().getImage());
        assertThat(exception.getMessage(), Matchers.matchesRegex(
                "Unable to load baseline with path: .+[\\\\/]baselines[\\\\/]corrupted_image.png"));
    }

    @Test
    void shouldSaveBaselineIntoFolder(@TempDir File folder) throws IOException
    {
        fileSystemBaselineRepository.setBaselinesFolder(folder);
        fileSystemBaselineRepository.init();
        Screenshot screenshot = mock(Screenshot.class);
        when(screenshot.getImage()).thenReturn(baseline);
        fileSystemBaselineRepository.saveBaseline(screenshot, BASELINE);
        File baselineFile = new File(folder, BASELINE + ".png");
        assertThat(baselineFile, FileMatchers.anExistingFile());
        assertThat(logger.getLoggingEvents(), is(List.of(info("Baseline saved to: {}",
                baselineFile.getAbsolutePath()))));
        assertThat(ImageIO.read(baselineFile), ImageTool.equalImage(baseline));
    }
}

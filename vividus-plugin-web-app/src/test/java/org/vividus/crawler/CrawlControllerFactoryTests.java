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

package org.vividus.crawler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;

class CrawlControllerFactoryTests
{
    @Test
    void shouldCreateCrawlController(@TempDir Path baseDirectory) throws URISyntaxException
    {
        try (MockedConstruction<SleepycatFrontierConfiguration> frontierConfig = mockConstruction(
                SleepycatFrontierConfiguration.class);
                MockedConstruction<CrawlController> crawlController = mockConstruction(CrawlController.class))
        {
            Path crawlStorage = baseDirectory.resolve("crawlStorageFolder");
            CrawlControllerFactory factory = new CrawlControllerFactory();
            factory.setCrawlStorageFolder(crawlStorage.toString());
            URI pageUri = new URI("https://example.com");

            CrawlController controller = factory.createCrawlController(pageUri);
            assertEquals(crawlController.constructed().get(0), controller);
            assertTrue(crawlStorage.toFile().exists());
        }
    }
}

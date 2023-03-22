/*
 * Copyright 2019-2023 the original author or authors.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.apache.hc.core5.net.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.PropertyMapper;

import edu.uci.ics.crawler4j.frontier.SleepycatFrontierConfiguration;

class CrawlControllerFactoryTests
{
    private static final String CRAWL_STORAGE_FOLDER_KEY = "crawl-storage-folder";
    private static final String URL = "https://example.com";

    private final IPropertyMapper propertyMapper = new PropertyMapper(".", PropertyNamingStrategies.KEBAB_CASE, null,
            Set.of());

    @Test
    void shouldCreateCrawlController(@TempDir Path baseDirectory) throws URISyntaxException
    {
        try (var frontier = mockConstruction(SleepycatFrontierConfiguration.class))
        {
            var crawlStorage = baseDirectory.resolve(CRAWL_STORAGE_FOLDER_KEY);
            Integer socketTimeout = 10_000;

            var config = Map.of(
                "socket-timeout", socketTimeout.toString(),
                CRAWL_STORAGE_FOLDER_KEY, crawlStorage.toString()
            );

            var factory = new CrawlControllerFactory(config, propertyMapper);

            var username = "username";
            var password = "password";

            var pageUri = new URIBuilder(URL).setUserInfo(username + ":" + password).build();

            var controller = factory.createCrawlController(pageUri);
            var crawlConfig = controller.getConfig();

            assertThat(frontier.constructed(), hasSize(1));
            assertEquals(crawlStorage.toString(), crawlConfig.getCrawlStorageFolder());
            assertTrue(crawlStorage.toFile().exists());
            assertEquals(socketTimeout, crawlConfig.getSocketTimeout());
            var authInfos = crawlConfig.getAuthInfos();
            assertThat(authInfos, hasSize(1));
            var authInfo = authInfos.get(0);
            assertEquals(username, authInfo.getUsername());
            assertEquals(password, authInfo.getPassword());
        }
    }

    @SuppressWarnings("try")
    @Test
    void shouldCreateCrawlControllerWithoutUserInfo(@TempDir Path baseDirectory) throws URISyntaxException
    {
        try (var ignored = mockConstruction(SleepycatFrontierConfiguration.class))
        {
            var crawlStorage = baseDirectory.resolve(CRAWL_STORAGE_FOLDER_KEY);

            var config = Map.of(CRAWL_STORAGE_FOLDER_KEY, crawlStorage.toString());

            var factory = new CrawlControllerFactory(config, propertyMapper);

            var pageUri = new URI(URL);

            var controller = factory.createCrawlController(pageUri);
            var crawlConfig = controller.getConfig();

            assertEquals(crawlStorage.toString(), crawlConfig.getCrawlStorageFolder());
            assertTrue(crawlStorage.toFile().exists());
            assertNull(crawlConfig.getAuthInfos());
        }
    }
}

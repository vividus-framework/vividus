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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.PropertyMapper;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.AuthInfo;
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
        try (MockedConstruction<SleepycatFrontierConfiguration> frontier = Mockito
                .mockConstruction(SleepycatFrontierConfiguration.class))
        {
            Path crawlStorage = baseDirectory.resolve(CRAWL_STORAGE_FOLDER_KEY);
            Integer socketTimeout = 10000;

            Map<String, String> config = Map.of(
                "socket-timeout", socketTimeout.toString(),
                CRAWL_STORAGE_FOLDER_KEY, crawlStorage.toString()
            );

            CrawlControllerFactory factory = new CrawlControllerFactory(config, propertyMapper);

            String username = "username";
            String password = "password";

            URI pageUri = new URIBuilder(URL).setUserInfo(username, password).build();

            CrawlController controller = factory.createCrawlController(pageUri);
            CrawlConfig crawlConfig = controller.getConfig();

            assertThat(frontier.constructed(), hasSize(1));
            assertEquals(crawlStorage.toString(), crawlConfig.getCrawlStorageFolder());
            assertTrue(crawlStorage.toFile().exists());
            assertEquals(socketTimeout, crawlConfig.getSocketTimeout());
            List<AuthInfo> authInfos = crawlConfig.getAuthInfos();
            assertThat(authInfos, hasSize(1));
            AuthInfo authInfo = authInfos.get(0);
            assertEquals(username, authInfo.getUsername());
            assertEquals(password, authInfo.getPassword());
        }
    }

    @Test
    void shouldCreateCrawlControllerWithoutUserInfo(@TempDir Path baseDirectory) throws URISyntaxException
    {
        try (MockedConstruction<SleepycatFrontierConfiguration> frontier = Mockito
                .mockConstruction(SleepycatFrontierConfiguration.class))
        {
            Path crawlStorage = baseDirectory.resolve(CRAWL_STORAGE_FOLDER_KEY);

            Map<String, String> config = Map.of(CRAWL_STORAGE_FOLDER_KEY, crawlStorage.toString());

            CrawlControllerFactory factory = new CrawlControllerFactory(config, propertyMapper);

            URI pageUri = new URI(URL);

            CrawlController controller = factory.createCrawlController(pageUri);
            CrawlConfig crawlConfig = controller.getConfig();

            assertEquals(crawlStorage.toString(), crawlConfig.getCrawlStorageFolder());
            assertTrue(crawlStorage.toFile().exists());
            assertNull(crawlConfig.getAuthInfos());
        }
    }
}

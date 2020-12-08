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

package org.vividus.xray.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.vividus.xray.VividusToXrayExporterApplication;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.exporter.XrayExporter;
import org.vividus.xray.facade.XrayFacade;
import org.vividus.xray.factory.TestCaseFactory;

@SpringBootTest(classes = VividusToXrayExporterApplication.class, properties = {
    "jira.endpoint=https://jira.vividus.com/",
    "xray-exporter.project-key=VIVIDUS"
})
class VividusToXrayExporterIntegrationTests
{
    @MockBean private XrayExporterOptions xrayExporterOptions;
    @SpyBean private XrayFacade xrayFacade;
    @SpyBean private TestCaseFactory testCaseFactory;
    @Autowired private XrayExporter xrayExporter;

    @Test
    void shouldStartContext(@TempDir Path tempDirectory) throws IOException
    {
        when(xrayExporterOptions.getJsonResultsDirectory()).thenReturn(tempDirectory);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> xrayExporter.exportResults());

        assertEquals("The directory '" + tempDirectory + "' does not contain needed JSON files",
                exception.getMessage());
        verifyNoInteractions(xrayFacade, testCaseFactory);
    }
}

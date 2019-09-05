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

package org.vividus.proxy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.browserup.harreader.model.Har;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.util.ResourceUtils;

@RunWith(PowerMockRunner.class)
public class HarParserTests
{
    @Test
    public void testParseHar() throws IOException
    {
        InputStream inputStream = ResourceUtils.findResource(HarParserTests.class, "/archive.har").openStream();
        HarParser parser = new HarParser(List.of());
        Har har = parser.parseHar(inputStream);
        assertEquals(1, har.getLog().getEntries().size());
    }

    @PrepareForTest(HarParser.class)
    @Test
    public void testParseHarDeserializationProblemHandlers() throws Exception
    {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        DeserializationProblemHandler problemHandler = mock(DeserializationProblemHandler.class);
        PowerMockito.whenNew(ObjectMapper.class).withAnyArguments().thenReturn(objectMapper);
        new HarParser(Arrays.asList(problemHandler, problemHandler));
        verify(objectMapper, times(2)).addHandler(problemHandler);
    }
}

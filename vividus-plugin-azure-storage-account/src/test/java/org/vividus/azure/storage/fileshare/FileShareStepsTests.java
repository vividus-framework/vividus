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

package org.vividus.azure.storage.fileshare;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareFileUploadInfo;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.StorageAccountEndpointsManager;
import org.vividus.context.VariableContext;
import org.vividus.steps.DataWrapper;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class FileShareStepsTests
{
    private static final String VARIABLE = "variable";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String FILE_PATH = "path/to/file.txt";
    private static final String SHARE_NAME = "share";
    private static final String KEY = "KEY";
    private static final String DATA = "data";
    private static final byte[] BYTES = DATA.getBytes(StandardCharsets.UTF_8);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(FileShareSteps.class);

    @Mock private StorageAccountEndpointsManager storageAccountEndpointsManager;
    @Mock private VariableContext variableContext;

    @Test
    void shouldDownloadToVariable()
    {
        runWithClient((steps, client) ->
        {
            ShareFileClient shareFileClient = mockShareFileClient(client);
            doNothing().when(shareFileClient).download(argThat(s ->
            {
                try
                {
                    s.write(BYTES);
                }
                catch (IOException e)
                {
                    throw new UncheckedIOException(e);
                }
                return true;
            }));
            steps.downloadFile(FILE_PATH, SHARE_NAME, KEY, SCOPES, VARIABLE);
            verify(variableContext).putVariable(SCOPES, VARIABLE, DATA);
        });
    }

    @Test
    void shouldDownloadToFile()
    {
        runWithClient((steps, client) ->
        {
            String baseFileName = "file";
            ShareFileClient shareFileClient = mockShareFileClient(client);
            doNothing().when(shareFileClient).download(argThat(s ->
            {
                try
                {
                    s.write(BYTES);
                }
                catch (IOException e)
                {
                    throw new UncheckedIOException(e);
                }
                return true;
            }));
            steps.downloadFile(FILE_PATH, SHARE_NAME, KEY, baseFileName, SCOPES, VARIABLE);
            verify(variableContext).putVariable(eq(SCOPES), eq(VARIABLE),
                    argThat(filename -> ((String) filename).contains(baseFileName)));
        });
    }

    @Test
    void shouldUploadFile()
    {
        runWithClient((steps, client) ->
        {
            var rootDirectoryClient = mock(ShareDirectoryClient.class);
            when(client.getRootDirectoryClient()).thenReturn(rootDirectoryClient);
            var pathDirectoryClient = mock(ShareDirectoryClient.class);
            when(rootDirectoryClient.createSubdirectoryIfNotExists("path")).thenReturn(pathDirectoryClient);
            var toDirectoryClient = mock(ShareDirectoryClient.class);
            when(pathDirectoryClient.createSubdirectoryIfNotExists("to")).thenReturn(toDirectoryClient);
            var shareFileClient = mock(ShareFileClient.class);
            when(toDirectoryClient.getFileClient("file.txt")).thenReturn(shareFileClient);
            var shareFileUploadInfo = mock(ShareFileUploadInfo.class);
            var inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
            when(shareFileClient.uploadRange(inputStreamArgumentCaptor.capture(), eq((long) BYTES.length))).thenReturn(
                    shareFileUploadInfo);
            var eTag = "eTag";
            when(shareFileUploadInfo.getETag()).thenReturn(eTag);

            steps.uploadFile(FILE_PATH, new DataWrapper(DATA), SHARE_NAME, KEY);

            assertArrayEquals(BYTES, inputStreamArgumentCaptor.getValue().readAllBytes());
            verify(shareFileClient).create(BYTES.length);
            assertThat(logger.getLoggingEvents(),
                    is(List.of(info("Upload of the data with eTag {} is completed", eTag))));
        });
    }

    @Test
    void shouldDeleteFile()
    {
        runWithClient((steps, client) ->
        {
            ShareFileClient shareFileClient = mockShareFileClient(client);
            steps.deleteFile(FILE_PATH, SHARE_NAME, KEY);
            verify(shareFileClient).delete();
            assertThat(logger.getLoggingEvents(), is(List.of(
                    info("The file with path '{}' is successfully deleted from the file share '{}'", FILE_PATH,
                            SHARE_NAME))));
        });
    }

    private void runWithStorageClient(FailableBiConsumer<FileShareSteps, ShareServiceClient, IOException> testToRun)
    {
        var shareServiceClient = mock(ShareServiceClient.class);
        var endpoint = "endpoint";
        when(storageAccountEndpointsManager.getFileServiceEndpoint(KEY)).thenReturn(endpoint);
        try (MockedConstruction<ShareServiceClientBuilder> serviceClientBuilder =
                mockConstruction(ShareServiceClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(shareServiceClient);
                }))
        {
            var steps = new FileShareSteps(storageAccountEndpointsManager, variableContext);
            testToRun.accept(steps, shareServiceClient);
            assertThat(serviceClientBuilder.constructed(), hasSize(1));
            var builder = serviceClientBuilder.constructed().get(0);
            InOrder ordered = inOrder(builder);
            ordered.verify(builder).endpoint(endpoint);
            ordered.verify(builder).buildClient();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private void runWithClient(FailableBiConsumer<FileShareSteps, ShareClient, IOException> testToRun)
    {
        runWithStorageClient((steps, shareServiceClient) -> {
            ShareClient shareClient = mock(ShareClient.class);
            when(shareServiceClient.getShareClient(SHARE_NAME)).thenReturn(shareClient);
            testToRun.accept(steps, shareClient);
        });
    }

    private ShareFileClient mockShareFileClient(ShareClient shareClient)
    {
        ShareFileClient shareFileClient = mock(ShareFileClient.class);
        when(shareClient.getFileClient(FILE_PATH)).thenReturn(shareFileClient);
        return shareFileClient;
    }
}

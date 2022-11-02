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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.blob.BlobServiceClientFactory;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class AzureBlobStorageBaselineStorageTests
{
    private static final String KEY = "key";
    private static final String CONTAINER = "container";
    private static final String BASELINE = "baseline";
    private static final byte[] IMAGE = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0,
            0, 0, 1, 8, 6, 0, 0, 0, 31, 21, -60, -119, 0, 0, 0, 13, 73, 68, 65, 84, 120, 94, 99, 96, -8, -1, -65, 30, 0,
            5, 127, 2, 126, 102, -25, 70, 104, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
    private static final String BASELINE_PNG = "baseline.png";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(AzureBlobStorageBaselineStorage.class);

    @Mock
    private BlobServiceClientFactory blobServiceClientFactory;
    @InjectMocks
    private AzureBlobStorageBaselineStorage storage;

    @BeforeEach
    void beforeEach()
    {
        storage.setContainer(CONTAINER);
        storage.setStorageAccountKey(KEY);
    }

    @Test
    void shouldGetBaselineFromAzureStorageAccount() throws IOException
    {
        var blobClient = mock(BlobClient.class);
        when(blobServiceClientFactory.createBlobClient(BASELINE_PNG, CONTAINER, KEY)).thenReturn(blobClient);
        when(blobClient.downloadContent()).thenReturn(BinaryData.fromBytes(IMAGE));
        assertArrayEquals(IMAGE, ImageTool.toByteArray(storage.getBaseline(BASELINE).get().getImage()));
    }

    @Test
    void shouldReturnEmptyBaselineIfThereIsNoSuchBlob() throws IOException
    {
        var exception = new BlobStorageException("The specified blob does not exist.", null, null);
        when(blobServiceClientFactory.createBlobClient(BASELINE_PNG, CONTAINER, KEY)).thenThrow(exception);
        assertEquals(Optional.empty(), storage.getBaseline(BASELINE_PNG));
        assertEquals(LOGGER.getLoggingEvents(), List.of(LoggingEvent.error(exception,
                "Unable to get blob with name `{}` from container `{}` in storage account with the key `{}`",
                BASELINE_PNG, CONTAINER, KEY)));
    }

    @Test
    void shouldThrowExceptionWhenItsNotHandledOne()
    {
        var exception = new BlobStorageException("The container does not exist.", null, null);
        when(blobServiceClientFactory.createBlobClient(BASELINE_PNG, CONTAINER, KEY)).thenThrow(exception);
        var actualException =
            assertThrows(BlobStorageException.class, () -> storage.getBaseline(BASELINE));
        assertSame(exception, actualException);
    }

    @Test
    void shouldSaveBaseline() throws IOException
    {
        var blobClient = mock(BlobClient.class);
        when(blobServiceClientFactory.createBlobClient(BASELINE_PNG, CONTAINER, KEY)).thenReturn(blobClient);
        Screenshot baselineScreenshot = new Screenshot(ImageTool.toBufferedImage(IMAGE));
        storage.saveBaseline(baselineScreenshot, BASELINE);
        verify(blobClient).upload(argThat((ArgumentMatcher<BinaryData>) data -> Arrays.equals(IMAGE, data.toBytes())),
                eq(true));
    }
}

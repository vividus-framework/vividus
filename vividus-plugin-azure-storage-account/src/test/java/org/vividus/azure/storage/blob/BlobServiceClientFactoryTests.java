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

package org.vividus.azure.storage.blob;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.StorageAccountEndpointsManager;

@ExtendWith(MockitoExtension.class)
class BlobServiceClientFactoryTests
{
    private static final String BLOB = "BLOB";
    private static final String CONTAINER = "container";
    private static final String KEY = "KEY";

    @Mock private StorageAccountEndpointsManager storageAccountEndpointsManager;
    @Mock private TokenCredential tokenCredential;
    @Mock private BlobServiceClient blobServiceClient;

    @InjectMocks private BlobServiceClientFactory factory;

    @Test
    void shouldCreateBlobServiceClient()
    {
        runTestWithBlobServiceClient((f, c) -> assertSame(blobServiceClient, f.createBlobStorageClient(KEY)));
    }

    @Test
    void shouldCreateBlobContainerClient()
    {
        runTestWithBlobServiceClient((f, c) -> {
            f.createBlobContainerClient(CONTAINER, KEY);
            verify(c).getBlobContainerClient(CONTAINER);
        });
    }

    @Test
    void shouldCreateBlobClient()
    {
        runTestWithBlobServiceClient((f, c) -> {
            var blobContainerClient = mock(BlobContainerClient.class);
            when(c.getBlobContainerClient(CONTAINER)).thenReturn(blobContainerClient);
            f.createBlobClient(BLOB, CONTAINER, KEY);
            verify(blobContainerClient).getBlobClient(BLOB);
        });
    }

    private void runTestWithBlobServiceClient(BiConsumer<BlobServiceClientFactory, BlobServiceClient> test)
    {
        String endpoint = "endpoint";
        when(storageAccountEndpointsManager.getBlobServiceEndpoint(KEY)).thenReturn(endpoint);
        try (MockedConstruction<BlobServiceClientBuilder> serviceClientBuilder =
                mockConstruction(BlobServiceClientBuilder.class, (mock, context) -> {
                    when(mock.credential(tokenCredential)).thenReturn(mock);
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(blobServiceClient);
                }))
        {
            test.accept(factory, blobServiceClient);
            assertThat(serviceClientBuilder.constructed(), hasSize(1));
            BlobServiceClientBuilder builder = serviceClientBuilder.constructed().get(0);
            InOrder ordered = inOrder(builder);
            ordered.verify(builder).credential(tokenCredential);
            ordered.verify(builder).endpoint(endpoint);
            ordered.verify(builder).buildClient();
        }
    }
}

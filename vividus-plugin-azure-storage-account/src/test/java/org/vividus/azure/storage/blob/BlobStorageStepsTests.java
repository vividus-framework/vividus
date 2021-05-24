/*
 * Copyright 2019-2021 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.steps.DataWrapper;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class BlobStorageStepsTests
{
    private static final String SECOND = "second";
    private static final String FIRST = "first";
    private static final String VARIABLE = "variable";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String BLOB = "BLOB";
    private static final String CONTAINER = "container";
    private static final String KEY = "KEY";
    private static final String DATA = "data";
    private static final byte[] BYTES = DATA.getBytes(StandardCharsets.UTF_8);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(BlobStorageSteps.class);

    @Mock private PropertyMappedCollection<String> storageAccountEndpoints;
    @Mock private BddVariableContext bddVariableContext;
    @Mock private JsonUtils jsonUtils;
    @Mock private DefaultAzureCredential defaultAzureCredential;

    @Test
    void shouldDownloadBlob()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            Mockito.doNothing().when(blobClient).download(argThat(s ->
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
            steps.downloadBlob(BLOB, CONTAINER, KEY, SCOPES, VARIABLE);
            verify(bddVariableContext).putVariable(SCOPES, VARIABLE, DATA);
        });
    }

    @Test
    void shouldDownloadBlobToFile()
    {
        runWithClient((steps, client) ->
        {
            String baseFileName = "blobFile";
            BlobClient blobClient = mockBlobClient(client);
            steps.downloadBlobToFile(BLOB, CONTAINER, KEY, baseFileName, SCOPES, VARIABLE);
            verify(blobClient).downloadToFile(argThat(filename -> filename.contains(baseFileName)));
            verify(bddVariableContext).putVariable(eq(SCOPES), eq(VARIABLE),
                    argThat(filename -> ((String) filename).contains(baseFileName)));
        });
    }

    @Test
    void shouldRetrieveBlobProperties()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            BlobProperties blobProperties = mock(BlobProperties.class);
            when(blobClient.getProperties()).thenReturn(blobProperties);
            String blobPropertiesAsJson = "{\"blob\":\"properties\"}";
            when(jsonUtils.toJson(blobProperties)).thenReturn(blobPropertiesAsJson);
            steps.retrieveBlobProperties(BLOB, CONTAINER, KEY, SCOPES, VARIABLE);
            verify(bddVariableContext).putVariable(SCOPES, VARIABLE, blobPropertiesAsJson);
        });
    }

    @Test
    void shouldUploadTextBlob()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            steps.uploadBlob(BLOB, new DataWrapper(DATA), CONTAINER, KEY);
            verify(blobClient).upload(argThat(data -> Arrays.equals(data.toBytes(), BYTES)));
        });
    }

    @Test
    void shouldUploadBinaryBlob()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            steps.uploadBlob(BLOB, new DataWrapper(BYTES), CONTAINER, KEY);
            verify(blobClient).upload(argThat(data -> Arrays.equals(data.toBytes(), BYTES)));
        });
    }

    @Test
    void shouldDeleteBlob()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            steps.deleteBlob(BLOB, CONTAINER, KEY);
            verify(blobClient).delete();
            assertThat(logger.getLoggingEvents(), is(List.of(
                    info("The blob with name '{}' is successfully deleted from the container '{}'", BLOB, CONTAINER))));
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldListFilteredBlobs()
    {
        runWithClient((steps, client) ->
        {
            PagedIterable<BlobItem> iterable = mock(PagedIterable.class);
            when(client.listBlobs()).thenReturn(iterable);
            BlobItem first = blobItem(FIRST);
            BlobItem second = blobItem(SECOND);
            BlobItem third = blobItem("third");
            when(iterable.stream()).thenReturn(Stream.of(first, second, third));
            steps.findBlobs(StringComparisonRule.CONTAINS, "s", CONTAINER, KEY, SCOPES, VARIABLE);
            verify(bddVariableContext).putVariable(SCOPES, VARIABLE, List.of(FIRST, SECOND));
        });
    }

    private BlobItem blobItem(String name)
    {
        BlobItem first = mock(BlobItem.class);
        when(first.getName()).thenReturn(name);
        return first;
    }

    private void runWithClient(FailableBiConsumer<BlobStorageSteps, BlobContainerClient, IOException> testToRun)
    {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        String endpoint = "endpoint";
        when(storageAccountEndpoints.get(KEY,
                "Storage account with key '%s' is not configured in properties", KEY)).thenReturn(endpoint);
        try (MockedConstruction<DefaultAzureCredentialBuilder> credentialsBuilder =
                mockConstruction(DefaultAzureCredentialBuilder.class, (mock, context) -> {
                    when(mock.build()).thenReturn(defaultAzureCredential);
                });
             MockedConstruction<BlobServiceClientBuilder> serviceClientBuilder =
                mockConstruction(BlobServiceClientBuilder.class, (mock, context) -> {
                    when(mock.credential(defaultAzureCredential)).thenReturn(mock);
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(blobServiceClient);
                }))
        {
            BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
            when(blobServiceClient.getBlobContainerClient(CONTAINER)).thenReturn(blobContainerClient);
            BlobStorageSteps steps = new BlobStorageSteps(storageAccountEndpoints, bddVariableContext, jsonUtils);
            testToRun.accept(steps, blobContainerClient);
            assertThat(credentialsBuilder.constructed(), hasSize(1));
            assertThat(serviceClientBuilder.constructed(), hasSize(1));
            BlobServiceClientBuilder builder = serviceClientBuilder.constructed().get(0);
            InOrder ordered = inOrder(builder);
            ordered.verify(builder).credential(defaultAzureCredential);
            ordered.verify(builder).endpoint(endpoint);
            ordered.verify(builder).buildClient();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private BlobClient mockBlobClient(BlobContainerClient blobContainerClient)
    {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB)).thenReturn(blobClient);
        return blobClient;
    }
}

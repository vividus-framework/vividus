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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.steps.DataWrapper;
import org.vividus.steps.StringComparisonRule;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class BlobStorageStepsTests
{
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String THIRD = "third";
    private static final String VARIABLE = "variable";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String BLOB = "BLOB";
    private static final String CONTAINER = "container";
    private static final String KEY = "KEY";
    private static final String DATA = "data";
    private static final byte[] BYTES = DATA.getBytes(StandardCharsets.UTF_8);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(BlobStorageSteps.class);

    @Mock private PropertyMappedCollection<String> storageAccountEndpoints;
    @Mock private VariableContext variableContext;
    @Mock private JsonUtils jsonUtils;
    @Mock private TokenCredential tokenCredential;

    @Test
    void shouldRetrieveBlobServiceProperties()
    {
        runWithStorageClient((steps, client) ->
        {
            BlobServiceProperties properties = mock(BlobServiceProperties.class);
            when(client.getProperties()).thenReturn(properties);
            String propertiesAsJson = "{\"blob-storage\":\"properties\"}";
            when(jsonUtils.toJson(properties)).thenReturn(propertiesAsJson);
            steps.retrieveBlobServiceProperties(KEY, SCOPES, VARIABLE);
            verify(variableContext).putVariable(SCOPES, VARIABLE, propertiesAsJson);
        });
    }

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
            verify(variableContext).putVariable(SCOPES, VARIABLE, DATA);
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
            verify(blobClient).downloadToFile(argThat(filename -> filename.contains(baseFileName)), eq(true));
            verify(variableContext).putVariable(eq(SCOPES), eq(VARIABLE),
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
            verify(variableContext).putVariable(SCOPES, VARIABLE, blobPropertiesAsJson);
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
    void shouldUpsertBlob()
    {
        runWithClient((steps, client) ->
        {
            BlobClient blobClient = mockBlobClient(client);
            steps.upsertBlob(BLOB, new DataWrapper(DATA), CONTAINER, KEY);
            verify(blobClient).upload(argThat(data -> Arrays.equals(data.toBytes(), BYTES)), eq(true));
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
            BlobItem third = blobItem(THIRD);
            when(iterable.stream()).thenReturn(Stream.of(first, second, third));
            steps.findBlobs(StringComparisonRule.CONTAINS, "s", CONTAINER, KEY, SCOPES, VARIABLE);
            verify(variableContext).putVariable(SCOPES, VARIABLE, List.of(FIRST, SECOND));
        });
    }

    static Stream<Arguments> matchingParametersToBlobs()
    {
        return Stream.of(
                arguments(Matchers.matchesRegex(".*d"), List.of(SECOND, THIRD)),
                arguments(null, List.of(FIRST, SECOND, THIRD))
        );
    }

    @ParameterizedTest
    @MethodSource("matchingParametersToBlobs")
    void shouldListFilteredBlobs(Matcher<String> blobNameMatcher, List<String> expectedBlobs)
    {
        var prefix = "datasets/";
        var filter = new BlobFilter(Optional.of(prefix), Optional.ofNullable(blobNameMatcher), Optional.empty());

        runWithClient((steps, client) ->
        {
            var blobItem1 = blobItem(FIRST);
            var blobItem2 = blobItem(SECOND);
            var blobItem3 = blobItem(THIRD);
            var blobItems = List.of(blobItem1, blobItem2, blobItem3);

            ListBlobsOptions options = testBlobsSearch(steps, client, blobItems, filter, expectedBlobs);
            assertEquals(1000, options.getMaxResultsPerPage());
            assertEquals(prefix, options.getPrefix());
        });
    }

    @Test
    void shouldListFilteredBlobsWithLimitAndWithoutPrefix()
    {
        int limit = 2;
        var filter = new BlobFilter(Optional.empty(), Optional.of(Matchers.matchesRegex(".*")), Optional.of(limit));

        runWithClient((steps, client) ->
        {
            var blobItem1 = blobItem(FIRST);
            var blobItem2 = blobItem(SECOND);
            var blobItem3 = mock(BlobItem.class);
            var blobItems = List.of(blobItem1, blobItem2, blobItem3);

            ListBlobsOptions options = testBlobsSearch(steps, client, blobItems, filter, List.of(FIRST, SECOND));
            assertEquals(limit, options.getMaxResultsPerPage());
            assertNull(options.getPrefix());
            verifyNoInteractions(blobItem3);
        });
    }

    @SuppressWarnings({ "unchecked", "PMD.CloseResource" })
    private ListBlobsOptions testBlobsSearch(BlobStorageSteps steps, BlobContainerClient client,
            List<BlobItem> blobItems, BlobFilter filter, List<String> expectedBlobs)
    {
        PagedResponse<BlobItem> pagedResponse = mock(PagedResponse.class);
        when(pagedResponse.getValue()).thenReturn(blobItems);

        PagedIterable<BlobItem> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.iterableByPage()).thenReturn(List.of(pagedResponse));

        var optionsCaptor = ArgumentCaptor.forClass(ListBlobsOptions.class);
        when(client.listBlobs(optionsCaptor.capture(), isNull())).thenReturn(pagedIterable);

        steps.findBlobs(filter, CONTAINER, KEY, SCOPES, VARIABLE);
        verify(variableContext).putVariable(SCOPES, VARIABLE, expectedBlobs);

        return optionsCaptor.getValue();
    }

    private BlobItem blobItem(String name)
    {
        BlobItem first = mock(BlobItem.class);
        when(first.getName()).thenReturn(name);
        return first;
    }

    private void runWithStorageClient(FailableBiConsumer<BlobStorageSteps, BlobServiceClient, IOException> testToRun)
    {
        BlobServiceClient blobServiceClient = mock(BlobServiceClient.class);
        String endpoint = "endpoint";
        when(storageAccountEndpoints.get(KEY,
                "Storage account with key '%s' is not configured in properties", KEY)).thenReturn(endpoint);
        try (MockedConstruction<BlobServiceClientBuilder> serviceClientBuilder =
                mockConstruction(BlobServiceClientBuilder.class, (mock, context) -> {
                    when(mock.credential(tokenCredential)).thenReturn(mock);
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(blobServiceClient);
                }))
        {
            BlobStorageSteps steps = new BlobStorageSteps(storageAccountEndpoints, tokenCredential, variableContext,
                    jsonUtils);
            testToRun.accept(steps, blobServiceClient);
            assertThat(serviceClientBuilder.constructed(), hasSize(1));
            BlobServiceClientBuilder builder = serviceClientBuilder.constructed().get(0);
            InOrder ordered = inOrder(builder);
            ordered.verify(builder).credential(tokenCredential);
            ordered.verify(builder).endpoint(endpoint);
            ordered.verify(builder).buildClient();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private void runWithClient(FailableBiConsumer<BlobStorageSteps, BlobContainerClient, IOException> testToRun)
    {
        runWithStorageClient((steps, blobServiceClient) -> {
            BlobContainerClient blobContainerClient = mock(BlobContainerClient.class);
            when(blobServiceClient.getBlobContainerClient(CONTAINER)).thenReturn(blobContainerClient);
            testToRun.accept(steps, blobContainerClient);
        });
    }

    private BlobClient mockBlobClient(BlobContainerClient blobContainerClient)
    {
        BlobClient blobClient = mock(BlobClient.class);
        when(blobContainerClient.getBlobClient(BLOB)).thenReturn(blobClient);
        return blobClient;
    }
}

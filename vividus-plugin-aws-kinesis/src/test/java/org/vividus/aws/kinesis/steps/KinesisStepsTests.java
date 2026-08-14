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

package org.vividus.aws.kinesis.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.aws.auth.AwsServiceClientsContext;
import org.vividus.context.VariableContext;
import org.vividus.testcontext.TestContext;
import org.vividus.variable.VariableScope;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.GetRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.GetRecordsResponse;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorRequest;
import software.amazon.awssdk.services.kinesis.model.GetShardIteratorResponse;
import software.amazon.awssdk.services.kinesis.model.ListShardsRequest;
import software.amazon.awssdk.services.kinesis.model.ListShardsResponse;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.kinesis.model.Record;
import software.amazon.awssdk.services.kinesis.model.Shard;
import software.amazon.awssdk.services.kinesis.model.ShardIteratorType;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class })
class KinesisStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(KinesisSteps.class);

    private static final String STREAM_NAME = "stream-name";
    private static final String SHARD_ID = "shard-id";
    private static final String SHARD_ITERATOR = "shard-iterator";
    private static final String DATA = "data";
    private static final Object KEY = GetShardIteratorResponse.class;

    @Mock private AwsServiceClientsContext clientsContext;
    @Mock private TestContext testContext;
    @Mock private VariableContext variableContext;

    @Test
    void shouldPutRecord()
    {
        runWithKinesisClient((kinesis, steps) ->
        {
            String partitionKey = "partition-key-1";
            String sequenceNumber = "sequence-number";
            PutRecordResponse result = PutRecordResponse.builder()
                    .shardId(SHARD_ID)
                    .sequenceNumber(sequenceNumber)
                    .build();
            when(kinesis.putRecord(argThat((PutRecordRequest request) -> STREAM_NAME.equals(request.streamName())
                    && partitionKey.equals(request.partitionKey())
                    && DATA.equals(request.data().asUtf8String())))).thenReturn(result);

            steps.putRecord(DATA, partitionKey, STREAM_NAME);

            assertThat(LOGGER.getLoggingEvents(), equalTo(List
                    .of(info("The data was placed to the shard with ID '{}' under the sequence number '{}'", SHARD_ID,
                            sequenceNumber))));
        });
    }

    @Test
    void shouldCreateShardIterators()
    {
        runWithKinesisClient((kinesis, steps) ->
        {
            ListShardsResponse shards = ListShardsResponse.builder()
                    .shards(Shard.builder().shardId(SHARD_ID).build())
                    .build();
            when(kinesis.listShards(
                    argThat((ListShardsRequest rq) -> STREAM_NAME.equals(rq.streamName())))).thenReturn(shards);

            GetShardIteratorResponse shardIteratorResult = GetShardIteratorResponse.builder()
                    .shardIterator(SHARD_ITERATOR)
                    .build();
            when(kinesis.getShardIterator(
                    argThat((GetShardIteratorRequest rq) -> STREAM_NAME.equals(rq.streamName())
                            && SHARD_ID.equals(rq.shardId())
                            && ShardIteratorType.LATEST == rq.shardIteratorType())))
                    .thenReturn(shardIteratorResult);
            steps.createShardIterators(STREAM_NAME);

            List<String> shardIterators = List.of(SHARD_ITERATOR);
            verify(testContext).put(KEY, shardIterators);

            assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(
                    info("The total number of shards in the stream '{}' is {}", STREAM_NAME, 1),
                    info("Shard iterators are created: {}", shardIterators)
            )));
        });
    }

    @Test
    void shouldDrainKinesisRecordsToVariable()
    {
        runWithKinesisClient((kinesis, steps) ->
        {
            when(testContext.get(GetShardIteratorResponse.class)).thenReturn(List.of(SHARD_ITERATOR));
            String nextShardIterator = "next-" + SHARD_ITERATOR;
            Record record = Record.builder().data(SdkBytes.fromUtf8String(DATA)).build();
            GetRecordsResponse result = GetRecordsResponse.builder()
                    .nextShardIterator(nextShardIterator)
                    .records(record)
                    .build();
            when(kinesis.getRecords(
                    argThat((GetRecordsRequest rq) -> SHARD_ITERATOR.equals(rq.shardIterator())))).thenReturn(result);

            Set<VariableScope> scopes = Set.of(VariableScope.STEP);
            String variableName = "var-name";
            steps.drainKinesisRecordsToVariable(scopes, variableName);

            List<String> nextShardIterators = List.of(nextShardIterator);
            verify(testContext).put(KEY, nextShardIterators);
            verify(variableContext).putVariable(scopes, variableName, List.of(DATA));
            assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(
                    info("Getting records using shard iterator '{}'", SHARD_ITERATOR),
                    info("Next shard iterators are: {}", nextShardIterators)
            )));
        });
    }

    @SuppressWarnings("PMD.CloseResource")
    void runWithKinesisClient(BiConsumer<KinesisClient, KinesisSteps> kinesisConsumer)
    {
        KinesisClient kinesis = mock();
        KinesisSteps steps = new KinesisSteps(clientsContext, testContext, variableContext);
        when(clientsContext.getServiceClient(any(), any())).thenReturn(kinesis);
        kinesisConsumer.accept(kinesis, steps);
    }
}

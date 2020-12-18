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

package org.vividus.aws.kinesis.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.ListShardsResult;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.ShardIteratorType;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.testcontext.TestContext;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class })
class KinesisStepsTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(KinesisSteps.class);

    private static final String STREAM_NAME = "stream-name";
    private static final String SHARD_ID = "shard-id";
    private static final String SHARD_ITERATOR = "shard-iterator";
    private static final String DATA = "data";
    private static final Object KEY = GetShardIteratorResult.class;

    @Mock private TestContext testContext;
    @Mock private IBddVariableContext bddVariableContext;

    @Test
    void shouldPutRecord()
    {
        runWithKinesisClient((kinesis, steps) ->
        {
            String partitionKey = "partition-key-1";
            ArgumentCaptor<ByteBuffer> dataCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
            String sequenceNumber = "sequence-number";
            PutRecordResult result = new PutRecordResult().withShardId(SHARD_ID).withSequenceNumber(sequenceNumber);
            when(kinesis.putRecord(eq(STREAM_NAME), dataCaptor.capture(), eq(partitionKey))).thenReturn(result);

            steps.putRecord(DATA, partitionKey, STREAM_NAME);

            assertEquals(DATA, new String(dataCaptor.getValue().array(), StandardCharsets.UTF_8));
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
            ListShardsResult shards = new ListShardsResult().withShards(new Shard().withShardId(SHARD_ID));
            when(kinesis.listShards(argThat(rq -> STREAM_NAME.equals(rq.getStreamName())))).thenReturn(shards);

            GetShardIteratorResult shardIteratorResult = new GetShardIteratorResult().withShardIterator(SHARD_ITERATOR);
            when(kinesis.getShardIterator(
                    argThat(rq -> STREAM_NAME.equals(rq.getStreamName()) && SHARD_ID.equals(rq.getShardId())
                            && ShardIteratorType.LATEST.toString().equals(rq.getShardIteratorType()))))
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
            when(testContext.get(GetShardIteratorResult.class)).thenReturn(List.of(SHARD_ITERATOR));
            String nextShardIterator = "next-" + SHARD_ITERATOR;
            Record record = new Record().withData(ByteBuffer.wrap(DATA.getBytes(StandardCharsets.UTF_8)));
            GetRecordsResult result = new GetRecordsResult()
                    .withNextShardIterator(nextShardIterator)
                    .withRecords(record);
            when(kinesis.getRecords(argThat(rq -> SHARD_ITERATOR.equals(rq.getShardIterator())))).thenReturn(result);

            Set<VariableScope> scopes = Set.of(VariableScope.STEP);
            String variableName = "var-name";
            steps.drainKinesisRecordsToVariable(scopes, variableName);

            List<String> nextShardIterators = List.of(nextShardIterator);
            verify(testContext).put(KEY, nextShardIterators);
            verify(bddVariableContext).putVariable(scopes, variableName, List.of(DATA));
            assertThat(LOGGER.getLoggingEvents(), equalTo(List.of(
                    info("Getting records using shard iterator '{}'", SHARD_ITERATOR),
                    info("Next shard iterators are: {}", nextShardIterators)
            )));
        });
    }

    void runWithKinesisClient(BiConsumer<AmazonKinesis, KinesisSteps> kinesisConsumer)
    {
        try (MockedStatic<AmazonKinesisClientBuilder> builder = mockStatic(AmazonKinesisClientBuilder.class))
        {
            AmazonKinesis kinesis = mock(AmazonKinesis.class);
            builder.when(AmazonKinesisClientBuilder::defaultClient).thenReturn(kinesis);

            KinesisSteps steps = new KinesisSteps(testContext, bddVariableContext);

            kinesisConsumer.accept(kinesis, steps);
        }
    }
}

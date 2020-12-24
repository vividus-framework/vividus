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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.ListShardsRequest;
import com.amazonaws.services.kinesis.model.ListShardsResult;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.kinesis.model.Shard;
import com.amazonaws.services.kinesis.model.ShardIteratorType;

import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.testcontext.TestContext;

public class KinesisSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KinesisSteps.class);
    private static final Object KEY = GetShardIteratorResult.class;

    private final AmazonKinesis amazonKinesis;
    private final TestContext testContext;
    private final IBddVariableContext bddVariableContext;

    public KinesisSteps(TestContext testContext, IBddVariableContext bddVariableContext)
    {
        this.amazonKinesis = AmazonKinesisClientBuilder.defaultClient();
        this.testContext = testContext;
        this.bddVariableContext = bddVariableContext;
    }

    /**
     * Writes a single data record into an Amazon Kinesis data stream.  You must specify the name of the stream that
     * captures, stores, and transports the data; a partition key; and the data blob itself.
     * The partition key is used by Kinesis Data Streams to distribute data across shards. Kinesis Data Streams
     * segregates the data records that belong to a stream into multiple shards, using the partition key associated
     * with each data record to determine the shard to which a given data record belongs.
     * Partition keys are Unicode strings, with a maximum length limit of 256 characters for each key. An MD5 hash
     * function is used to map partition keys to 128-bit integer values and to map associated data records to shards
     * using the hash key ranges of the shards.
     * The step logs the shard ID of where the data record was placed and the sequence number that was assigned
     * to the data record. Sequence numbers increase over time and are specific to a shard within a stream, not
     * across all shards within
     * a stream.
     * After you write a record to a stream, you cannot modify that record or its order within the stream.
     *
     * @param data         The data blob to put into the record.
     * @param partitionKey The partition key determining which shard in the stream the data record is assigned to.
     * @param streamName   The name of the Amazon Kinesis data stream to put the data record into.
     */
    @When("I put record `$data` with partition key `$partitionKey` to Kinesis stream `$streamName`")
    public void putRecord(String data, String partitionKey, String streamName)
    {
        ByteBuffer wrappedData = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));
        PutRecordResult result = amazonKinesis.putRecord(streamName, wrappedData, partitionKey);
        LOGGER.atInfo()
                .addArgument(result::getShardId)
                .addArgument(result::getSequenceNumber)
                .log("The data was placed to the shard with ID '{}' under the sequence number '{}'");
    }

    /**
     * Creates Amazon Kinesis shard iterators. A shard iterator expires 5 minutes after it is returned to the requester.
     * A shard iterator specifies the shard position from which to start reading data records sequentially.
     * @param streamName The name of the Amazon Kinesis data stream.
     */
    @When("I start consuming records from Kinesis stream `$streamName`")
    public void createShardIterators(String streamName)
    {
        ListShardsRequest listShardsRequest = new ListShardsRequest().withStreamName(streamName);
        ListShardsResult listShardsResult = amazonKinesis.listShards(listShardsRequest);
        List<Shard> shards = listShardsResult.getShards();
        LOGGER.atInfo()
                .addArgument(streamName)
                .addArgument(shards::size)
                .log("The total number of shards in the stream '{}' is {}");

        List<String> shardIterators = shards.stream()
                .map(Shard::getShardId)
                .map(shardId -> new GetShardIteratorRequest()
                        .withStreamName(streamName)
                        .withShardId(shardId)
                        .withShardIteratorType(ShardIteratorType.LATEST)
                )
                .map(amazonKinesis::getShardIterator)
                .map(GetShardIteratorResult::getShardIterator)
                .collect(Collectors.toList());

        LOGGER.info("Shard iterators are created: {}", shardIterators);
        testContext.put(KEY, shardIterators);
    }

    /**
     * Gets data records from a Kinesis data stream's shard and drains the consumed records to the specified variable.
     * <p>
     * The shard iterator created at step <code>When I start consuming records from Kinesis stream `$streamName`</code>
     * specifies the position in the shard from which you want to start reading data records sequentially. If there
     * are no records available in the portion of the shard that the iterator points to, an empty list of records is
     * saved. Each draining moves the iterator to the position next after the last consumed record.
     * </p>
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName the variable name to store the records. The records are accessible via zero-based index,
     *                     e.g. `${my-var[0]}` will return the first received record.
     */
    @When("I drain consumed Kinesis records to $scopes variable `$variableName`")
    public void drainKinesisRecordsToVariable(Set<VariableScope> scopes, String variableName)
    {
        List<String> records = new ArrayList<>();
        List<String> nextShardIterators = new ArrayList<>();

        for (String shardIterator : testContext.<List<String>>get(KEY))
        {
            LOGGER.info("Getting records using shard iterator '{}'", shardIterator);

            GetRecordsRequest request = new GetRecordsRequest().withShardIterator(shardIterator);
            GetRecordsResult result = amazonKinesis.getRecords(request);

            nextShardIterators.add(result.getNextShardIterator());
            result.getRecords().stream()
                    .map(Record::getData)
                    .map(ByteBuffer::array)
                    .map(data -> new String(data, StandardCharsets.UTF_8))
                    .forEach(records::add);
        }

        LOGGER.info("Next shard iterators are: {}", nextShardIterators);
        testContext.put(KEY, nextShardIterators);

        bddVariableContext.putVariable(scopes, variableName, records);
    }
}

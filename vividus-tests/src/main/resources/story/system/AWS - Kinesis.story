Meta:
    @epic vividus-plugin-aws-kinesis
    @requirementId 1204
    @skip Amazon Kinesis is too expensive for open source projects

Scenario: Produce/consume records to/from Amazon Kinesis data stream
When I initialize the scenario variable `record` with value `Hello #{toEpochSecond(#{generateDate(P, yyyy-MM-dd'T'HH:mm:ss)})}`
When I start consuming records from Kinesis stream `vividus-data-stream`
When I put record `${record}` with partition key `hello` to Kinesis stream `vividus-data-stream`
When I drain consumed Kinesis records to scenario variable `consumed-records`
Then `${consumed-records[0]}` is equal to `${record}`

Meta:
    @epic vividus-plugin-avro

Scenario: Validate step 'When I convert Avro data from `$resourceNameOrFilePath` to JSON and save result to $scopes variable `$variableName`'
When I create temporary file with name `event-message.avro` and content `#{loadBinaryResource(/data/event-message.avro)}` and put path to scenario variable `temp-file-path`
When I convert Avro data from `${temp-file-path}` to JSON and save result to scenario variable `avro-data`
Then JSON element from `${avro-data}` by JSON path `$` is equal to `
[{
    "SequenceNumber": 0,
    "Offset": "0",
    "EnqueuedTimeUtc": "11/5/2021 1:25:22 PM",
    "SystemProperties": {
        "x-opt-enqueued-time": 1636118722484
    },
    "Properties": {},
    "Body": "my-data"
}]
`

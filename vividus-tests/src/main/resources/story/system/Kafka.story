Meta:
    @epic vividus-plugin-kafka
    @requirementId 1049

Scenario: Produce/consume data to/from Kafka
When I initialize the scenario variable `message` with value `message-from-system-vividus-test-#{generate(regexify '[a-z]{8}')}`
When I initialize the story variable `topic` with value `l6eo4ztm-default`
When I start consuming messages from Kafka topics `${topic}`
When I send data `${message}` to Kafka topic `${topic}`
When I wait with `PT30S` timeout until count of consumed Kafka messages is equal to `1`
When I stop consuming messages from Kafka
When I drain consumed Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages[0]}` is equal to `${message}`


Scenario: Wait until expected message appears in the Kafka topic
When I initialize the scenario variable `message-marker` with value `#{generate(regexify '[a-z]{8}')}`
When I start consuming messages from Kafka topics `${topic}`
When I send data `{"key" : "failed-${message-marker}"}` to Kafka topic `${topic}`
When I send data `{"key" : "passed-${message-marker}"}` to Kafka topic `${topic}`
When I execute steps with delay `PT1S` at most 30 times while variable `messageCount` is = `0`:
|step                                                                                                                               |
|When I peek consumed Kafka messages to scenario variable `messages`                                                                |
|When I save number of elements from `${messages}` found by JSON path `$..[?(@.key == "failed")]` to scenario variable `messageCount`|
When I drain consumed Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages}` is equal to `[{"key" : "failed-${message-marker}"}, {"key" : "passed-${message-marker}"}]`
When I stop consuming messages from Kafka

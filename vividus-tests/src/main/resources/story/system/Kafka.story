Meta:
    @epic vividus-plugin-kafka
    @requirementId 1049

Scenario: Produce/consume data to/from Kafka
When I initialize the scenario variable `message` with value `message-from-system-vividus-test-#{generate(regexify '[a-z]{8}')}`
When I initialize the story variable `topic` with value `l6eo4ztm-default`
When I start consuming messages from `vividus` Kafka topics `${topic}`
When I send data `${message}` to `vividus` Kafka topic `${topic}`
When I wait with `PT30S` timeout until count of consumed `vividus` Kafka messages is equal to `1`
When I stop consuming messages from `vividus` Kafka
When I drain consumed `vividus` Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages[0]}` is equal to `${message}`


Scenario: Wait until expected message appears in the Kafka topic
When I start consuming messages from `vividus` Kafka topics `${topic}`
When I send data `{"key" : "failed"}` to `vividus` Kafka topic `${topic}`
When I send data `{"key" : "passed"}` to `vividus` Kafka topic `${topic}`
When I execute steps with delay `PT1S` at most 30 times while variable `messageCount` is = `0`:
|step                                                                                                                               |
|When I peek consumed `vividus` Kafka messages to scenario variable `messages`                                                                |
|When I save number of elements from `${messages}` found by JSON path `$..[?(@.key == "failed")]` to scenario variable `messageCount`|
When I drain consumed `vividus` Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages}` is equal to `[{"key" : "failed"}, {"key" : "passed"}]`
When I stop consuming messages from `vividus` Kafka

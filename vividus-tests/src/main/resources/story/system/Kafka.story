Meta:
    @epic vividus-plugin-kafka
    @requirementId 1049

Scenario: Produce/consume data to/from Kafka
When I initialize the scenario variable `message` with value `message-from-system-vividus-test-#{generate(regexify '[a-z]{8}')}`
When I initialize the scenario variable `topic` with value `l6eo4ztm-default`
When I start consuming messages from Kafka topics `${topic}`
When I send data `${message}` to Kafka topic `${topic}`
When I wait with `PT30S` timeout until count of consumed Kafka messages is equal to `1`
When I stop consuming messages from Kafka
When I drain consumed Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages[0]}` is equal to `${message}`

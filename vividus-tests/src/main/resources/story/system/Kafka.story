Meta:
    @epic vividus-plugin-kafka
    @requirementId 1049

Scenario: Send data to Kafka
When I send data `message-from-system-vividus-test-#{generate(regexify '[a-z]{8}')}` to Kafka topic `l6eo4ztm-default`

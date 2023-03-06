Meta:
    @epic vividus-plugin-kafka
    @requirementId 1049

Scenario: Init
Given I initialize story variable `topic` with value `hnzzmrpq-default`

Scenario: [DEPRECATED] Produce/consume data to/from Kafka
Given I initialize scenario variable `message` with value `{"message-from-system-vividus-test": "#{generate(regexify '[a-z]{8}')}"}`
When I start consuming messages from `vividus` Kafka topics `${topic}`
When I send data `${message}` to `vividus` Kafka topic `${topic}`
When I wait with `PT60S` timeout until count of consumed `vividus` Kafka messages is equal to `1`
When I stop consuming messages from `vividus` Kafka
When I drain consumed `vividus` Kafka messages to scenario variable `consumed-messages`
Then `${consumed-messages[0]}` is equal to `${message}`


Scenario: [DEPRECATED] Wait until expected message appears in the Kafka topic
When I start consuming messages from `vividus` Kafka topics `${topic}`
When I send data `{"key" : "failed"}` to `vividus` Kafka topic `${topic}`
When I send data `{"key" : "passed"}` to `vividus` Kafka topic `${topic}`
When I execute steps with delay `PT5S` at most 12 times while variable `messageCount` is = `0`:
|step                                                                                                                                |
|When I peek consumed `vividus` Kafka messages to scenario variable `messages`                                                       |
|When I save number of elements from `${messages}` found by JSON path `$..[?(@.key == "failed")]` to scenario variable `messageCount`|
When I drain consumed `vividus` Kafka messages to scenario variable `consumed-messages`
Then JSON element from `${consumed-messages}` by JSON path `$` is equal to `[{"key" : "failed"}, {"key" : "passed"}]` ignoring array order
When I stop consuming messages from `vividus` Kafka


Scenario: Produce/consume events to/from Kafka
Meta:
    @requirementId 2915
Given I initialize scenario variable `event-value` with value `{"event-from-system-vividus-test": "#{generate(regexify '[a-z]{8}')}"}`
When I start consuming events from `vividus` Kafka topics `${topic}`
When I execute steps:
|step            |
|<eventPublisher>|
When I wait with `PT60S` timeout until count of consumed `vividus` Kafka events is equal to `1`
When I stop consuming events from `vividus` Kafka
When I drain consumed `vividus` Kafka events to scenario variable `consumed-events`
Then `${consumed-events[0]}` is equal to `${event-value}`
Examples:
|eventPublisher                                                                                       |
|When I send event with value `${event-value}` to `vividus` Kafka topic `${topic}`                    |
|When I send event with key `event-key` and value `${event-value}` to `vividus` Kafka topic `${topic}`|


Scenario: Wait until expected event appears in the Kafka topic
When I start consuming events from `vividus` Kafka topics `${topic}`
When I send event with value `{"status" : "failed"}` to `vividus` Kafka topic `${topic}`
When I send event with value `{"status" : "passed"}` to `vividus` Kafka topic `${topic}`
When I execute steps with delay `PT5S` at most 12 times while variable `eventCount` is = `0`:
|step                                                                                                                               |
|When I peek consumed `vividus` Kafka events to scenario variable `events`                                                          |
|When I save number of elements from `${events}` found by JSON path `$..[?(@.status == "failed")]` to scenario variable `eventCount`|
When I drain consumed `vividus` Kafka events to scenario variable `consumed-events`
Then JSON element from `${consumed-events}` by JSON path `$` is equal to `[{"status" : "failed"}, {"status" : "passed"}]` ignoring array order
When I stop consuming events from `vividus` Kafka

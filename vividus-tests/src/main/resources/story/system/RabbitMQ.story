Meta:
    @epic vividus-plugin-rabbitmq

Scenario: Produce/consume messages to/from RabbitMQ
Given I initialize scenario variable `message` with value `#{generate(regexify '[a-z]{8}')}`
When I publish message `${message}` with routing key `test_queue` to RabbitMQ broker `cloudamqp`
When I retrieve message from queue `test_queue` of RabbitMQ broker `cloudamqp` with `PT60S` timeout and save it to scenario variable `receivedMessage`
Then `${message}` is equal to `${receivedMessage}`

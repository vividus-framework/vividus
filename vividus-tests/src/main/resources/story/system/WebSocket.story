Meta:
    @epic vividus-plugin-websocket
    @requirementId 1402

Scenario: WebSocket interactions

When I initialize the scenario variable `message` with value `#{generate(regexify '[a-z]{15}')}`
When I connect to `echo` websocket
When I send text message `${message}` over `echo` websocket
When I wait with `PT30S` timeout until count of text messages received over `echo` websocket is greater than `0`
When I drain text messages received over `echo` websocket to scenario variable `messages`
Then `${messages[0]}` is equal to `${message}`
When I disconnect from `echo` websocket

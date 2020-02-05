Description: Integration tests for variables

Meta:
    @group variables

Scenario: Verify default variables
Then `1` is = `${key:1}`
Then `2` is = `${list[0]:2}`
Then `3` is = `${map.key:3}`
Then `4` is = `${list[0].key:4}`

Scenario: Verify batch scope has higher priority than global scope
Then `${scope-priority-check}` is equal to `should be batch`

Scenario: Verify step for saving examples table to variable as list of maps
When I initialize SCENARIO variable `testMap` with values:
|key1  |key2  |
|value1|value2|
Then `value1` is = `${testMap[0].key1}`
Then `value2` is = `${testMap[0].key2}`

Description: Integration tests for ExecutableSteps class.

Meta:
    @epic vividus-core
    @feature executable-steps

Scenario: Step verification When I iterate while counter is $comparisonRule `$limit` with increment `$increment` starting from `$seed`:$stepsToExecute

When I execute steps while counter is less than or equal to `10` with increment `3` starting from `1`:
|step                                                                                                |
|When I initialize the SCENARIO variable `key-${iterationVariable}` with value `${iterationVariable}`|
Then `${key-1}` is equal to `1`
Then `${key-2}` is equal to `${key-2}`
Then `${key-3}` is equal to `${key-3}`
Then `${key-4}` is equal to `4`
Then `${key-5}` is equal to `${key-5}`
Then `${key-6}` is equal to `${key-6}`
Then `${key-7}` is equal to `7`
Then `${key-8}` is equal to `${key-8}`
Then `${key-9}` is equal to `${key-9}`
Then `${key-10}` is equal to `10`
Then `${key-11}` is equal to `${key-11}`
Then `${key-12}` is equal to `${key-12}`
Then `${key-13}` is equal to `${key-13}`

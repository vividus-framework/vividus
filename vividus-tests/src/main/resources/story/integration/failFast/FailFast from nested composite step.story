Meta:
    @feature fail-fast

Scenario: This is the first successful scenario
Then `true` is equal to `true`

Scenario: Execute nested failing composite step
When I execute steps:
|step                                                                                |
|Then `true` is equal to `true`                                                      |
|When I execute failing composite step                                               |
|Then `nested step should never be executed` is equal to `must fail if it's executed`|
Then `outer step should never be executed` is equal to `must fail if it's executed`

Scenario: This scenario should be skipped because the previous one is failed
Then `true` is equal to `false`

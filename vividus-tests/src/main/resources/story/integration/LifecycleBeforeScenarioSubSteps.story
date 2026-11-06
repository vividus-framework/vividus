Meta:
    @epic vividus-core
    @feature sub-steps

Lifecycle:
Before:
Scope: SCENARIO
When I execute steps while counter is less than or equal to `1` with increment `1` starting from `0`:
|step                                                                              |
|Given I initialize scenario variable `iteration` with value `${iterationVariable}`|

Scenario: Verify scenario is reported when Lifecycle Before steps log between sub-steps
Then `${iteration}` is equal to `1`

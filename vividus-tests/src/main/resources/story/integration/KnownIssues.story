Description: Integration tests for known issues functionality

Meta:
    @epic vividus-core
    @feature known-issues

Scenario: Known issue should be detected and matched by variable pattern
When I initialize the scenario variable `var` with value `value-#{generate(regexify '[a-z]{5}')}`
Then `4` is equal to `5`

Scenario: Known issues should be detected and matched for separate JSON assertions
Then JSON element from `
[
    {
        "different_value": "123ij8",
        "missing_number": 1
    }
]
` by JSON path `$` is equal to `
[
    {
        "different_value": "kj32n48",
        "extra_number": 0
    }
]
`
IGNORING_ARRAY_ORDER

Scenario: Known issue should be detected and matched by step pattern with composite step
When I use composite step in known-issues

Scenario: Known issue should should stop scenario if it's marked as fail test case fast
Then `vividus` matches `[A-Z]+`
!-- The next step should not be performed
Then `true` is equal to `false`

Scenario: Known issue should be detected in sub-steps
Meta:
    @issueId 1094
When the condition `#{eval( true )}` is true I do
|step                                     |
|When I use composite step in known-issues|
!-- The next step should not be performed
Then `true` is equal to `false`

Scenario: Known issue should should stop scenario and the rest of the story if it's marked as fail test case and test suite fast
Then `vividus` matches `[A-Z]+`
!-- The next step should not be performed
Then `true` is equal to `false`

Scenario: Should not be performed
Then `failure` matches `\d+`

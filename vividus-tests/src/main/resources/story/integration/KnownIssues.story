Description: Integration tests for known issues functionality

Meta:
    @epic vividus-core
    @feature known-issues

Scenario: Known issue should be detected and matched by variable pattern
When I initialize the scenario variable `var` with value `value-#{generate(regexify '[a-z]{5}')}`
Then `4` is equal to `5`

Scenario: Known issues should be detected and matched for separate JSON assertions
Then a JSON element from '
[
    {
        "different_value": "123ij8",
        "missing_number": 1
    }
]
' by the JSON path '$' is equal to '
[
    {
        "different_value": "kj32n48",
        "extra_number": 0
    }
]
'
IGNORING_ARRAY_ORDER

Description: Integration tests for known issues functionality

Meta:
    @group known-issues

Scenario: Known issue should be detected and matched by variable pattern
When I initialize the scenario variable `var` with value `value-#{generate(regexify '[a-z]{5}')}`
Then `4` is equal to `5`

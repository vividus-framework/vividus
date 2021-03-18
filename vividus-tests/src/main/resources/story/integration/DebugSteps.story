Description: Integration tests for DebugSteps class.

Meta:
    @epic vividus-core

Scenario: Verify wait for debug step
When I initialize the SCENARIO variable `period` with value `PT2S`
When I initialize the SCENARIO variable `date1` with value `#{generateDate(P, yyyy-MM-dd HH:mm:ss)}`
When I wait `${period}` for debug
When I initialize the SCENARIO variable `date2` with value `#{generateDate(P, yyyy-MM-dd HH:mm:ss)}`
Then `${period}` is equal to `#{diffDate(${date1}, yyyy-MM-dd HH:mm:ss, ${date2}, yyyy-MM-dd HH:mm:ss)}`

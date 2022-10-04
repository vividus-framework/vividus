Description: Integration tests for DebugSteps class.

Meta:
    @epic vividus-core

Scenario: Verify wait for debug step
Given I initialize scenario variable `secondsToWait` with value `2`
Given I initialize scenario variable `date1` with value `#{generateDate(P, yyyy-MM-dd HH:mm:ss)}`
When I wait `PT${secondsToWait}S` for debug
Given I initialize scenario variable `date2` with value `#{generateDate(P, yyyy-MM-dd HH:mm:ss)}`
Given I initialize scenario variable `actualDiff` with value `#{diffDate(${date1}, yyyy-MM-dd HH:mm:ss, ${date2}, yyyy-MM-dd HH:mm:ss)}`
Then `${secondsToWait}` is less than or equal to `#{replaceFirstByRegExp(PT(\d)S, $1, ${actualDiff})}`

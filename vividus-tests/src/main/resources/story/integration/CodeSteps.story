Scenario: Step verification: "When I execute javascript `$jsCode` and save result to $scopes variable `$variableName`"
!-- Deprecated
Given I am on a page with the URL '${vividus-test-site-url}'
When I perform javascript 'return JSON.stringify(window.performance.timing)' and save result to the 'scenario' variable 'timings'
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1


Scenario: Step verification: "When I execute javascript `$jsCode` and save result to $scopes variable `$variableName`"
Given I am on a page with the URL '${vividus-test-site-url}'
When I execute javascript `return JSON.stringify(window.performance.timing)` and save result to scenario variable `timings`
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1

Scenario: Step verification: "When I perform async javascript '$jsCode' and save result to the '$scopes' variable '$variableName'"
!-- Deprecated
When I perform async javascript 'arguments[0](JSON.stringify(window.performance.timing))' and save result to the 'scenario' variable 'timings'
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1

Scenario: Step verification: "When I execute async javascript `$jsCode` and save result to $scopes variable `$variableName`"
When I execute async javascript `arguments[0](JSON.stringify(window.performance.timing))` and save result to scenario variable `timings`
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1

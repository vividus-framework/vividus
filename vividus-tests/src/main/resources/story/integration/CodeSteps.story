Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification: "When I execute javascript `$jsCode` and save result to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}`
When I execute javascript `return JSON.stringify(window.performance.timing)` and save result to scenario variable `timings`
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1


Scenario: Step verification: "When I execute async javascript `$jsCode` and save result to $scopes variable `$variableName`"
When I execute async javascript `arguments[0](JSON.stringify(window.performance.timing))` and save result to scenario variable `timings`
Then number of JSON elements from `${timings}` by JSON path `$.connectStart` is = 1


Scenario: Step verification: "When I execute javascript `$jsCode` with arguments:$args"
Then number of elements found by `xpath(//img)` is = `1`
When I execute javascript `document.querySelector('[name="vividus-logo"]').remove()` with arguments:
Then number of elements found by `xpath(//img)` is = `0`

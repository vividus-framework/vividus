Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: "I save number of open tabs to $scopes variable `$variable`"
Given I am on page with URL `${vividus-test-site-url}`
When I open new tab
When I save number of open tabs to SCENARIO variable `count`
Then `${count}` is equal to `2`
When I close the current window

Scenario: Verify deprecated step: "When I get the number of open windows and set it to the $scopes variable '$variable'"
Given I am on page with URL `${vividus-test-site-url}`
When I open new tab
When I get the number of open windows and set it to the SCENARIO variable 'count'
Then `${count}` is equal to `2`
When I close the current window

Scenario: Verify step: "I save table to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable `table`
Then `${table}` is equal to table:
|A|B|C|
|A1|B1|C1|
|A2|B2|C2|
|A3|B3|C3|

Scenario: Verify deprecated step: "When I save table to $scopes variable '$variableName'"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable 'table'
Then `${table[1]}` is equal to table ignoring extra columns:
|A|C|
|A2|C2|

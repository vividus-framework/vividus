Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: "I save table to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable `table`
Then `${table}` is equal to table:
|A |B |C |
|A1|B1|C1|
|A2|B2|C2|
|A3|B3|C3|

Scenario: Verify deprecated step: "When I save table to $scopes variable '$variableName'"
Given I am on page with URL `${vividus-test-site-url}/table.html`
When I change context to element located `xpath(//table)`
When I save table to SCENARIO variable 'table'
Then `${table[1]}` is equal to table ignoring extra columns:
|A |C |
|A2|C2|

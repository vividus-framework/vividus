Meta:
    @epic vividus-plugin-web-app

Scenario: Validation of step When I change window size to `$sizeAsString`
Given I am on a page with the URL '${vividus-test-site-url}/stickyHeader.html'
When I change context to element located `id(myHeader)`
Then `${context-width}` is <conditionBefore> `<targetWidth>`
When I change window size to `<targetWidth>x<targetHeight>`
Then `${context-width}` is <= `<targetWidth>`
Examples:
|conditionBefore|targetWidth|targetHeight|
|>              |640        |320         |
|<              |720        |480         |

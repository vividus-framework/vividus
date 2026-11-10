Meta:
    @epic vividus-plugin-web-app

Scenario: Validation of step When I change window size to `$sizeAsString`
Given I am on page with URL `${vividus-test-site-url}/stickyHeader.html`
When I change context to element located by `id(myHeader)`
Then `${context-width}` is <conditionBefore> `<targetWidth>`
When I change window size to `<targetWidth>x<targetHeight>`
Then `${context-width}` is <= `<targetWidth>`
Examples:
|conditionBefore|targetWidth|targetHeight|
|>              |640        |320         |
|<              |720        |480         |


Scenario: Validation of step When I maximize window
When I maximize window
Given I initialize story variable `maximizedHeight` with value `${browser-window-height}`
Given I initialize story variable `maximizedWidth` with value `${browser-window-width}`
When I change window size to `720x480`
When I maximize window
Then `${browser-window-height}` is equal to `${maximizedHeight}`
Then `${browser-window-width}` is equal to `${maximizedWidth}`

Description: Integration tests for PageSteps class.

Meta:
    @epic vividus-plugin-web-app
    @capability.pageLoadStrategy eager

Scenario: Verify step: 'Then page is scrolled to element located by `$locator`'
Given I am on page with URL `${vividus-test-site-url}/delayedScroll.html`
When I click on element located by `id(anchor)`
When I wait until scroll is finished
Then page is scrolled to element located by `id(toClick)`


Scenario: Verify deprecated step: When I open URL `$URL` in new window; Verify step: When I stop page loading
Meta:
    @requirementId 1154; 1236
When I open URL `${vividus-test-site-url}/delayedLoading?imageTimeout=1000` in new window
When I stop page loading
When I execute async javascript `setTimeout(() => arguments[0]('wait for page to load'), 1000)` and save result to scenario variable `timeout`
When I change context to element located by `cssSelector(img)`
Then `${context-height}` is < `100`
Then `${context-width}`  is < `100`

Scenario: Verify step: Then metric $webPerformanceMetric is $comparisonRule `$duration`
Then metric <metric> is less than `PT6S`
Examples:
|metric               |
|TIME_TO_FIRST_BYTE   |
|DNS_LOOKUP_TIME      |
|DOM_CONTENT_LOAD_TIME|
|PAGE_LOAD_TIME       |

Scenario: Verify URL components of the opened page
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then `${current-page-url}` is equal to `${vividus-test-site-url}/mouseEvents.html`
Then `#{extractHostFromUrl(${current-page-url})}` is equal to `${vividus-test-site-host}`
Then `#{extractPathFromUrl(${current-page-url})}` is equal to `/mouseEvents.html`
Then `${current-page-url}` matches `.+mouse.+`

Scenario: Verify URL components of the opened page with deprecated steps
Then the page with the URL '${vividus-test-site-url}/mouseEvents.html' is loaded
Then the host of the page URL is '${vividus-test-site-host}'
Then the page has the relative URL '/mouseEvents.html'
Then the page with the URL containing 'mouse' is loaded

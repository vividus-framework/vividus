Description: Integration tests for PageSteps class.

Meta:
    @epic vividus-plugin-web-app
    @capability.pageLoadStrategy eager

Scenario:  Verify step: I am on page with URL '$pageURL'; Verify step: 'Then page is scrolled to element located `$locator`'
Given I am on page with URL '${vividus-test-site-url}/delayedScroll.html'
When I click on element located by `id(anchor)`
When I wait until scroll is finished
Then page is scrolled to element located `id(toClick)`


Scenario:  Verify step: page has relative URL '$relativeURL'; Verify step: host of the page URL is '$host'
Given I am on page with URL '${vividus-test-site-url}/delayedScroll.html'
Then page has relative URL '/delayedScroll.html'
Then host of the page URL is '${vividus-test-site-url}'

Scenario:  Verify composite step: the page has the relative URL '$relativeURL'; Verify composite step: the host of the page URL is '$host'
Given I am on page with URL '${vividus-test-site-url}/delayedScroll.html'
Then the page has the relative URL '/delayedScroll.html'
Then the host of the page URL is '${vividus-test-site-url}'


Scenario: Verify step: When I open URL '$URL' in new window; Verify step: When I stop page loading
Meta:
    @requirementId 1154; 1236
When I open URL '${vividus-test-site-url}/delayedLoading?imageTimeout=1000' in new window
When I stop page loading
When I execute async javascript `setTimeout(() => arguments[0]('wait for page to load'), 1000)` and save result to scenario variable `timeout`
When I change context to element located by `cssSelector(img)`
Then `${context-height}` is < `100`
Then `${context-width}`  is < `100`


Scenario: Verify step: I refresh page; Verify step: page with URL '$URL' is loaded
Given I am on page with URL '${vividus-test-site-url}'
When I refresh page
Then page with URL '${vividus-test-site-url}' is loaded

Scenario: Verify composite step: I refresh the page; Verify composite step: the page with the URL '$URL' is loaded
Given I am on page with URL '${vividus-test-site-url}'
When I refresh the page
Then the page with the URL '${vividus-test-site-url}' is loaded


Scenario: Verify step: page with URL containing '$URLpart' is loaded
Given I am on page with URL '${vividus-test-site-url}/delayedScroll.html'
When I refresh page
Then page with URL containing '/delayedScroll.html' is loaded

Scenario: Verify composite step: the page with the URL containing '$URLpart' is loaded; Verify composite step: When I open URL `$URL` in new window
When I open URL `https://www.google.com/doodles` in new window
When I refresh page
Then the page with the URL containing 'doodle' is loaded


Scenario: Verify step: I go to relative URL '$relativeURL'
Given I am on page with URL 'https://www.google.com/'
When I go to relative URL 'doodles'
Then page with URL 'https://www.google.com/doodles' is loaded

Scenario: Verify composite step: I go to relative URL `$relativeURL`
Given I am on page with URL 'https://www.google.com/'
When I go to relative URL `doodles`
Then page with URL 'https://www.google.com/doodles' is loaded


Scenario: Verify step: page title $comparisonRule '$text'
Given I am on page with URL 'https://www.google.com/'
Then page title is equal to 'Google'

Scenario: Verify composite step: the page title $comparisonRule '$text'; Verify composite step: 'I am on page with URL `$pageURL`'
Given I am on page with URL `https://www.google.com/`
Then the page title is equal to 'Google'


Scenario: Verify step: Then metric $webPerformanceMetric is $comparisonRule `$duration`
Then metric <metric> is less than `PT5S`
Examples:
|metric               |
|TIME_TO_FIRST_BYTE   |
|DNS_LOOKUP_TIME      |
|DOM_CONTENT_LOAD_TIME|
|PAGE_LOAD_TIME       |

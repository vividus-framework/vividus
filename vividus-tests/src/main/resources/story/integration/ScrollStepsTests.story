Meta:
    @epic vividus-plugin-web-app

Scenario: Scroll RIGHT for element Verify steps: When I scroll context to $scrollDirection edge, When I wait until element located by `$locator` has text matching `$regex`
Given I am on page with URL `${vividus-test-site-url}/scrollableElements.html`
When I change context to element located by `id(scrollable)`
When I scroll context to RIGHT edge
When I reset context
When I wait until element located by `id(current-horizontal):a` has text matching `\d+`

Scenario: Scroll LEFT for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(scrollable)`
When I scroll context to LEFT edge
When I reset context
When I wait until element located by `id(current-horizontal):a` contains text `0`

Scenario: Scroll BOTTOM for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(current-vertical):a`
When I change context to element located by `id(scrollable)`
When I scroll context to BOTTOM edge
When I reset context
When I wait until element located by `id(current-horizontal):a` has text matching `\d+`

Scenario: Scroll TOP for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(scrollable)`
When I scroll context to TOP edge
When I reset context
When I wait until element located by `id(current-vertical):a` contains text `0`

Scenario: Verify steps: "When I scroll element located by `$locator` into view", "Then element located by `$locator` $presence visible in viewport", "When I wait until element located by `$locator` has text matching `$regex`", "When I wait until element located by `$locator` appears in viewport"
Meta:
    @requirementId 436
    @playwrightSupported
Given I am on page with URL `${vividus-test-site-url}/scrollableElements.html`
Then element located by `xpath(//a[text()="Contact"])` is not visible in viewport
When I scroll element located by `xpath(//a[text()="Contact"])` into view
When I wait until element located by `xpath(//a[text()="Contact"])` appears in viewport
Then element located by `xpath(//a[text()="Contact"])` is visible in viewport
When I wait until element located by `id(current-vertical):a` has text matching `\d+`

Scenario: Verify deprecated steps: "When I scroll element located `$locator` into view", "Then page is scrolled to element located `$locator`"
When I refresh page
When I scroll element located `xpath(//a[text()="Contact"])` into view
Then page is scrolled to element located `xpath(//a[text()="Contact"])`
When I wait until element located by `id(current-vertical):a` has text matching `\d+`

Scenario: Scroll BOTTOM for page Verify step: When I scroll context to $scrollDirection edge
Given I am on page with URL `${vividus-test-site-url}/scrollablePage.html`
When I scroll context to BOTTOM edge
When I execute javascript `return document.documentElement.scrollTop` and save result to scenario variable `scroll`
Then `${scroll}` is > `0`

Scenario: Scroll TOP for page Verify step: When I scroll context to $scrollDirection edge
When I scroll context to TOP edge
When I execute javascript `return document.documentElement.scrollTop` and save result to scenario variable `scroll`
Then `${scroll}` is = `0`

Scenario: Validate scrolling of element into view when window scroll is not in the initial position
Given I am on page with URL `${vividus-test-site-url}/hugeStickyHeader.html`
When I scroll element located by `id(target)` into view
When I click on element located by `id(click-me)`

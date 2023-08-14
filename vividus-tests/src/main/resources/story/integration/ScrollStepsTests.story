Meta:
    @epic vividus-plugin-web-app

Scenario: Scroll RIGHT for element Verify step: When I scroll context to $scrollDirection edge
Given I am on page with URL `${vividus-test-site-url}/scrollableElements.html`
When I change context to element located by `id(scrollable)`
When I scroll context to RIGHT edge
When I change context to element located by `id(current-horizontal):a`
Then the text matches '\d+'

Scenario: Scroll LEFT for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(scrollable)`
When I scroll context to LEFT edge
When I reset context
When I wait until element located by `id(current-horizontal):a` contains text `0`

Scenario: Scroll BOTTOM for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(current-vertical):a`
When I change context to element located by `id(scrollable)`
When I scroll context to BOTTOM edge
When I change context to element located by `id(current-vertical):a`
Then the text matches '\d+'

Scenario: Scroll TOP for element Verify step: When I scroll context to $scrollDirection edge
When I change context to element located by `id(scrollable)`
When I scroll context to TOP edge
When I reset context
When I wait until element located by `id(current-vertical):a` contains text `0`

Scenario: Verify steps: "When I scroll element located by `$locator` into view", "Then page is scrolled to element located by `$locator`"
Meta:
    @requirementId 436
When I refresh page
When I scroll element located by `xpath(//a[text()="Contact"])` into view
Then page is scrolled to element located by `xpath(//a[text()="Contact"])`
When I change context to element located by `id(current-vertical):a`
Then the text matches '\d+'

Scenario: Verify deprecated steps: "When I scroll element located `$locator` into view", "Then page is scrolled to element located `$locator`"
When I refresh page
When I scroll element located `xpath(//a[text()="Contact"])` into view
Then page is scrolled to element located `xpath(//a[text()="Contact"])`
When I change context to element located by `id(current-vertical):a`
Then the text matches '\d+'

Scenario: Scroll BOTTOM for page Verify step: When I scroll context to $scrollDirection edge
Given I am on page with URL `${vividus-test-site-url}/scrollablePage.html`
When I scroll context to BOTTOM edge
When I execute javascript `return document.documentElement.scrollTop` and save result to scenario variable `scroll`
Then `${scroll}` is > `0`

Scenario: Scroll TOP for page Verify step: When I scroll context to $scrollDirection edge
When I scroll context to TOP edge
When I execute javascript `return document.documentElement.scrollTop` and save result to scenario variable `scroll`
Then `${scroll}` is = `0`

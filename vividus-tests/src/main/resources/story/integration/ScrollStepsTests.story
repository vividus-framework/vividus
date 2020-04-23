Meta:
    @epic vividus-plugin-web-app

Scenario: Scroll RIGHT for element Verify step: When I scroll context to $scrollDirection edge
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/scrollableElements.html'
When I change context to an element by By.id(scrollable)
When I scroll context to RIGHT edge
When I change context to an element by By.id(current-horizontal):a
Then the text matches '\d+'

Scenario: Scroll LEFT for element Verify step: When I scroll context to $scrollDirection edge
When I change context to an element by By.id(scrollable)
When I scroll context to LEFT edge
When I change context to an element by By.id(current-horizontal):a
Then the text matches '0'

Scenario: Scroll BOTTOM for element Verify step: When I scroll context to $scrollDirection edge
When I change context to an element by By.id(current-vertical):a
When I change context to an element by By.id(scrollable)
When I scroll context to BOTTOM edge
When I change context to an element by By.id(current-vertical):a
Then the text matches '\d+'

Scenario: Scroll TOP for element Verify step: When I scroll context to $scrollDirection edge
When I change context to an element by By.id(scrollable)
When I scroll context to TOP edge
When I change context to an element by By.id(current-vertical):a
Then the text matches '0'

Scenario: Verify step: When I scroll to the end of the context
When I change context to an element by By.id(current-vertical):a
When I change context to an element by By.id(scrollable)
When I scroll to the end of the context
When I change context to an element by By.id(current-vertical):a
Then the text matches '\d+'

Scenario: Verify step: When I scroll to the start of the context
When I change context to an element by By.id(current-vertical):a
When I change context to an element by By.id(scrollable)
When I scroll to the start of the context
When I change context to an element by By.id(current-vertical):a
Then the text matches '0'

Scenario: Scroll BOTTOM for page Verify step: When I scroll context to $scrollDirection edge
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/scrollablePage.html'
When I scroll context to BOTTOM edge
When I perform javascript 'return document.documentElement.scrollTop' and save result to the 'scenario' variable 'scroll'
Then `${scroll}` is > `0`

Scenario: Scroll TOP for page Verify step: When I scroll context to $scrollDirection edge
When I scroll context to TOP edge
When I perform javascript 'return document.documentElement.scrollTop' and save result to the 'scenario' variable 'scroll'
Then `${scroll}` is = `0`

Scenario: Verify step: When I scroll to the end of the page
When I scroll to the end of the page
When I perform javascript 'return document.documentElement.scrollTop' and save result to the 'scenario' variable 'scroll'
Then `${scroll}` is > `0`

Scenario: Verify step: When I scroll to the start of the page
When I scroll to the start of the page
When I perform javascript 'return document.documentElement.scrollTop' and save result to the 'scenario' variable 'scroll'
Then `${scroll}` is = `0`

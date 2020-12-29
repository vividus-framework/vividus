Description: Integration tests for PageSteps class.

Meta:
    @epic vividus-plugin-web-app
    @capability.pageLoadStrategy eager

Scenario: Verify step: 'Then page is scrolled to element located `$locator`'
Given I am on a page with the URL '${vividus-test-site-url}/delayedScroll.html'
When I click on element located `By.id(anchor)`
When I wait until scroll is finished
Then page is scrolled to element located `id(toClick)`


Scenario: Verify step: 'Then the page is scrolled to an element with the attribute '$attributeType'='$attributeValue''
When I refresh the page
When I click on element located `By.id(anchor)`
When I wait until scroll is finished
Then the page is scrolled to an element with the attribute 'id'='toClick'


Scenario: Verify step: When I open URL `$URL` in new window; Verify step: When I stop page loading
Meta:
    @requirementId 1154; 1236
When I open URL `${vividus-test-site-url}/delayedLoading?imageTimeout=1000` in new window
When I stop page loading
When I perform async javascript 'setTimeout(() => arguments[0]('wait for page to load'), 1000)' and save result to the 'scenario' variable 'timeout'
When I change context to element located `cssSelector(img)`
Then `${context-height}` is < `100`
Then `${context-width}`  is < `100`

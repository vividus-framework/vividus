Description: Integration tests for PageSteps class.

Meta:
    @epic vividus-plugin-web-app

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

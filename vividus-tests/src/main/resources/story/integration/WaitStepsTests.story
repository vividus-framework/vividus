Description: Integration tests for WaitSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification I wait until element located '$locator' disappears
Given I am on a page with the URL 'https://www.w3schools.com/jQuery/tryit.asp?filename=tryjquery_hide_slow'
When I switch to a frame with the attribute 'id'='iframeResult'
When I initialize the scenario variable `paragraph-locator` with value `By.xpath(.//p[text() = 'This is a paragraph with little content.'])`
Then number of elements found by `${paragraph-locator}` is equal to `1`
When I click on a button with the name 'Hide'
When I wait until element located `${paragraph-locator}` disappears
Then number of elements found by `${paragraph-locator}` is equal to `0`


Scenario: Verify step: When I wait until scroll finished
Given I am on a page with the URL '${vividus-test-site-url}/delayedScroll.html'
When I click on element located `By.id(anchor)`
When I wait until scroll is finished
When I execute sequence of actions:
|type          |argument                                        |
|CLICK         |By.id(toClick)                                  |
Then an alert is not present
Then an element with the id 'anchor' disappears


Scenario: Verify step: When I wait until scroll finished should not lock the tests when no scroll performing
When I refresh the page
When I wait until scroll is finished
When I execute sequence of actions:
|type          |argument                                        |
|CLICK         |By.id(toClick)                                  |
Then an alert is not present
Then an element with the id 'anchor' disappears

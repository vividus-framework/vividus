Description: Integration tests for WaitSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification I wait until element located '$locator' disappears
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
When I initialize the scenario variable `disappearing-locator` with value `By.id(element-to-hide)`
Then number of elements found by `${disappearing-locator}` is equal to `1`
When I click on element located `By.id(button-hide)`
When I wait until element located `${disappearing-locator}` disappears
Then number of elements found by `${disappearing-locator}` is equal to `0`


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

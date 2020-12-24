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

Scenario: Step verification I wait '$duration' with '$pollingDuration' polling until element located `$locator` becomes $state
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
Then number of elements found by `<element>` is equal to `<before>`
When I click on element located `<button>`
When I wait `PT10S` with `PT1S` polling until element located `<element>` becomes <state>
Then number of elements found by `<element>` is equal to `<after>`
Examples:
|button            |element               |state      |before|after|
|By.id(button-hide)|By.id(element-to-hide)|NOT_VISIBLE|1     |0    |
|By.id(button-show)|By.id(element-to-show)|VISIBLE    |0     |1    |

Scenario: Verify step: When I wait until elements with the name '$elementName' appear
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
Then number of elements found by `id(element-to-show)` is equal to `0`
When I click on element located `id(button-show)`
When I wait until elements with the name 'element-to-show' appear
Then number of elements found by `id(element-to-show)` is equal to `1`

Scenario: Verify step: 'When I wait until element located `$locator` appears'
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
Then number of elements found by `id(element-to-show)` is equal to `0`
When I click on element located `id(button-show)`
When I wait until element located `id(element-to-show)` appears
Then number of elements found by `id(element-to-show)` is equal to `1`

Scenario: Verify step: 'When I wait until element located `$locator` disappears'
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
Then number of elements found by `id(element-to-hide)` is equal to `1`
When I click on element located `id(button-hide)`
When I wait until element located `id(element-to-hide)` disappears
Then number of elements found by `id(element-to-hide)` is equal to `0`

Scenario: Verify step: 'Then element located `$locator` exists for `$duration` duration'
Then element located `id(button-hide)` exists for `PT1S` duration
Then number of elements found by `id(button-hide)` is equal to `1`

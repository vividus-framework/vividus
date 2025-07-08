Description: Integration tests for WaitSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: 'When I wait until element located by `$locator` stops moving'
Given I am on page with URL `${vividus-test-site-url}/movingElement.html`
When I click on element located by `buttonName(Start moving)`
When I wait until element located by `cssSelector(.internal)` stops moving
When I change context to element located by `cssSelector(.external)`
When I COMPARE_AGAINST baseline with name `moving-element-after-stopping#{eval(isWindows ? '-windows' : '')}`
When I reset context

Scenario: Step verification "When I wait until element located by '$locator' disappears"
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Given I initialize scenario variable `disappearing-locator` with value `By.id(element-to-hide)`
Then number of elements found by `${disappearing-locator}` is equal to `1`
When I click on element located by `id(button-hide)`
When I wait until element located by `${disappearing-locator}` disappears
Then number of elements found by `${disappearing-locator}` is equal to `0`


Scenario: Verify step: When I wait until scroll finished
Given I am on page with URL `${vividus-test-site-url}/delayedScroll.html`
When I click on element located by `id(anchor)`
When I wait until scroll is finished
When I execute sequence of actions:
|type          |argument                                        |
|CLICK         |By.id(toClick)                                  |
Then an alert is not present
When I wait until element located by `id(anchor)` disappears


Scenario: Verify step: When I wait until scroll finished should not lock the tests when no scroll performing
When I refresh page
When I wait until scroll is finished
When I execute sequence of actions:
|type          |argument                                        |
|CLICK         |By.id(toClick)                                  |
Then an alert is not present
When I wait until element located by `id(anchor)` disappears

Scenario: Step verification I wait '$duration' with '$pollingDuration' polling until element located by `$locator` becomes $state
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Then number of elements found by `<element>` is equal to `<before>`
When I click on element located by `<button>`
When I wait `PT10S` with `PT1S` polling until element located by `<element>` becomes <state>
Then number of elements found by `<element>` is equal to `<after>`
Examples:
|button            |element               |state      |before|after|
|By.id(button-hide)|By.id(element-to-hide)|NOT_VISIBLE|1     |0    |
|By.id(button-show)|By.id(element-to-show)|VISIBLE    |0     |1    |

Scenario: Verify step: 'When I set page load timeout to `$duration`'
Meta:
    @requirementId 2122
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
When I set page load timeout to `PT20S`
When I open URL `${vividus-test-site-url}/delayedLoading?imageTimeout=10000` in new tab
When I set page load timeout to `PT10S`

Scenario: Verify step: 'When I wait until number of elements located by `$locator` is $comparisonRule $number'
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
When I wait until number of elements located by `id(element-to-show):i` is = 1
When I click on element located by `id(button-show)`
When I wait until number of elements located by `id(element-to-show):i` is equal to 0
Then number of elements found by `id(element-to-show)` is equal to `1`

Scenario: Verify steps: "When I wait until alert appears" and "When I wait until alert disappears"
Given I am on page with URL `${vividus-test-site-url}/alertprompt.html`
When I click on element located by `buttonName(Survey)`
When I wait until alert appears
When I type text `` in alert and accept it
When I wait until alert disappears

Scenario: Verify step: 'When I wait until page title $comparisonRule `$pattern`'
When I wait until page title contains `Alert`

Scenario: Verify step: 'When I wait until frame with name `$frameName` appears and I switch to it'
Given I am on page with URL `${vividus-test-site-url}/frames.html`
When I wait until frame with name `exampleCom` appears and I switch to it
Then text `Example Domain` exists

Scenario: Verify step: 'Then element located by `$locator` appears in `$timeout`', 'When I wait until element located by `$locator` appears in `$timeout`'
When I wait until element located by `id(non-existent-element)` appears in `PT4S`
!-- Deprecated
Then element located by `id(non-existent-element)` appears in `PT4S`

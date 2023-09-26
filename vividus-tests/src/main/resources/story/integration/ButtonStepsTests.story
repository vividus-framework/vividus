Description: Integration tests for ButtonSteps class.

Meta:
    @epic vividus-plugin-web-app


Scenario: Deprecated step verification Then a radio button with the name '$radioOption' exists
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then a radio button with the name 'Email' exists

Scenario: Radio button locator verification
Then number of elements found by `radioButton(Owl)` is equal to `1`
Then number of NOT_SELECTED elements found by `radioButton(Owl)` is equal to `1`
When I click on element located by `radioButton(Owl)`
Then number of SELECTED elements found by `radioButton(Owl)` is equal to `1`

Scenario: Deprecated step verification Then a [$state] radio button with the name '$radioOption' exists
Then a [NOT_SELECTED] radio button with the name 'Mobile' exists
When I select a radio button with the name 'Mobile'
Then a [SELECTED] radio button with the name 'Mobile' exists

Scenario: Deprecated step verification Then element contains radio buttons:$radioOptions
When I change context to element located `xpath(//h3[text()='Radio button example:']/following-sibling::div)`
Then an element contains the radio buttons:
|radioOption|
|Email      |
|Mobile     |
|Owl        |
When I switch back to page


Scenario: Step verification a button with the name '$buttonName' exists
Then a button with the name 'counter' exists


Scenario: Step verification a [$state] button with the name '$buttonName' exists
Then a [ENABLED] button with the name 'counter' exists


Scenario: Step verification a button with the name '$buttonName' does not exist
Then a button with the name 'fake-button' does not exist

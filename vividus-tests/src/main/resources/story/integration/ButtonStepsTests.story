Description: Integration tests for ButtonSteps class.

Meta:
    @epic vividus-plugin-web-app


Scenario: Step verification Then a radio button with the name '$radioOption' exists
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
Then a radio button with the name 'Email' exists

Scenario: Step verification Then a [$state] radio button with the name '$radioOption' exists
Then a [NOT_SELECTED] radio button with the name 'Mobile' exists
When I select a radio button with the name 'Mobile'
Then a [SELECTED] radio button with the name 'Mobile' exists


Scenario: Step verification a button with the name '$buttonName' exists
Then a button with the name 'counter' exists


Scenario: Step verification a [$state] button with the name '$buttonName' exists
Then a [ENABLED] button with the name 'counter' exists


Scenario: Step verification a button with the name '$buttonName' does not exist
Then a button with the name 'fake-button' does not exist

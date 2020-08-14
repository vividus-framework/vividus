Description: Integration tests for CheckboxSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Set-Up
Given I am on a page with the URL '${vividus-test-site-url}/checkboxes.html'


Scenario: Validation of step: 'When I check a checkbox'
When I change context to element located `id(single)`
Then a [NOT_SELECTED] checkbox with the name 'One' exists
When I check a checkbox
Then a [SELECTED] checkbox with the name 'One' exists
When I refresh the page


Scenario: Validation of step: 'When I check all the checkboxes'
When I change context to element located `id(double)`
Then a [NOT_SELECTED] checkbox with the name 'Two' exists
Then a [NOT_SELECTED] checkbox with the name 'Three' exists
When I check all the checkboxes
Then a [SELECTED] checkbox with the name 'Two' exists
Then a [SELECTED] checkbox with the name 'Three' exists
When I refresh the page


Scenario: Validation of step: 'When I $checkBoxAction a checkbox with the name '$checkBoxName''
When I change context to element located `id(checked)`
Then a [SELECTED] checkbox with the name 'Four' exists
When I UNCHECK a checkbox with the name 'Four'
Then a [NOT_SELECTED] checkbox with the name 'Four' exists
When I refresh the page


Scenario: Validation of step: 'When I uncheck a checkbox with the attribute '$attributeType'='$attributeValue''
When I change context to element located `id(checked)`
Then a [SELECTED] checkbox with the name 'Four' exists
When I uncheck a checkbox with the attribute 'id'='four'
Then a [NOT_SELECTED] checkbox with the name 'Four' exists
When I refresh the page


Scenario: Validation of step: 'Then a checkbox with the name '$checkboxName' exists'
When I change context to element located `id(single)`
Then a checkbox with the name 'One' exists
When I switch back to the page


Scenario: Validation of step: 'Then a [$state] checkbox with the name '$checkboxName' exists'
When I change context to element located `id(checked)`
Then a [SELECTED] checkbox with the name 'Four' exists
When I switch back to the page


Scenario: Validation of step: 'Then a checkbox with the attribute '$attributeType'='$attributeValue' exists'
When I change context to element located `id(single)`
Then a checkbox with the attribute 'id'='one' exists
When I switch back to the page


Scenario: Validation of step: 'Then a [$state] checkbox with the attribute '$attributeType'='$attributeValue' exists'
When I change context to element located `id(checked)`
Then a [SELECTED] checkbox with the attribute 'id'='four' exists
When I switch back to the page


Scenario: Validateion of step: 'Then a checkbox with the name '$checkBox' does not exist'
Then a checkbox with the name '🙈 🙉 🙊' does not exist


Scenario: Validation of step: 'When I $checkBoxAction a checkbox by the xpath '$xpath''
When I change context to element located `id(single)`
When I CHECK a checkbox by the xpath '//input[@id='one']'
Then a [SELECTED] checkbox with the name 'One' exists
When I refresh the page


Scenario: Validation of step: 'When I check a checkbox with the attribute '$attributeType'='$attributeValue''
When I change context to element located `id(single)`
When I check a checkbox with the attribute 'id'='one'
When I CHECK a checkbox by the xpath '//input[@id='one']'
Then a [SELECTED] checkbox with the name 'One' exists

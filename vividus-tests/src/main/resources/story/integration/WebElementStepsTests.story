Description: Integration tests for WebElementsSteps class.

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/nestedFrames.html'


Scenario: Validation of step: 'Then a frame with the attribute '$attributeType'='$attributeValue' exists'
Then a frame with the attribute 'id'='parent' exists
Then number of elements found by `id(parent)` is = `1`


Scenario: Validation of step: 'Then a [$state] frame with the attribute '$attributeType'='$attributeValue' exists'
Then a [VISIBLE] frame with the attribute 'id'='parent' exists
Then number of elements found by `id(parent)` is = `1`

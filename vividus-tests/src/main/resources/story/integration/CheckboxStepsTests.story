Description: Integration tests for CheckboxSteps class.

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}/checkboxes.html`

Scenario: Validation of step: 'When I $checkboxAction checkbox located by `$searchAttributes`'
When I change context to element located by `id(single)`
Then number of not selected elements found by `checkboxName(One)` is equal to `1`
When I check checkbox located by `id(one)`
Then number of selected elements found by `checkboxName(One)` is equal to `1`
When I refresh page

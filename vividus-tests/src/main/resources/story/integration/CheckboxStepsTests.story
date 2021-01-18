Description: Integration tests for CheckboxSteps class.

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Before:
Scope: STORY
Given I am on a page with the URL '${vividus-test-site-url}/checkboxes.html'

Scenario: Validation of step: 'When I $checkboxAction checkbox located by `$searchAttributes`'
When I change context to element located `id(single)`
Then number of not selected elements found by `checkboxName(One)` is equal to `1`
When I check checkbox located by `id(one)`
Then number of selected elements found by `checkboxName(One)` is equal to `1`
When I refresh the page

Scenario: Validation of step 'When I $checkboxState all checkboxes located by `$checkboxesLocator`'
When I change context to element located `id(double)`
Then number of not selected elements found by `checkboxName(Two)` is equal to `1`
Then number of not selected elements found by `checkboxName(Three)` is equal to `1`
When I check all checkboxes located by `xpath(.//input)`
Then number of selected elements found by `checkboxName(Two)` is equal to `1`
Then number of selected elements found by `checkboxName(Three)` is equal to `1`

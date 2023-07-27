Meta:
    @epic vividus-plugin-web-app
    @feature locators

Scenario: Verify checkboxName(...) locator
Given I am on page with URL `${vividus-test-site-url}/checkboxes.html`
Then number of elements found by `checkboxName(One)` is equal to `1`

Scenario: Verify tagName(...) locator, index(...), state(...) and attribute(...) filters
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
When I save `id` attribute value of element located `tagName(button)->filter.index(2)` to SCENARIO variable `elementId`
Then `${elementId}` is equal to `button-show`
When I save `id` attribute value of element located `tagName(button)->filter.attribute(id=button-show)` to SCENARIO variable `elementId`
Then `${elementId}` is equal to `button-show`
When I save `id` attribute value of element located `tagName(div):a->filter.state(not visible)` to SCENARIO variable `elementId`
Then `${elementId}` is equal to `element-to-show`

Scenario: Verify unnormalizedXPath(...) locator
Meta:
    @requirementId 1546
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I enter `A  B` in field located by `xpath(//input[@id='text'])`
Then number of elements found by `unnormalizedXPath(//div[text()='A  B'])` is = `1`

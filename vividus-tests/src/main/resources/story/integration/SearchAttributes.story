Meta:
    @epic vividus-plugin-web-app
    @feature search-attributes

Scenario: Verify checkboxName(...) search attribute
Given I am on a page with the URL '${vividus-test-site-url}/checkboxes.html'
Then number of elements found by `checkboxName(One)` is equal to `1`


Scenario: Verify tagName(...) search attribute and index(...) filter
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
When I save `id` attribute value of element located `tagName(button):i->filter.index(2)` to SCENARIO variable `elementId`
Then `${elementId}` is equal to `button-show`

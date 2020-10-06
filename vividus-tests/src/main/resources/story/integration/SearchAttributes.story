Meta:
    @epic vividus-plugin-web-app
    @feature search-attributes

Scenario: Verify checkboxName(...) search attribute
Given I am on a page with the URL '${vividus-test-site-url}/checkboxes.html'
Then number of elements found by `checkboxName(One)` is equal to `1`

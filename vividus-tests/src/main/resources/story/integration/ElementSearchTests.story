Meta:
    @epic vividus-plugin-web-app

Scenario: Verify that caseInsensitiveText search excluding parent elements
Meta:
    @issueId 533
Given I am on page with URL `${vividus-test-site-url}/links.html`
Then number of elements found by `caseInsensitiveText(LINK TO AN ELEMENT)` is equal to `1`

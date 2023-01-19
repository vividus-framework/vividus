Meta:
  @requirementId 2516

Scenario: Should use dynamic locator
Given I am on page with URL `${vividus-test-site-url}`
Then number of elements found by `image(vividus-logo, Vividus Logo):a->filter.index(1)` is = `1`
Then number of elements found by `image-name(vividus-logo, Vividus Logo):a->filter.index(1)` is = `1`

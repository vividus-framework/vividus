Meta:
    @epic vividus-plugin-accessibility

Scenario: Validate against WCAG2A ignoring link-name and image-alt violations
Given I am on a page with the URL '${vividus-test-site-url}/accessibility.html'
When I perform accessibility scan:
|standard|violationsToIgnore |elementsToIgnore|
|WCAG2A  |link-name,image-alt|By.id(ignore)   |


Scenario: Validate html-has-lang and html-lang-valid violations
Given I am on a page with the URL '${vividus-test-site-url}/accessibility.html'
When I perform accessibility scan:
|violationsToCheck            |
|html-has-lang,html-lang-valid|


Scenario: Validate against default WCAG2xA tag ignoring link-name and image-alt violations
Given I am on a page with the URL '${vividus-test-site-url}/accessibility.html'
When I perform accessibility scan:
|standard|violationsToIgnore |elementsToIgnore|
|WCAG2xA |link-name,image-alt|By.id(ignore)   |

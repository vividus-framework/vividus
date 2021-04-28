Description: Integration tests for AccessibilitySteps class.

Meta:
    @epic vividus-plugin-accessibility

Scenario: Validate against WCAG2AAA
Given I am on a page with the URL '${vividus-test-site-url}/accessibility.html'
Then number of elements found by `id(errors)` is = `1`
When I change context to element located `xpath(//body)`
Then I test accessibility:
|standard|level |elementsToIgnore                                 |elementsToCheck|violationsToIgnore                                                                     |
|WCAG2AAA|NOTICE|By.id(ignore), By.cssSelector(#errors > h1 > img)|               |WCAG2AAA.Principle1.Guideline1_3.1_3_1.H42.2,WCAG2AAA.Principle2.Guideline2_4.2_4_9.H30|
|WCAG2AAA|NOTICE|By.xpath(//*)                                    |               |                                                                                       |


Scenario: Validate against Section 508
When I switch back to the page
Then I test accessibility:
|standard   |level  |elementsToCheck  |violationsToIgnore       |
|Section_508|WARNING|xpath(//img)     |Section508.A.Img.Ignored |

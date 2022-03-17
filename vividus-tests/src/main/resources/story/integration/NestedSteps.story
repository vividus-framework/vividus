Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step "When I find $comparisonRule '$number' elements by $locator and for each element do$stepsToExecute"
Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
When I find = `2` elements by `By.xpath(//div):a` and for each element do
|step                                                                                        |
|When I set 'id' attribute value of the context element to the 'scenario' variable 'idValue' |
|Then `${idValue}` matches `element-to-.*`                                                   |

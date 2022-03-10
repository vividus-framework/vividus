Meta:
    @epic vividus-extension-selenium


Scenario: Verify step: When I find $comparisonRule `$number` elements `$locator` and while they exist do up to $iterationLimit iteration of$stepsToExecute
Meta:
    @requirementId 2054

Given I am on a page with the URL '${vividus-test-site-url}/elementState.html'
When I find > `0` elements `id(element-to-hide)` and while they exist do up to 10 iteration of
|step                                             |
|When I click on element located `id(button-hide)`|

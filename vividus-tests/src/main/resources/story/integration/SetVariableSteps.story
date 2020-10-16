Meta:
    @epic vividus-extension-selenium

Scenario: Verify step: "When I set the text found in search context to the '$scopes' variable '$variableName'"
Given I am on a page with the URL '${vividus-test-site-url}/inputs.html'
When I enter `text` in field located `By.id(text)`
When I change context to element located `By.id(output)`
When I set the text found in search context to the 'SCENARIO' variable 'variableName'
Then `text` is equal to `${variableName}`
When I change context to the page


Scenario: Verify step: "When I set '$attributeName' attribute value of the context element to the '$scopes' variable '$variableName'"
When I change context to element located `By.id(text)`
When I set 'name' attribute value of the context element to the 'SCENARIO' variable 'variableName'
Then `text` is equal to `${variableName}`
When I change context to the page


Scenario: Verify step: "When I set '$attributeName' attribute value of the element by $locator to the $scopes variable '$variableName'"
When I set 'name' attribute value of the element by By.id(text) to the SCENARIO variable 'variableName'
Then `text` is equal to `${variableName}`


Scenario: Verify step: "When I save text of context element to $scopes variable `$variableName`"
When I enter `text` in field located `By.id(text)`
When I change context to element located `By.id(output)`
When I save text of context element to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`
When I change context to the page


Scenario: Verify step: "When I save `$attributeName` attribute value of context element to $scopes variable `$variableName`"
When I change context to element located `By.id(text)`
When I save `name` attribute value of context element to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`
When I change context to the page


Scenario: Verify step: "When I save `$attributeName` attribute value of element located `$locator` to $scopes variable `$variableName`"
When I save `name` attribute value of element located `By.id(text)` to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`

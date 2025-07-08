Meta:
    @epic vividus-extension-selenium; vividus-plugin-web-app

Scenario: Verify step: "When I set the text found in search context to the '$scopes' variable '$variableName'"
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I enter `text` in field located by `id(text)`
When I change context to element located by `id(output)`
When I set the text found in search context to the 'SCENARIO' variable 'variableName'
Then `text` is equal to `${variableName}`
When I reset context


Scenario: Verify step: "When I set '$attributeName' attribute value of the context element to the '$scopes' variable '$variableName'"
When I change context to element located by `id(text)`
When I set 'name' attribute value of the context element to the 'SCENARIO' variable 'variableName'
Then `text` is equal to `${variableName}`
When I reset context


Scenario: Verify step: "When I set '$attributeName' attribute value of the element by $locator to the $scopes variable '$variableName'"
When I set 'name' attribute value of the element by By.id(text) to the SCENARIO variable 'variableName'
Then `text` is equal to `${variableName}`


Scenario: Verify step: "When I save text of context element to $scopes variable `$variableName`", "When I save text of element located by `$locator` to $scopes variable `$variableName`"
When I enter `text` in field located by `id(text)`
When I change context to element located by `id(output)`
When I save text of context element to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`
When I reset context
When I save text of element located by `id(output)` to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`


Scenario: Verify step: "When I save `$attributeName` attribute value of context element to $scopes variable `$variableName`"
When I change context to element located by `id(text)`
When I save `name` attribute value of context element to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`
When I reset context


Scenario: Verify step: "When I save `$attributeName` attribute value of element located by `$locator` to $scopes variable `$variableName`"
When I save `name` attribute value of element located by `id(text)` to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`
!-- Deprecated
When I save `name` attribute value of element located `By.id(text)` to SCENARIO variable `variableName`
Then `text` is equal to `${variableName}`


Scenario: Verify step: "When I save `$attributeName` attribute value of element located by `$locator` to $scopes variable `$variableName`" for element with shadowCssSelector
Meta:
    @requirementId 1494
Given I am on page with URL `${vividus-test-site-url}/shadowDom.html`
When I save `class` attribute value of element located by `shadowCssSelector(#shadow-upper-host; #shadow-inner-host; .target-element):in` to SCENARIO variable `variableName`
Then `target-element` is equal to `${variableName}`


Scenario: Verify save number of elements steps
Given I am on page with URL `${vividus-test-site-url}/links.html`
When I save number of elements located by `tagName(a)` to SCENARIO variable `numberOfLinks`
Then `${numberOfLinks}` is equal to `5`
!-- Deprecated
When I save number of elements located `tagName(a)` to SCENARIO variable `numberOfLinks`
Then `${numberOfLinks}` is equal to `5`

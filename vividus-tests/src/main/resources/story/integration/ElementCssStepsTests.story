Description: Description: Integration tests for features working with CSS properties.

Meta:
    @epic vividus-plugin-web-app

Scenario: Deprecated step verification Then the context element has the CSS property '$cssName'='$cssValue'
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then the context element has the CSS property 'color'='rgba(0, 0, 0, 1)'

Scenario: Step verification context element has CSS property `$cssName` with value that $comparisonRule `$cssValue`
Meta:
    @playwrightSupported

Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then context element has CSS property `background-color` with value that is equal to `rgba(0, 0, 0, 0)`

Scenario: Deprecated step verification Then the context element has the CSS property '$cssName' containing '$cssValue'
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then the context element has the CSS property 'color' containing '(0, 0, 0, 1)'

Scenario: Verify step: "When I save `$cssProperty` CSS property value of element located by `$locator` to $scopes variable `$variableName`"
Given I am on page with URL `${vividus-test-site-url}`
When I save `background-image` CSS property value of element located by `xpath(//body)` to scenario variable `cssPropertyValue`
Then `${cssPropertyValue}` is = `none`

Scenario: Step verification Then context element has CSS properties
Meta:
    @playwrightSupported

Given I am on page with URL `${vividus-test-site-url}/relativeLocator.html`
When I change context to element located by `id(block1)`
Then context element does have CSS properties matching rules:
|cssProperty |comparisonRule |expectedValue |
|align-items |is equal to    |center        |
|border      |contains       |solid         |
|font-size   |contains       |20            |
|font-size   |is equal to    |20px          |

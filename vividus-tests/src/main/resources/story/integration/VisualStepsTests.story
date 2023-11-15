Description: System tests for Visual plugin steps

Meta:
    @epic vividus-plugin-visual

Scenario: Validation shootingStrategy visual testing on a page with scrollable element with ignores
Given I am on page with URL `${vividus-test-site-url}/visualTestIntegration.html`
When I compare_against baseline with name `visual-simple-shootingStrategy-with-ignores` using storage `filesystem` and ignoring:
|ELEMENT      |
|xpath(//a[1])|
and screenshot configuration:
|shootingStrategy|
|SIMPLE          |
When I compare_against baseline with name `visual-viewportPasting-shootingStrategy-with-ignores` using storage `filesystem` and ignoring:
|ELEMENT      |
|xpath(//a[1])|
and screenshot configuration:
|shootingStrategy|
|VIEWPORT_PASTING|

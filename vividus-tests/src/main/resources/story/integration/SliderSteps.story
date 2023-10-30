Meta:
    @epic vividus-plugin-web-app

Scenario: Verify step: "When I set value `$value` in slider located by `$locator`" and "Then value `$value` is selected in slider located by `$locator`"
Given I am on page with URL `${vividus-test-site-url}/slider.html`
Then value `10` is selected in slider located by `id(slider)`
When I set value `90` in slider located by `id(slider)`
Then value `90` is selected in slider located by `id(slider)`

Scenario: Verify deprecated steps: "When I select the value '$value' in a slider by the xpath '$xpath'" and "Then the value '$value' is selected in a slider by the xpath '$xpath'"
When I select the value '50' in a slider by the xpath '//*[@id='slider']'
Then the value '50' is selected in a slider by the xpath '//*[@id='slider']'

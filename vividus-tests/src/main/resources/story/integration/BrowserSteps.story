Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification "When I close browser"
Given I am on main application page
Given I initialize scenario variable `item-key` with value `#{generate(regexify '[a-z]{10}')}`
Given I initialize scenario variable `item-value` with value `#{generate(regexify '[a-z]{10}')}`
When I set local storage item with key `${item-key}` and value `${item-value}`
When I close browser
Given I am on main application page
When I save local storage item with key `${item-key}` to scenario variable `value-on-new-browser`
Then `${value-on-new-browser:null}` is equal to `null`

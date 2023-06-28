Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
|windowsTitle|indexTitle       |
|Windows     |Vividus Test Site|


Scenario: Verify deprecated step with "is equal to": "When I switch to window with title that $stringComparisonRule `$title`"; Verify deprecated composite step "Then the page title $comparisonRule '$text'"
Given I am on page with URL `${vividus-test-site-url}/windows.html`
Then the page title is equal to '<windowsTitle>'
When I click on element located by `id(plain)`
When I switch to window with title that is equal to `<indexTitle>`
Then page title is equal to `<indexTitle>`
When I close the current window
Then page title is equal to `<windowsTitle>`


Scenario: Verify steps: with "contains": "When I switch to tab with title that $stringComparisonRule `$title`" and "When I go to relative URL `$relativeURL`"; Verify step "Then page title $comparisonRule `$text`"
Given I am on main application page
When I go to relative URL `/windows.html`
Then page title is equal to `<windowsTitle>`
When I click on element located by `id(plain)`
When I switch to tab with title that contains `<indexTitle>`
Then page title is equal to `<indexTitle>`
When I close current tab
Then page title is equal to `<windowsTitle>`


Scenario: Verify steps: "When I wait `$duration` until tab with title that $comparisonRule `$tabTitle` appears and switch to it" and "When I go to relative URL `$relativeURL`"
Given I am on main application page
When I go to the relative URL 'windows.html'
Then page title is equal to `<windowsTitle>`
When I click on element located by `id(timeout)`
When I wait `PT3S` until tab with title that is equal to `<indexTitle>` appears and switch to it
Then page title is equal to `<indexTitle>`
When I close current tab
Then page title is equal to `<windowsTitle>`

Scenario: Verify deprecated step: "When I wait `$duration` until window with title that $comparisonRule `$windowTitile` appears and switch to it"
Given I am on main application page
When I go to the relative URL 'windows.html'
Then page title is equal to `<windowsTitle>`
When I click on element located by `id(timeout)`
When I wait `PT3S` until window with title that is equal to `<indexTitle>` appears and switch to it
Then page title is equal to `<indexTitle>`
When I close current tab
Then page title is equal to `<windowsTitle>`


Scenario: Verify step: "When I switch to frame located `$locator`"
Given I am on page with URL `${vividus-test-site-url}/nestedFrames.html`
When I change context to element located by `id(toRemove):a`
When I execute javascript `
document.querySelector('#toRemove').remove();
return [];
` and save result to scenario variable `result`
When I switch to frame located `id(parent)`
When I switch to frame located `id(exampleCom)`
When I click on element located by `xpath(//a)`


Scenario: Verify steps: "When I reset context" AND "When I change context to element located by `$locator`"
When I change context to element located by `xpath(//body)`
Then number of elements found by `By.xpath(html)` is equal to `0`
When I reset context
Then number of elements found by `By.xpath(html)` is equal to `1`


Scenario: Verify step: "When I change context to element located by `$locator` in scope of current context"
Given I am on page with URL `${vividus-test-site-url}`
When I change context to element located by `xpath(//a)`
When I change context to element located `xpath(.//*)` in scope of current context
When I save `name` attribute value of context element to scenario variable `name`
Then `${name}` is = `vividus-logo`


Scenario: Verify step: "When I reset context"
When I change context to element located by `xpath(//body)`
Then number of elements found by `By.xpath(html)` is equal to `0`
When I reset context
Then number of elements found by `By.xpath(html)` is equal to `1`


Scenario: Should switch to first visible parent frame or main document if the current frame is closed
Given I am on page with URL `${vividus-test-site-url}/frames.html`
When I click on element located by `id(modalButton)`
When I wait until element located by `id(modalWindow)` appears
When I switch to frame located `id(firstFrame)`
When I switch to frame located `id(secondFrame)`
When I click on element located by `id(close)`
Then number of elements found by `id(modalButton)` is equal to `1`


Scenario: Verify context healing
Given I am on page with URL `${vividus-test-site-url}`
When I change context to element located by `tagName(a)`
When I execute javascript `location.reload();` with arguments:
Then number of elements found by `cssSelector(img)` is = `1`


Scenario: Verify deprecated step: "When I attempt to close current window with possibility to handle alert" with alert
Meta:
    @requirementId 2314
When I open URL `${vividus-test-site-url}/onbeforeunloadAlert.html` in new tab
Then an alert is not present
When I click on element located by `xpath(//a[text() = 'here'])`
When I attempt to close current window with possibility to handle alert
Then an alert is present
When I dismiss alert with message which matches `.*`
When I wait until an alert disappears
Then an alert is not present
When I attempt to close current window with possibility to handle alert
Then an alert is present
When I accept alert with message which matches `.*`
When I switch to tab with title that is equal to `Vividus Test Site`
Then number of elements found by `By.xpath(//img[@name='vividus-logo'])` is equal to `1`

Scenario: Verify step: "When I attempt to close current tab with possibility to handle alert" with alert
Meta:
    @requirementId 2314
When I open URL `${vividus-test-site-url}/onbeforeunloadAlert.html` in new tab
Then an alert is not present
When I click on element located by `xpath(//a[text() = 'here'])`
When I attempt to close current tab with possibility to handle alert
Then an alert is present
When I dismiss alert with message which matches `.*`
When I wait until an alert disappears
Then an alert is not present
When I attempt to close current tab with possibility to handle alert
Then an alert is present
When I accept alert with message which matches `.*`
When I switch to tab with title that is equal to `Vividus Test Site`
Then number of elements found by `By.xpath(//img[@name='vividus-logo'])` is equal to `1`

Scenario: Verify step: "When I attempt to close current tab with possibility to handle alert" without alert
Meta:
    @requirementId 2314
Given I am on page with URL `${vividus-test-site-url}`
When I open URL `${vividus-test-site-url}/onbeforeunloadAlert.html` in new tab
Then an alert is not present
When I attempt to close current tab with possibility to handle alert
Then number of elements found by `By.xpath(//img[@name='vividus-logo'])` is equal to `1`

Scenario: Verify step: "When I open new tab" (new tab doesn't inherit the state of the previous tab and can't handle alert)
When I open new tab
Given I am on page with URL `${vividus-test-site-url}/onbeforeunloadAlert.html`
Then an alert is not present
When I click on element located by `xpath(//a[text() = 'here'])`
!-- No alert should be shown and tab should be kept open, but focus should be switched to another tab
When I attempt to close current tab with possibility to handle alert
Then page title is equal to `Vividus Test Site`
Then number of elements found by `By.xpath(//img[@name='vividus-logo'])` is equal to `1`

Meta:
    @group plugin-web-app

Lifecycle:
Examples:
|windowsTitle|indexTitle       |vivdus-test-site                                           |
|Windows     |Vividus Test Site|https://vividus-test-site.herokuapp.com/windows.html|


Scenario: Verify step: "When I switch to a window with the name '$windowName'"
Given I am on a page with the URL '<vivdus-test-site>'
Then the page title is equal to '<windowsTitle>'
When I click on element located `id(plain)`
When I switch to a window with the name '<indexTitle>'
Then the page title is equal to '<indexTitle>'
When I close the current window
Then the page title is equal to '<windowsTitle>'


Scenario: Verify step: "When I switch to a window with the name containing '$windowPartName'"
Given I am on a page with the URL '<vivdus-test-site>'
Then the page title is equal to '<windowsTitle>'
When I click on element located `id(plain)`
When I switch to a window with the name containing '<indexTitle>'
Then the page title is equal to '<indexTitle>'
When I close the current window
Then the page title is equal to '<windowsTitle>'


Scenario: Verify step: "When I wait `$duration` until window with title that $comparisonRule `$windowTitile` appears and switch to it"
Given I am on a page with the URL '<vivdus-test-site>'
Then the page title is equal to '<windowsTitle>'
When I click on element located `id(timeout)`
When I wait `PT3S` until window with title that is equal to `<indexTitle>` appears and switch to it
Then the page title is equal to '<indexTitle>'
When I close the current window
Then the page title is equal to '<windowsTitle>'

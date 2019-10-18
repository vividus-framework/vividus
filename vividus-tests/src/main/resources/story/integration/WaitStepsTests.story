Description: Integration tests for WaitSteps class.

Meta:
    @group vividus-plugin-web-app

Scenario: Step verification I wait until element located '$locator' disappears
Given I am on a page with the URL 'https://www.w3schools.com/jQuery/tryit.asp?filename=tryjquery_hide_slow'
When I switch to a frame with the attribute 'id'='iframeResult'
When I initialize the scenario variable `paragraph-locator` with value `By.xpath(.//p[text() = 'This is a paragraph with little content.'])`
Then number of elements found by `${paragraph-locator}` is equal to `1`
When I click on a button with the name 'Hide'
When I wait until element located `${paragraph-locator}` disappears
Then number of elements found by `${paragraph-locator}` is equal to `0`

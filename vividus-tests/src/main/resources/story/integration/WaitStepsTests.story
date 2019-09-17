Description: Integration tests for WaitSteps class.

Meta:
    @group vividus-plugin-web-app

Scenario: Step verification I wait until element located '$locator' disappears
Given I am on a page with the URL 'https://www.mkyong.com/wp-content/uploads/jQuery/jQuery-fadeIn-faceOut-fadeTo-example.html'
Then number of elements found by `By.xpath(.//*[@class='fadeOutbox'])` is equal to `2`
When I click on element located `By.xpath(.//*[@class='fadeOutbox'][1])`
When I wait until element located `By.xpath(.//*[@class='fadeOutbox'][1])` disappears
Then number of elements found by `By.xpath(.//*[@class='fadeOutbox'])` is equal to `1`

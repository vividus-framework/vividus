Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification 'When I drag element located `$origin` and drop it at $location of element located `$target`'
Given I am on a page with the URL 'https://4qp6vjp319.codesandbox.io/'
When I wait until element located `By.xpath(//div[@id='root']/ul)` appears
When I change context to element located `By.xpath(//div[@id='root']/ul)`
Then the text matches 'item 0.*item 1.*item 2.*item 3.*'
When I drag element located `By.xpath(//li[contains(., 'item 0')])` and drop it at top of element located `By.xpath(//li[contains(., 'item 3')])`
Then the text matches 'item 1.*item 2.*item 0.*item 3.*'

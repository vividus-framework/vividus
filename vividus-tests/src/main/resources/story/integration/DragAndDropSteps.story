Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification 'When I drag element located `$origin` and drop it at $location of element located `$target`'
Given I am on page with URL `https://4qp6vjp319.codesandbox.io/`
When I click on element located by `linktext(Yes, proceed to preview)`
When I wait until element located by `xpath(//div[@id='root']/ul)` appears
When I change context to element located by `xpath(//div[@id='root']/ul)`
Then text matches `item 0.*item 1.*item 2.*item 3.*item 4.*item 5.*item 6.*`
When I drag element located by `By.xpath(//li[contains(., 'item 0')])` and drop it at top of element located by `By.xpath(//li[contains(., 'item 3')])`
Then text matches `item 1.*item 2.*item 0.*item 3.*item 4.*item 5.*item 6.*`
When I wait until element located by `xpath(//div[@id='root']/ul)` appears
When I drag element located `By.xpath(//li[contains(., 'item 2')])` and drop it at top of element located `By.xpath(//li[contains(., 'item 5')])`
Then text matches `item 1.*item 0.*item 3.*item 4.*item 2.*item 5.*item 6.*`

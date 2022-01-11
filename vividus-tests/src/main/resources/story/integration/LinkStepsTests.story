Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
|linksUrl                           |
|${vividus-test-site-url}/links.html|


Scenario: Step verification Then context contains list of link items with the text: $expectedLinkItems
Given I am on a page with the URL '<linksUrl>'
When I change context to element located `By.xpath(//body)`
Then context contains list of link items with the text:
|text              |
|Link to an element|
|Link with tooltip |


Scenario: Step verification Then context contains list of link items with the text and link: $expectedLinkItems
Then context contains list of link items with the text and link:
|text              |link      |
|Link to an element|#ElementId|
|Link with tooltip |#         |

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


Scenario: Step verification When I hover a mouse over a link with the text '$text'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `0`
When I hover a mouse over a link with the text 'Link with tooltip'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `1`

Scenario: Step verification When I click on a link with the CSS selector '$cssSelector'
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
Then number of elements found by `xpath(//div[@id="clickResult" and text() = '1'])` is equal to `0`
When I click on element located `By.cssSelector(#counter)`
Then number of elements found by `xpath(//div[@id="clickResult" and text() = '1'])` is equal to `1`

Scenario: Step verification When I hover a mouse over a link with the URL '$URL'
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/links.html'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `0`
When I hover a mouse over a link with the URL '#'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `1`

Scenario: Step verification When I click on a link with the URL containing '$URLpart' and the text '$text'
Given I am on a page with the URL '<linksUrl>'
When I click on a link with the URL containing 'ementId' and the text 'Link to an element'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on an image with the tooltip '$tooltipImage'
Given I am on a page with the URL 'http://www.echoecho.com/htmllinks06.htm'
When I click on an image with the tooltip 'Link to this page'
Then the page with the URL 'http://www.echoecho.com/myfile.htm' is loaded

Scenario: Step verification When I click on a link with the text '$text' and URL '$URL'
Given I am on a page with the URL '<linksUrl>'
When I click on a link with the text 'Link to an element' and URL '#ElementId'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the text '$text'
Given I am on a page with the URL '<linksUrl>'
When I click on a link with the text 'Link to an element'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the URL '$URL'
Given I am on a page with the URL '<linksUrl>'
When I click on a link with the URL '#ElementId'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the URL containing '$URLpart'
Given I am on a page with the URL '${vividus-test-site-url}/index.html'
When I click on a link with the URL containing 'nks.html'
Then the page with the URL '<linksUrl>' is loaded

Scenario: Step verification Then a link with the text '$text' does not exist
Given I am on a page with the URL '<linksUrl>'
Then a link with the text 'Link to an element' exists
When I change context to element located `By.xpath(//a[@href= '#'])`
Then a link with the text 'Link to an element' does not exist

Scenario: Step verification Then a link with the text '$text' and URL '$URL' does not exist
When I switch back to the page
Then a link with the text 'Link to an element' and URL '#ElementId' exists
When I change context to element located `By.xpath(//a[@href= '#'])`
Then a link with the text 'Link to an element' and URL '#ElementId' does not exist

Scenario: Step verification: Then a link with the URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL '<linksUrl>'
Then a link with the URL '#' and tooltip 'Link title' exists

Scenario: Step verification: Then a [$state] link with the text '$text' and tooltip '$tooltip' exists
Then a [VISIBLE] link with the text 'Link with tooltip' and tooltip 'Link title' exists

Scenario: Step verification: Then a [$state] link with the URL '$URL' and tooltip '$tooltip' exists
Then a [VISIBLE] link with the URL '#' and tooltip 'Link title' exists

Scenario: Step verification: Then a link with the tooltip '$tooltip' exists
Then a link with the tooltip 'Link title' exists

Scenario: Step verification: Then a [$state] link with the tooltip '$tooltip' exists
Then a [VISIBLE] link with the tooltip 'Link title' exists

Scenario: Step verification: Then a link with the URL '$URL' and tooltip '$tooltip' does not exist
When I click on element located `By.xpath(//a[@title='Link title'])`
Then a link with the URL '#' and tooltip 'Link title' does not exist

Scenario: Step verification Then a link with the text '$text' and URL '$URL' and tooltip '$tooltip' does not exist
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to frame located `By.id(iframeResult)`
Then a link with the text 'Visit our HTML Tutorial' and tooltip 'Go to W3Schools HTML section' exists
When I click on a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/'
Then a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' does not exist

Scenario: Step verification Then a link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to frame located `By.id(iframeResult)`
Then a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a [$state] link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to frame located `By.id(iframeResult)`
Then a [VISIBLE] link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a [$state] link with text '$text' and URL containing '$URLpart' exists
Given I am on a page with the URL '<linksUrl>'
Then a [VISIBLE] link with text 'Link to an element' and URL containing '#ElementId' exists

Scenario: Step verification Then a link with the text '$text' and URL containing '$URLpart' exists
Given I am on a page with the URL '<linksUrl>'
Then a link with the text 'Link to an element' and URL containing 'ementId' exists

Scenario: Step verification Then a link with the text '$text' and URL '$URL' exists
Given I am on a page with the URL '<linksUrl>'
Then a link with the text 'Link to an element' and URL '#ElementId' exists

Scenario: Step verification Then a [$state] link with the text '$text' and URL '$URL' exists
Given I am on a page with the URL '<linksUrl>'
Then a [VISIBLE] link with the text 'Link to an element' and URL '#ElementId' exists

Scenario: Step verification Then a link tag with href '$href' exists
Given I am on a page with the URL '<linksUrl>'
Then a link tag with href 'img/vividus.png' exists

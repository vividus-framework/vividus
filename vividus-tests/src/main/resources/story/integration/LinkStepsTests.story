Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification Then context contains list of link items with the text: $expectedLinkItems
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Link'
When I change context to an element by By.cssSelector(.standard-table)
Then context contains list of link items with the text:
|text                                                    |
|RFC 8288, section 3: Link Serialisation in HTTP Headers |
|RFC 5988, section 5: The Link Header Field              |

Scenario: Step verification Then context contains list of link items with the text and link: $expectedLinkItems
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Link'
When I change context to an element by By.cssSelector(.standard-table)
Then context contains list of link items with the text and link:
|text                                                    |link                                         |
|RFC 8288, section 3: Link Serialisation in HTTP Headers |https://tools.ietf.org/html/rfc8288#section-3|
|RFC 5988, section 5: The Link Header Field              |https://tools.ietf.org/html/rfc5988#section-5|

!-- Composites down there

Scenario: Step verification When I hover a mouse over a link with the text '$text'
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/CSS/:hover$samples/Basic_example?revision=1544989#'
When I hover a mouse over a link with the text 'Try hovering over this link.'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `1`

Scenario: Step verification When I click on a link with the CSS selector '$cssSelector'
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/CSS/@media/any-hover'
When I click on element located `By.cssSelector(.page-header > a.logo)`
Then the page with the URL 'https://developer.mozilla.org/en-US/' is loaded

Scenario: Step verification When I hover a mouse over a link with the URL '$URL'
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/CSS/:hover$samples/Basic_example?revision=1544989#'
When I hover a mouse over a link with the URL '#'
Then number of elements found by `By.cssSelector(body > a:hover)` is equal to `1`

Scenario: Step verification When I click on a link with the URL containing '$URLpart' and the text '$text'
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
When I click on a link with the URL containing 'ementId' and the text 'Link to an element'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on an image with the tooltip '$tooltipImage'
Given I am on a page with the URL 'http://www.echoecho.com/htmllinks06.htm'
When I click on an image with the tooltip 'Link to this page'
Then the page with the URL 'http://www.echoecho.com/myfile.htm' is loaded

Scenario: Step verification When I click on a link with the text '$text' and URL '$URL'
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
When I click on a link with the text 'Link to an element' and URL '#ElementId'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the text '$text'
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
When I click on a link with the text 'Link to an element'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the URL '$URL'
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
When I click on a link with the URL '#ElementId'
Then the page with the URL '${vividus-test-site-url}/links.html#ElementId' is loaded

Scenario: Step verification When I click on a link with the URL containing '$URLpart'
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/'
When I click on a link with the URL containing '-US/docs/MDN/About#Copyrights_and_licenses'
Then the page with the URL 'https://developer.mozilla.org/en-US/docs/MDN/About#Copyrights_and_licenses' is loaded

Scenario: Step verification Then a link with the text '$text' does not exist
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/CSS/@media/any-hover'
Then a link with the text 'CSS: Cascading Style Sheets' exists
When I change context to an element by By.cssSelector(.page-header)
Then a link with the text 'CSS: Cascading Style Sheets' does not exist

Scenario: Step verification Then a link with the text '$text' and URL '$URL' does not exist
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/CSS/@media/any-hover'
Then a link with the text 'CSS: Cascading Style Sheets' and URL '/en-US/docs/Web/CSS' exists
When I change context to an element by By.cssSelector(.page-header)
Then a link with the text 'CSS: Cascading Style Sheets' and URL '/en-US/docs/Web/CSS' does not exist

Scenario: Step verification Then a link with the URL '$URL' and tooltip '$tooltip' does not exist
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP'
When I change context to an element by By.xpath(//div[@class='quick-links'])
When I click on element located `By.xpath(//*[text()='HTTP headers'])`
Then a link with the URL '/en-US/docs/Web/HTTP/Headers/Accept' and tooltip 'The Accept request HTTP header advertises which content types, expressed as MIME types, the client is able to understand. Using content negotiation, the server then selects one of the proposals, uses it and informs the client of its choice with the Content-Type response header. Browsers set adequate values for this header depending on the context where the request is done: when fetching a CSS stylesheet a different value is set for the request than when fetching an image, video or a script.' exists
When I click on element located `By.xpath(//*[text()='HTTP headers'])`
Then a link with the URL '/en-US/docs/Web/HTTP/Headers/Accept' and tooltip 'The Accept request HTTP header advertises which content types, expressed as MIME types, the client is able to understand. Using content negotiation, the server then selects one of the proposals, uses it and informs the client of its choice with the Content-Type response header. Browsers set adequate values for this header depending on the context where the request is done: when fetching a CSS stylesheet a different value is set for the request than when fetching an image, video or a script.' does not exist

Scenario: Step verification Then a link with the text '$text' and URL '$URL' and tooltip '$tooltip' does not exist
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to a frame with the attribute 'id'='iframeResult'
Then a link with the text 'Visit our HTML Tutorial' and tooltip 'Go to W3Schools HTML section' exists
When I click on a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/'
Then a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' does not exist

Scenario: Step verification Then a link with the text '$text' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to a frame with the attribute 'id'='iframeResult'
Then a link with the text 'Visit our HTML Tutorial' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a [$state] link with the text '$text' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to a frame with the attribute 'id'='iframeResult'
Then a [VISIBLE] link with the text 'Visit our HTML Tutorial' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a link with the URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP'
When I change context to an element by By.xpath(//div[@class='quick-links'])
When I click on element located `By.xpath(//*[text()='HTTP headers'])`
Then a link with the URL '/en-US/docs/Web/HTTP/Headers/Accept' and tooltip 'The Accept request HTTP header advertises which content types, expressed as MIME types, the client is able to understand. Using content negotiation, the server then selects one of the proposals, uses it and informs the client of its choice with the Content-Type response header. Browsers set adequate values for this header depending on the context where the request is done: when fetching a CSS stylesheet a different value is set for the request than when fetching an image, video or a script.' exists

Scenario: Step verification Then a [$state] link with the URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP'
When I change context to an element by By.xpath(//div[@class='quick-links'])
When I click on element located `By.xpath(//*[text()='HTTP headers'])`
Then a [VISIBLE] link with the URL '/en-US/docs/Web/HTTP/Headers/Accept' and tooltip 'The Accept request HTTP header advertises which content types, expressed as MIME types, the client is able to understand. Using content negotiation, the server then selects one of the proposals, uses it and informs the client of its choice with the Content-Type response header. Browsers set adequate values for this header depending on the context where the request is done: when fetching a CSS stylesheet a different value is set for the request than when fetching an image, video or a script.' exists

Scenario: Step verification Then a link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to a frame with the attribute 'id'='iframeResult'
Then a link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a [$state] link with the text '$text' and URL '$URL' and tooltip '$tooltip' exists
Given I am on a page with the URL 'https://www.w3schools.com/html/tryit.asp?filename=tryhtml_links_title'
When I switch to a frame with the attribute 'id'='iframeResult'
Then a [VISIBLE] link with the text 'Visit our HTML Tutorial' and URL 'https://www.w3schools.com/html/' and tooltip 'Go to W3Schools HTML section' exists

Scenario: Step verification Then a [$state] link with text '$text' and URL containing '$URLpart' exists
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
Then a [VISIBLE] link with text 'Link to an element' and URL containing '#ElementId' exists

Scenario: Step verification Then a link with the text '$text' and URL containing '$URLpart' exists
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
Then a link with the text 'Link to an element' and URL containing 'ementId' exists

Scenario: Step verification Then a link with the text '$text' and URL '$URL' exists
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
Then a link with the text 'Link to an element' and URL '#ElementId' exists

Scenario: Step verification Then a [$state] link with the text '$text' and URL '$URL' exists
Given I am on a page with the URL '${vividus-test-site-url}/links.html'
Then a [VISIBLE] link with the text 'Link to an element' and URL '#ElementId' exists

Scenario: Step verification Then a link tag with href '$href' exists
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/'
Then a link tag with href 'https://developer.mozilla.org/en-US/search/xml' exists

Scenario: Step verification Then a link with the tooltip '$tooltip' exists
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Expect'
Then a link with the tooltip 'The Connection general header controls whether or not the network connection stays open after the current transaction finishes. If the value sent is keep-alive, the connection is persistent and not closed, allowing for subsequent requests to the same server to be done.' exists

Scenario: Step verification Then a [$state] link with the tooltip '$tooltip' exists
Given I am on a page with the URL 'https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Expect'
Then a [VISIBLE] link with the tooltip 'The Connection general header controls whether or not the network connection stays open after the current transaction finishes. If the value sent is keep-alive, the connection is persistent and not closed, allowing for subsequent requests to the same server to be done.' exists

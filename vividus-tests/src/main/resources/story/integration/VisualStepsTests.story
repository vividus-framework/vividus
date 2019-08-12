Description: Integration tests for VisualSteps class. Page for verification origin:
https://developer.mozilla.org/ru/docs/Web/HTML/Element/area

Meta:
    @group vividus-plugin-visual

Lifecycle:
Examples:
|action         |imageLocator    |
|COMPARE_AGAINST|By.xpath(.//img)|

Scenario: Set-Up
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/area$samples/Example'

Scenario: Validation of step When I $actionType baseline with `$name` for full page
When I <action> baseline with `full-page`

Scenario: Validation of step When I $actionType baseline with `$name` for context element
When I change context to an element by <imageLocator>
When I <action> baseline with `context`
When I change context to the page

Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for full page with element cut
When I <action> baseline with `full-page-element-cut` ignoring:
|ELEMENT         |
|<imageLocator>  |

Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for full page with area cut
When I <action> baseline with `full-page-area-cut` ignoring:
|AREA            |
|<imageLocator>  |

Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for context element with element cut
When I change context to an element by By.xpath(.//body)
When I <action> baseline with `context-element-cut` ignoring:
|ELEMENT         |
|<imageLocator>  |

Scenario: Validation of step When I $actionType baseline with `$name` ignoring:$ignoredElements for context element not in viewport with element cut
Given I am on a page with the URL 'https://en.wikipedia.org/wiki/Java_(programming_language)'
When I change context to an element by By.xpath(//h3[contains(. ,'Example with methods')]/following-sibling::div[.//pre])
When I <action> baseline with `not-viewport-context-element-cut` ignoring:
|ELEMENT                                                                                     |
|By.xpath(.//span[text()='// This is an example of a single line comment using two slashes'])|

Scenario: Validation of step When I $actionType baseline with `$name` for full page with element/area cut
Given I am on a page with the URL 'https://docs.oracle.com/javase/tutorial/java/generics/types.html'
When I <action> baseline with `full-page-with-scroll-element-area-cut` ignoring:
|ELEMENT                                |AREA                                                                                      |
|By.id(LeftBar), By.className(codeblock)|By.className(NavBit), By.xpath(//*[@class='footertext' and .//a[contains(.,'Copyright')]])|

Scenario: Validation of contextual visual testing on a page with scrollable element
Given I am on a page with the URL 'https://www.bombaysapphire.com/'
When I pass age check
When I change context to an element by By.tagName(footer)
When I <action> baseline with `scrollable-element-context` using screenshot configuration:
|scrollableElement                    |webHeaderToCut|webFooterToCut|scrollTimeout|
|By.xpath(//div[@class="page__inner"])|80            |0             |PT1S         |

Scenario: Validation of full-page visual testing on a page with scrollable element with ignores
When I change context to the page
When I <action> baseline with `scrollable-element-fullpage-with-ignores` ignoring:
|ELEMENT                  |AREA                                   |
|By.id(menu-social-follow)|By.xpath(.//div[@class='slick-slider'])|
using screenshot configuration:
|scrollableElement                    |webHeaderToCut|webFooterToCut|coordsProvider|scrollTimeout|
|By.xpath(//div[@class="page__inner"])|80            |0             |CEILING       |PT1S         |

Description: Integration tests for ElementSteps class.

Meta:
    @group vividus-plugin-web-app

Lifecycle:
Examples:
|URL                                                                                    |URL2                                                                                                |
|https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/textarea$samples/Basic_example|https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Defining_a_radio_group?|

Scenario: Step verification Then element located `$locator` exists
Given I am on a page with the URL '<URL>'
Then element located `By.fieldName(textarea):a->filter.fieldTextPart(something)` exists

Scenario: Step verification Then element located `$locator` does not exist
Given I am on a page with the URL '<URL>'
Then element located `By.fieldName(not-existing-name)` does not exist

Scenario: Step verification When I click on an element located `$locator`
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/API/MouseEvent/button$samples/Example'
Then a [ENABLED] element with the tag 'p' does not exist
When I click on element located `By.xpath(//button)`
Then a [ENABLED] element with the tag 'p' exists

Scenario: Step verification When I click on all elements located `$locator`
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?revision=1557218'
Then number of elements found by `By.xpath(//input)` is EQUAL_TO `3`
Then element located `By.xpath(//pre)->filter.state(ENABLED)` does not exist
When I click on all elements located `By.xpath(//input)`
When I click on element located `By.xpath(//button)`
Then element located `By.xpath(//pre)->filter.state(ENABLED)` exists

Scenario: Step verification Then each element located `$locator` has the same '$dimension'
Given I am on a page with the URL '<URL2>'
Then each element located `By.xpath(//input[@type='radio'])` has the same `width`

Scenario: Step verification Then number of elements found by `$locator` is $comparisonRule `$quantity`
Given I am on a page with the URL '<URL2>'
Then number of elements found by `By.xpath(//input[@type='radio'])` is GREATER_THAN `2`

Scenario: Step verification Then each element with locator `$elementXpath` has `$number` child elements with locator `$childXpath`
Given I am on a page with the URL '<URL2>'
Then each element with locator `By.xpath(//form)` has `2` child elements with locator `By.xpath(.//div)`

Scenario: Step verification Then at least one element with locator '$locator' exists
Given I am on a page with the URL '<URL2>'
Then at least one element with locator `By.xpath(//button)` exists

Scenario: Step verification When I hover a mouse over an element with locator '$locator'
Given I am on a page with the URL '<URL2>'
When I hover mouse over element with locator `By.xpath(//*[@type='submit'])`

Scenario: Step verification When I click on an element '$searchAttributes' then the page does not refresh
Given I am on a page with the URL '<URL2>'
When I click on an element 'By.xpath((//*[@type='radio'])[1])' then the page does not refresh
Then the page has the relative URL '/en-US/docs/Web/HTML/Element/input/radio$samples/Defining_a_radio_group?'

Scenario: Step verification Then the context element has a width of '$widthInPerc'% relative to the parent element
Given I am on a page with the URL '<URL2>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has a width of '7'% relative to the parent element

Scenario: Step verification When I perform right click on an element located `$locator`
Given I am on a page with the URL '<URL2>'
When I perform right click on element located `By.xpath(//*[text()='Submit'])`

Scenario: Step verification Then the context element has the CSS property '$cssName'='$cssValue'
Given I am on a page with the URL '<URL2>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has the CSS property 'height'='21px'

Scenario: Step verification Then the context element has the CSS property '$cssName' containing '$cssValue'
Given I am on a page with the URL '<URL2>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has the CSS property 'height' containing '21px'

!-- Composites down there

Scenario: Step verification Then an element with the name '$elementName' containing text '$text' exists
Given I am on a page with the URL '<URL>'
Then an element with the name 'textarea' containing text 'something' exists

Scenario: Step verification Then an element with the name '$elementName' and text '$text' exists
Given I am on a page with the URL '<URL>'
Then an element with the name 'textarea' and text 'Write something here' exists

Scenario: Step verification Then a [$state] element with the name '$elementName' and text '$text' esists
Given I am on a page with the URL '<URL>'
Then a [ENABLED] element with the name 'textarea' and text 'Write something here' exists

Scenario: Step verification Then an element with the name '$elementName' exists
Given I am on a page with the URL '<URL>'
Then an element with the name 'textarea' exists

Scenario: Step verification Then an element with the name '$elementName' does not exist
Given I am on a page with the URL '<URL>'
Then an element with the name 'not-button' does not exist

Scenario: Step verification Then a [$state] element with the name '$elementName' exists
Given I am on a page with the URL '<URL>'
Then a [ENABLED] element with the name 'textarea' exists

Scenario: Step verification Then an element with the tag '$elementTag' and text '$text' exists
Given I am on a page with the URL '<URL>'
Then an element with the tag 'textarea' and text 'Write something here' exists

Scenario: Step verification Then an element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<URL>'
Then an element with the attribute 'rows'='10' exists

Scenario: Step verification Then a [$state] element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<URL>'
Then a [ENABLED] element with the attribute 'rows'='10' exists

Scenario: Step verification Then an element with the tag '$elementTag' and attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<URL>'
Then an element with the tag 'textarea' and attribute 'rows'='10' exists

Scenario: Step verification Then an element with the attribute '$attributeType' containing '$attributeValue' exists
Given I am on a page with the URL '<URL>'
Then an element with the attribute 'rows' containing '10' exists

Scenario: Step verification Then each element by the xpath '$xpath' has the same '$dimension'
Given I am on a page with the URL '<URL2>'
Then each element by the xpath '//input[@type='radio']' has the same 'width'

Scenario: Step verification Then each element with the xpath '$elementXpath' has '$number' child elements with the xpath '$childXpath'
Given I am on a page with the URL '<URL2>'
Then each element with the xpath '//form' has '2' child elements with the xpath './/div'

Scenario: Step verification Then at least one element by the xpath '$xpath' exists
Given I am on a page with the URL '<URL2>'
Then at least one element by the xpath '//button' exists

Scenario: Step verification Then an element by the xpath '$xpath' exists
Given I am on a page with the URL '<URL2>'
Then an element by the xpath '//button' exists

Scenario: Step verification Then an element by the cssSelector '$cssSelector' exists
Given I am on a page with the URL '<URL2>'
Then an element by the cssSelector 'button' exists

Scenario: Step verification Then at least one element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<URL2>'
Then at least one element with the attribute 'type'='submit' exists

Scenario: Step verification When I click on an element with the attribute '$attributeType'='$attributeValue'
Given I am on a page with the URL '<URL2>'
When I click on an element with the attribute 'type'='submit'

Scenario: Step verification When I click on an element with the text '$text'
Given I am on a page with the URL '<URL2>'
When I click on an element with the text 'Submit'

Scenario: Step verification When I click on all elements by xpath '$xpath'
Given I am on a page with the URL '<URL2>'
When I click on all elements by xpath '//button[@type='submit']'

Scenario: Step verification When I click on an element by the xpath '$xpath'
Given I am on a page with the URL '<URL2>'
When I click on an element by the xpath '//button[@type='submit']'

Scenario: Step verification When I hover a mouse over an element with the xpath '$xpath'
Given I am on a page with the URL '<URL2>'
When I hover a mouse over an element with the xpath '//*[@type='submit']'

Scenario: Step verification When I perform right click on an element by the xpath '$xpath'
Given I am on a page with the URL '<URL2>'
When I perform right click on an element by the xpath '//*[@type='submit']'

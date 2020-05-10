Description: Integration tests for ElementSteps class.

Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
{transformer=FROM_LANDSCAPE}
|textAreaURL         |https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/textarea$samples/Basic_example                           |
|buttonURL           |https://mdn.mozillademos.org/en-US/docs/Web/API/MouseEvent/button$samples/Example                                 |
|rightClickButtonURL |http://demo.guru99.com/test/simple_context_menu.html                                                              |
|radioButtonsURL     |https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?|

Scenario: Step verification When I click on element located `$locator`
Given I am on a page with the URL '<buttonURL>'
Then a [ENABLED] element with the tag 'p' does not exist
When I click on element located `By.xpath(//button)`
Then a [ENABLED] element with the tag 'p' exists

Scenario: Step verification When I click on all elements located `$locator`
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?'
Then number of elements found by `By.xpath(.//input)` is equal to `3`
Then number of elements found by `By.xpath(.//pre)->filter.state(ENABLED)` is equal to `0`
When I click on all elements located `By.xpath(.//input)`
When I click on element located `By.xpath(.//button)`
Then number of elements found by `By.xpath(.//pre)->filter.state(ENABLED)` is equal to `1`

Scenario: Step verification Then each element located `$locator` has same '$dimension'
Given I am on a page with the URL '<radioButtonsURL>'
Then each element located `By.xpath(.//input[@type='radio'])` has same `width`

Scenario: Step verification Then number of elements found by `$locator` is $comparisonRule `$quantity`
Given I am on a page with the URL '<radioButtonsURL>'
Then number of elements found by `By.xpath(.//input[@type='radio'])` is greater than `2`

Scenario: Step verification Then each element with locator `$elementXpath` has `$number` child elements with locator `$childXpath`
Given I am on a page with the URL '<radioButtonsURL>'
Then each element with locator `By.xpath(.//form)` has `2` child elements with locator `By.xpath(.//div)`

Scenario: Step verification When I hover a mouse over an element located '$locator'
Given I am on a page with the URL 'https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_onmousemove_over_enter'
When I switch to a frame with the attribute 'id'='iframeResult'
When I change context to an element by By.xpath(//div[contains(., 'onmouseover: Mouse over me!')])
When I hover mouse over element located `By.xpath(self::*)`
Then the text 'onmouseover: 1' exists

Scenario: Step verification When I click on an element '$searchAttributes' then the page does not refresh
Given I am on a page with the URL '<radioButtonsURL>'
When I click on an element 'By.xpath((.//*[@type='radio'])[1])' then the page does not refresh
Then the page has the relative URL '/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?'

Scenario: Step verification Then the context element has a width of '$widthInPerc'% relative to the parent element
Given I am on a page with the URL '<radioButtonsURL>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has a width of '7'% relative to the parent element

Scenario: Step verification When I perform right click on element located `$locator`
Given I am on a page with the URL '<rightClickButtonURL>'
Then number of elements found by `By.xpath(.//*[@class='context-menu-item context-menu-icon context-menu-icon-edit'])` is equal to `0`
When I perform right click on element located `By.xpath(.//*[@class='context-menu-one btn btn-neutral'])`
Then number of elements found by `By.xpath(.//*[@class='context-menu-item context-menu-icon context-menu-icon-edit'])` is equal to `1`

Scenario: Step verification Then the context element has the CSS property '$cssName'='$cssValue'
Given I am on a page with the URL '<radioButtonsURL>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has the CSS property 'color'='rgba(0, 0, 0, 1)'

Scenario: Step verification Then the context element has the CSS property '$cssName' containing '$cssValue'
Given I am on a page with the URL '<radioButtonsURL>'
When I change context to an element with the attribute 'type'='submit'
Then the context element has the CSS property 'color' containing '(0, 0, 0, 1)'

Scenario: Step verification When I select an element '$locator' and upload the file '$filePath'
Given I am on a page with the URL 'http://demo.guru99.com/test/upload/'
When I select element located `By.id(uploadfile_0)` and upload file `/data/file_for_upload_step.png`
When I click on element located `By.name(send)`
Then the text 'has been successfully uploaded' exists

Scenario: Should not fail click step when element in Cross-Origin frame
Given I am on a page with the URL 'https://vividus-test-site.herokuapp.com/frames.html'
When I switch to a frame with the attribute 'id'='exampleCom'
When I click on element located `By.xpath(//a[contains(text(), 'More')])`

!-- Composites down there

Scenario: Step verification When I enter `$text` in field located `$locator` using keyboard
Given I am on a page with the URL '${vividus-test-site-url}/inputs.html'
When I initialize the scenario variable `text` with value `#{generate(regexify '[a-z]{15}')}`
When I enter `${text}` in field located `By.id(text)` using keyboard
Then the text '${text}' exists

Scenario: Step verification Then an element with the name '$elementName' containing text '$text' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the name 'textarea' containing text 'something' exists

Scenario: Step verification Then an element with the name '$elementName' and text '$text' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the name 'textarea' and text 'Write something here' exists

Scenario: Step verification Then a [$state] element with the name '$elementName' and text '$text' exists
Given I am on a page with the URL '<textAreaURL>'
Then a [ENABLED] element with the name 'textarea' and text 'Write something here' exists

Scenario: Step verification Then an element with the name '$elementName' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the name 'textarea' exists

Scenario: Step verification Then an element with the name '$elementName' does not exist
Given I am on a page with the URL '<textAreaURL>'
Then an element with the name 'not-button' does not exist

Scenario: Step verification Then a [$state] element with the name '$elementName' exists
Given I am on a page with the URL '<textAreaURL>'
Then a [ENABLED] element with the name 'textarea' exists

Scenario: Step verification Then an element with the tag '$elementTag' and text '$text' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the tag 'textarea' and text 'Write something here' exists

Scenario: Step verification Then an element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the attribute 'rows'='10' exists

Scenario: Step verification Then a [$state] element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<textAreaURL>'
Then a [ENABLED] element with the attribute 'rows'='10' exists

Scenario: Step verification Then an element with the tag '$elementTag' and attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the tag 'textarea' and attribute 'rows'='10' exists

Scenario: Step verification Then an element with the attribute '$attributeType' containing '$attributeValue' exists
Given I am on a page with the URL '<textAreaURL>'
Then an element with the attribute 'rows' containing '10' exists

Scenario: Step verification Then each element by the xpath '$xpath' has same '$dimension'
Given I am on a page with the URL '<radioButtonsURL>'
Then each element by the xpath './/input[@type='radio']' has same 'width'

Scenario: Step verification Then each element by the xpath '$elementXpath' has '$number' child elements by the xpath '$childXpath'
Given I am on a page with the URL '<radioButtonsURL>'
Then each element by the xpath './/form' has '2' child elements by the xpath './/div'

Scenario: Step verification Then at least one element by the xpath '$xpath' exists
Given I am on a page with the URL '<radioButtonsURL>'
Then at least one element by the xpath './/button' exists

Scenario: Step verification Then an element by the xpath '$xpath' exists
Given I am on a page with the URL '<radioButtonsURL>'
Then an element by the xpath './/button' exists

Scenario: Step verification Then an element by the cssSelector '$cssSelector' exists
Given I am on a page with the URL '<radioButtonsURL>'
Then an element by the cssSelector 'button' exists

Scenario: Step verification Then at least one element with the attribute '$attributeType'='$attributeValue' exists
Given I am on a page with the URL '<radioButtonsURL>'
Then at least one element with the attribute 'type'='submit' exists

Scenario: Step verification When I click on an element with the attribute '$attributeType'='$attributeValue'
Given I am on a page with the URL '<buttonURL>'
Then a [ENABLED] element with the tag 'p' does not exist
When I click on an element with the attribute 'id'='button'
Then a [ENABLED] element with the tag 'p' exists

Scenario: Step verification When I click on an element with the text '$text'
Given I am on a page with the URL '<buttonURL>'
Then a [ENABLED] element with the tag 'p' does not exist
When I click on an element with the text 'Click here with your mouse...'
Then a [ENABLED] element with the tag 'p' exists

Scenario: Step verification When I click on all elements by xpath '$xpath'
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input/radio$samples/Data_representation_of_a_radio_group?'
Then number of elements found by `By.xpath(.//input)` is equal to `3`
Then number of elements found by `By.xpath(.//pre)->filter.state(ENABLED)` is equal to `0`
When I click on all elements by xpath './/input'
When I click on an element by the xpath './/button'
Then number of elements found by `By.xpath(.//pre)->filter.state(ENABLED)` is equal to `1`

Scenario: Step verification When I click on an element by the xpath '$xpath'
Given I am on a page with the URL '<buttonURL>'
Then a [ENABLED] element with the tag 'p' does not exist
When I click on an element by the xpath './/button'
Then a [ENABLED] element with the tag 'p' exists

Scenario: Step verification When I hover a mouse over an element with the xpath '$xpath'
Given I am on a page with the URL 'https://www.w3schools.com/jsref/tryit.asp?filename=tryjsref_onmousemove_over_enter'
When I switch to a frame with the attribute 'id'='iframeResult'
When I change context to an element by By.xpath(//div[contains(., 'onmouseover: Mouse over me!')])
When I hover a mouse over an element with the xpath 'self::*'
Then the text 'onmouseover: 1' exists

Scenario: Step verification When I perform right click on an element by the xpath '$xpath'
Given I am on a page with the URL '<rightClickButtonURL>'
Then an element by the xpath './/*[@class='context-menu-item context-menu-icon context-menu-icon-edit']' does not exist
When I perform right click on an element by the xpath './/*[@class='context-menu-one btn btn-neutral']'
Then an element by the xpath './/*[@class='context-menu-item context-menu-icon context-menu-icon-edit']' exists

Scenario: Step verification When I select an element with the '$attributeType'='$attributeValue' and upload the file '$filePath'
Given I am on a page with the URL 'http://demo.guru99.com/test/upload/'
When I select an element with the 'id'='uploadfile_0' and upload the file '/data/file_for_upload_step.png'
When I click on element located `By.name(send)`
Then the text 'has been successfully uploaded' exists

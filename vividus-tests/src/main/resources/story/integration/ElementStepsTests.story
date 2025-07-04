Description: Integration tests for ElementSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Step verification Then number of elements found by `$locator` is $comparisonRule `$quantity` and When I click on element located by `$locator`
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Then number of elements found by `id(element-to-hide)` is equal to `1`
When I click on element located by `id(button-hide)`
When I wait until element located by `id(element-to-hide)` disappears
Then number of elements found by `id(element-to-hide)` is equal to `0`

Scenario: Step verification Then each element located `$locator` has same '$dimension'
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then each element located `xpath(.//input[@type='radio'])` has same `width`

Scenario: Step verification Then each element located by `$locator` has same '$dimension'
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then each element located by `xpath(.//input[@type='radio'])` has same `width`

Scenario: Deprecated step verification Then each element with locator `$elementXpath` has `$number` child elements with locator `$childXpath`
Given I am on page with URL `${vividus-test-site-url}/dropdowns.html`
Then each element with locator `tagName(select)` has `3` child elements with locator `tagName(option)`

Scenario: Deprecated step verification When I hover a mouse over an element located '$locator'
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
When I hover mouse over element located `xpath(//div[contains(., 'Mouse enter!')])`
Then text `Mouse enter count: 1` exists

Scenario: Step verification When I hover mouse over element located by `$locator`
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
When I hover mouse over element located by `xpath(//div[contains(., 'Mouse enter!')])`
Then text `Mouse enter count: 1` exists

Scenario: Deprecated step verification When I click on an element '$searchAttributes' then the page does not refresh
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
When I click on an element 'xpath((.//*[@type='radio'])[1])' then the page does not refresh
Then the page has the relative URL '/mouseEvents.html'

Scenario: Step verification When I click on element located by `$locator` then page does not refresh
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
When I click on element located by `xpath((.//*[@type='radio'])[1])` then page does not refresh
Then the page has the relative URL '/mouseEvents.html'

Scenario: Step verification Then the context element has a width of '$widthInPerc'% relative to the parent element
Given I am on page with URL `${vividus-test-site-url}/dropdowns.html`
When I change context to element located by `xpath(.//*[@for='colors'])`
Then context element has width of 13% relative to parent element

Scenario: Deprecated step verification Then the context element has a width of '$widthInPerc'% relative to the parent element
Given I am on page with URL `${vividus-test-site-url}/dropdowns.html`
When I change context to element located by `xpath(.//*[@for='colors'])`
Then the context element has a width of '13'% relative to the parent element

Scenario: Deprecated step verification When I perform right click on an element located `$locator`
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then number of elements found by `elementName(Foo)` is = `1`
Then number of elements found by `name(Bar)` is = `0`
When I perform right click on element located `id(context-menu)`
Then number of elements found by `name(Foo)` is = `0`
Then number of elements found by `name(Bar)` is = `1`

Scenario: Step verification When I perform right-click on an element located by `$locator`
Given I am on page with URL `${vividus-test-site-url}/mouseEvents.html`
Then number of elements found by `name(Foo)` is = `1`
Then number of elements found by `name(Bar)` is = `0`
When I perform right-click on element located by `id(context-menu)`
Then number of elements found by `name(Foo)` is = `0`
Then number of elements found by `name(Bar)` is = `1`

Scenario: Deprecated step verification Then the context element has the CSS property '$cssName'='$cssValue'
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then the context element has the CSS property 'color'='rgba(0, 0, 0, 1)'

Scenario: Step verification context element has CSS property `$cssName` with value that $comparisonRule `$cssValue`
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then context element has CSS property `<name>` with value that is equal to `<value>`
Examples:
|name            |value                 |
|color           |rgba(0, 0, 0, 1)      |
|color           |rgb(0, 0, 0)          |
|background-color|rgba(0, 0,0,0)        |
|lighting-color  |rgba(255, 255, 255, 1)|

Scenario: Deprecated step verification Then the context element has the CSS property '$cssName' containing '$cssValue'
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
When I change context to element located by `xpath(.//*[@title='Text input section'])`
Then the context element has the CSS property 'color' containing '(0, 0, 0, 1)'

Scenario: Deprecated step verification When I select element `$locator` and upload file `$filePath`
Given I am on page with URL `${vividus-test-site-url}/upload.html`
When I select element located `By.id(uploadInput)` and upload file `/data/file_for_upload_step.png`
When I click on element located by `id(submit)`
Then text `File has been successfully uploaded. Server received: 445508 bytes` exists

Scenario: Step verification When I select element located by `$locator` and upload `$resourceNameOrFilePath`
Given I am on page with URL `${vividus-test-site-url}/upload.html`
When I select element located by `id(uploadInput)` and upload `/data/file_for_upload_step.png`
When I click on element located by `id(submit)`
Then text `File has been successfully uploaded. Server received: 445508 bytes` exists

Scenario: Should not fail click step when element in Cross-Origin frame
Given I am on page with URL `${vividus-test-site-url}/frames.html`
When I switch to frame located by `id(exampleCom)`
When I click on element located by `xpath(//a[contains(text(), 'More')])`

Scenario: Steps verification: "When I set focus on context element", "Then context element is $focusState", "When I press $keys on keyboard"
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Given I initialize scenario variable `car-brand` with value `#{generate(Brand.car)}`
When I change context to element located by `id(text)`
Then context element is not in focus
When I set focus on context element
Then context element is in focus
When I press ${car-brand} on keyboard
When I reset context
Then text `${car-brand}` exists
!-- Dynamic focus element
When I enter `a` in field located by `id(text)`
When I press bcde on keyboard
Then text `abcde` exists

Scenario: Steps verification: "When I set focus on context element", "Then context element is $focusState", "When I press $keys on keyboard"
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Given I initialize scenario variable `sport-brand` with value `#{generate(Brand.sport)}`
Then element located by `id(text)` is not in focus
When I set focus on element located by `id(text)`
Then element located by `id(text)` is in focus
When I change context to element located by `id(text)`
When I press ${sport-brand} on keyboard
When I reset context
Then text `${sport-brand}` exists

!-- Composites down there

Scenario: Steps verification: "When I set focus to the context element", "Then the context element is $focusState"
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Given I initialize scenario variable `watch-brand` with value `#{generate(Brand.watch)}`
When I change context to element located by `id(text)`
Then the context element is not in focus
When I set focus to the context element
Then the context element is in focus
When I press ${watch-brand} on keyboard
When I reset context
Then text `${watch-brand}` exists

Scenario: Verification of step "When I enter `$text` in field located `$locator` using keyboard" and locator "fieldName(with-label-name)"
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Given I initialize scenario variable `text` with value `#{generate(regexify '[a-z]{15}')}`
When I enter `${text}` in field located `fieldName(Input:)` using keyboard
Then text `${text}` exists

Scenario: Step verification Then an element with the name '$elementName' containing text '$text' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the name 'story' containing text 'stormy night' exists

Scenario: Step verification Then an element with the name '$elementName' and text '$text' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the name 'story' and text 'It was a dark and stormy night...' exists

Scenario: Step verification Then a [$state] element with the name '$elementName' and text '$text' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then a [ENABLED] element with the name 'story' and text 'It was a dark and stormy night...' exists

Scenario: Step verification Then an element with the name '$elementName' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the name 'Text area section' exists

Scenario: Step verification Then an element with the name '$elementName' does not exist
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the name 'not-button' does not exist

Scenario: Step verification Then a [$state] element with the name '$elementName' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then a [ENABLED] element with the name 'Text area section' exists

Scenario: Step verification Then an element with the tag '$elementTag' and text '$text' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the tag 'textarea' and text 'It was a dark and stormy night...' exists

Scenario: Step verification Then an element with the attribute '$attributeType'='$attributeValue' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the attribute 'rows'='5' exists

Scenario: Step verification Then a [$state] element with the attribute '$attributeType'='$attributeValue' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then a [ENABLED] element with the attribute 'rows'='5' exists

Scenario: Step verification Then an element with the tag '$elementTag' and attribute '$attributeType'='$attributeValue' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the tag 'textarea' and attribute 'rows'='5' exists

Scenario: Step verification Then an element with the attribute '$attributeType' containing '$attributeValue' exists
Given I am on page with URL `${vividus-test-site-url}/inputs.html`
Then an element with the attribute 'rows' containing '5' exists

Scenario: Step verification Then an element by the cssSelector '$cssSelector' exists
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Then an element by the cssSelector '#element-to-hide' exists

Scenario: Step verification Then at least one element with the attribute '$attributeType'='$attributeValue' exists
Given I am on page with URL `${vividus-test-site-url}/dropdowns.html`
Then at least one element with the attribute 'for'='colors' exists

Scenario: Step verification When I click on an element with the attribute '$attributeType'='$attributeValue'
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Then number of elements found by `id(element-to-hide)` is equal to `1`
When I click on an element with the attribute 'id'='button-hide'
When I wait until element located by `id(element-to-hide)` disappears

Scenario: Step verification When I click on an element with the text '$text'
Given I am on page with URL `${vividus-test-site-url}/elementState.html`
Then number of elements found by `id(element-to-hide)` is equal to `1`
When I click on an element with the text 'hide'
When I wait until element located by `id(element-to-hide)` disappears

Scenario: Step verification 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`'
Given I am on page with URL `${vividus-test-site-url}`
Then number of VISIBLE elements found by `tagName(img):a` is = `1`

Scenario: Verify step: "When I save `$cssProperty` CSS property value of element located by `$locator` to $scopes variable `$variableName`"
When I save `background-image` CSS property value of element located by `xpath(//body)` to scenario variable `cssPropertyValue`
Then `${cssPropertyValue}` is = `none`

Scenario: Step verification Then elements located by `$locator` are sorted by text in $sortingOrder order
Given I am on page with URL `${vividus-test-site-url}/sortedListOfElement.html`
Then elements located by `tagName(h3)` are sorted by text in ASCENDING order

Scenario: Validate CSS factory selector generation for inputs containing special symbols
Given I am on page with URL `${vividus-test-site-url}/selector.html`
When I find GREATER_THAN `0` elements by `tagName(button)` and for each element do
|step                                         |
|When I click on element located by `xpath(.)`|
When I save `innerText` attribute value of element located `id(count)` to scenario variable `count`
Then `${count}` is equal to `6`

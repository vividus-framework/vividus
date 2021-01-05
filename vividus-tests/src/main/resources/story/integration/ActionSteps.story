Description: Integration tests for ActionSteps class.

Meta:
    @epic vividus-plugin-web-app

Scenario: Action verification MOVE_BY_OFFSET
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
Then number of elements found by `By.xpath(//*[@id='moveCount' and text()='0'])` is equal to `1`
When I execute sequence of actions:
|type          |argument                                   |
|MOVE_TO       |By.xpath(//div[contains(., 'Mouse move!')])|
|MOVE_BY_OFFSET|(0, 10)                                    |
|MOVE_BY_OFFSET|(0, -10)                                   |
|MOVE_BY_OFFSET|(0, 10)                                    |
|MOVE_BY_OFFSET|(0, -10)                                   |
Then number of elements found by `By.xpath(//*[@id='moveCount' and text()='5'])` is equal to `1`

Scenario: Action verification MOVE_TO
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
Then number of elements found by `By.xpath(//*[@id='enterCount' and text()='0'])` is equal to `1`
When I execute sequence of actions:
|type   |argument                                    |
|MOVE_TO|By.xpath(//div[contains(., 'Mouse enter!')])|
Then number of elements found by `By.xpath(//*[@id='enterCount' and text()='1'])` is equal to `1`

Scenario: Action verification CLICK
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I initialize the scenario variable `expectedText` with value `Good day!`
Then the text '${expectedText}' does not exist
When I execute sequence of actions:
|type |argument                             |
|CLICK|By.xpath(//button[text()='Click me'])|
Then the text '${expectedText}' exists

Scenario: Action verification CLICK with no argument
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I initialize the scenario variable `expectedText` with value `Good day!`
Then the text '${expectedText}' does not exist
When I execute sequence of actions:
|type   |argument                             |
|MOVE_TO|By.xpath(//button[text()='Click me'])|
|CLICK  |                                     |
Then the text '${expectedText}' exists

Scenario: Action verification CLICK_AND_HOLD
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I change context to element located `By.id(target)`
Then the context element has the CSS property 'background-color'='rgba(255, 255, 255, 1)'
When I execute sequence of actions:
|type          |argument         |
|CLICK_AND_HOLD|By.xpath(self::*)|
Then the context element has the CSS property 'background-color'='rgba(255, 0, 0, 1)'

Scenario: Action verification CLICK_AND_HOLD with no argument
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I change context to element located `By.id(target)`
Then the context element has the CSS property 'background-color'='rgba(255, 255, 255, 1)'
When I execute sequence of actions:
|type          |argument         |
|MOVE_TO       |By.xpath(self::*)|
|CLICK_AND_HOLD|                 |
Then the context element has the CSS property 'background-color'='rgba(255, 0, 0, 1)'

Scenario: Action verification RELEASE
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I change context to element located `By.id(target)`
Then the context element has the CSS property 'background-color'='rgba(255, 255, 255, 1)'
When I execute sequence of actions:
|type          |argument         |
|CLICK_AND_HOLD|By.xpath(self::*)|
|RELEASE       |By.xpath(self::*)|
Then the context element has the CSS property 'background-color'='rgba(0, 128, 0, 1)'

Scenario: Action verification RELEASE with no argument
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I change context to element located `By.id(target)`
Then the context element has the CSS property 'background-color'='rgba(255, 255, 255, 1)'
When I execute sequence of actions:
|type          |argument         |
|CLICK_AND_HOLD|By.xpath(self::*)|
|RELEASE       |                 |
Then the context element has the CSS property 'background-color'='rgba(0, 128, 0, 1)'

Scenario: Action verification DOUBLE_CLICK
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I initialize the scenario variable `expectedText` with value `Good day!`
Then the text '${expectedText}' does not exist
When I execute sequence of actions:
|type        |argument                                |
|DOUBLE_CLICK|By.xpath(//p[text()='Double-click me.'])|
Then the text '${expectedText}' exists

Scenario: Action verification DOUBLE_CLICK with no argument
Given I am on a page with the URL '${vividus-test-site-url}/mouseEvents.html'
When I initialize the scenario variable `expectedText` with value `Good day!`
Then the text '${expectedText}' does not exist
When I execute sequence of actions:
|type        |argument                                |
|MOVE_TO     |By.xpath(//p[text()='Double-click me.'])|
|DOUBLE_CLICK|                                        |
Then the text '${expectedText}' exists

Scenario: Action verification ENTER_TEXT
Given I am on a page with the URL '${vividus-test-site-url}/inputs.html'
When I initialize the scenario variable `input` with value `#{generate(regexify '[a-z]{15}')}`
Then the text '${input}' does not exist
When I click on element located `By.xpath(//label[@for='text'])`
When I execute sequence of actions:
|type      |argument|
|ENTER_TEXT|${input}|
Then the text '${input}' exists

Scenario: Action verification PRESS_KEYS
Given I am on a page with the URL '${vividus-test-site-url}/inputs.html'
When I initialize the scenario variable `input` with value `mark#{generate(regexify '[a-z]{10}')}`
When I enter `${input}` in field located `By.id(text)`
Then the text '${input}' exists
When I click on element located `By.xpath(//label[@for='text'])`
When I find = `1` elements `By.xpath(//div[@id='output' and text()!='mark'])` and while they exist do up to 11 iteration of
|step                                                            |
|When I execute sequence of actions:                             |
|{headerSeparator=!, valueSeparator=!}                           |
|!type      !argument   !                                        |
|!PRESS_KEYS!BACK_SPACE !                                        |
Then number of elements found by `By.xpath(//div[@id='output' and text()='mark'])` is equal to `1`

Scenario: Action verification KEY_DOWN and KEY_UP combiantion
Meta:
    @requirementId 686
Given I am on a page with the URL 'https://mdn.mozillademos.org/en-US/docs/Web/HTML/Element/input$samples/caret-color'
When I initialize the scenario variable `inputText` with value `mark#{generate(regexify '[a-z]{10}')}`
When I initialize the scenario variable `inputLocator` with value `By.id(textInput)`
When I enter `${inputText}` in field located `${inputLocator}`
Then field value is `${inputText}`
When I click on element located `${inputLocator}`
When I execute sequence of actions:
|type      |argument|
|KEY_DOWN  |CONTROL |
|PRESS_KEYS|a       |
|KEY_UP    |CONTROL |
|PRESS_KEYS|DELETE  |
Then field value is ``

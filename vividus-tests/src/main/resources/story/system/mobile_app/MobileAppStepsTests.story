Description: Integration tests for mobile app steps

Meta:
    @epic vividus-plugin-mobile-app

Lifecycle:
Examples:
/data/tables/system/mobile_app/locators/${target-platform}.table

Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Meta:
    @healthCheck
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Text Part Filter
Then number of elements found by `xpath(<textElementXpath>)->filter.textPart(Home)` is equal to `1`


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Accessibility Id Locator
Then number of VISIBLE elements found by `accessibilityId(<togglerAccessibilityId>):a` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator` with duration `$duration`'
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `0`
When I tap on element located `accessibilityId(<togglerAccessibilityId>)` with duration `PT0.5S`
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator`'
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)` is equal to `0`
When I tap on element located `xpath(<menuButtonXpath>)`
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)` is equal to `1`


Scenario: Verify step: 'When I type `$text` in field located `$locator`'
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<menuInputXpath>)`
When I initialize the scenario variable `text` with value `#{generate(regexify '[a-z]{10}')}`
When I type `${text}` in field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `1`


Scenario: Verify step: 'When I clear field located `$locator`' and Appium XPath Locator
When I clear field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameInputXpath>)` is equal to `1`


Scenario: Verify step: 'When I wait until element located `$locator` disappears'
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<menuWaitXpath>)`
Then number of elements found by `accessibilityId(<pictureAccessibilityId>)` is equal to `1`
When I tap on element located `accessibilityId(<hidePictureAccessibilityId>)`
When I wait until element located `accessibilityId(<pictureAccessibilityId>)` disappears
Then number of elements found by `accessibilityId(<pictureAccessibilityId>)` is equal to `0`


Scenario: Verify step: 'When I wait until element located `$locator` appears'
Then number of elements found by `accessibilityId(<pictureAccessibilityId>)` is equal to `0`
When I tap on element located `accessibilityId(<showPictureAccessibilityId>)`
When I wait until element located `accessibilityId(<pictureAccessibilityId>)` appears
Then number of elements found by `accessibilityId(<pictureAccessibilityId>)` is equal to `1`


Scenario: Verify step: 'When I swipe $direction to element located `$locator` with duration $swipeDuration'
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<menuScrollViewXpath>)`
Then number of elements found by `accessibilityId(<startElementAccessibilityId>)` is equal to `1`
Then number of elements found by `accessibilityId(<endElementAccessibilityId>)` is equal to `0`
When I swipe UP to element located `accessibilityId(<endElementAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<startElementAccessibilityId>)` is equal to `0`
Then number of elements found by `accessibilityId(<endElementAccessibilityId>)` is equal to `1`
When I swipe DOWN to element located `accessibilityId(<startElementAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<startElementAccessibilityId>)` is equal to `1`
Then number of elements found by `accessibilityId(<endElementAccessibilityId>)` is equal to `0`


Scenario: [Android] Verify step: 'When I upload file `$filePath` to device'
Meta:
    @targetPlatform android
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(//android.widget.TextView[@text='Image'])`
When I upload file `/data/mobile-upload-image.png` to device
When I tap on element located `accessibilityId(select-image-accessibilityLabel)`
When I tap on element located `xpath(//android.widget.TextView[@text='Choose from Library…'])`
When I tap on element located `xpath(//android.widget.Button[@text='Allow'])`
When I wait until element located `xpath(//android.widget.TextView[@text='Pictures'])` appears
When I tap on element located `xpath(//android.widget.TextView[@text='Pictures'])`
When I tap on element located `xpath((//android.view.ViewGroup[contains(@content-desc, "Photo taken")])[1])`
Then number of elements found by `xpath(//android.widget.TextView[@text='228x228'])` is equal to `1`


Scenario: [iOS] Verify step: 'When I upload file `$filePath` to device' AND 'iosClassChain' locator
Meta:
    @targetPlatform ios
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `iosClassChain(**/XCUIElementTypeButton[$name == "Image"$])`
When I upload file `/data/mobile-upload-image.png` to device
When I tap on element located `accessibilityId(select-image-testID)`
When I tap on element located `accessibilityId(Choose from Library…)`
When I wait until element located `accessibilityId(Recents)` appears
When I tap on element located `accessibilityId(Recents)`
When I tap on element located `xpath((//XCUIElementTypeCell[contains(@name, "Photo")])[last()])`
Then number of elements found by `xpath(//XCUIElementTypeStaticText[@value='228x228'])` is equal to `1`

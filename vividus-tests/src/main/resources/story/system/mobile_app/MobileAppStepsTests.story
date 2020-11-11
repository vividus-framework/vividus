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


Scenario: [Android] Verify step: 'When I change Appium session settings:$settings'
Meta:
    @targetPlatform android
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`
When I change Appium session settings:
|name                  |value|
|allowInvisibleElements|true |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`
When I change Appium session settings:
|name                  |value|
|allowInvisibleElements|false|
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`


Scenario: [iOS] Verify step: 'When I change Appium session settings:$settings'
Meta:
    @targetPlatform ios
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`
When I change Appium session settings:
|name            |value|
|snapshotMaxDepth|1    |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`
When I change Appium session settings:
|name            |value     |
|snapshotMaxDepth|50        |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Text Part Filter and Text Filter
Then number of elements found by `xpath(<textElementXpath>)->filter.textPart(om)` is equal to `1`
Then number of elements found by `xpath(<textElementXpath>)->filter.text(Home)` is equal to `1`


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


Scenario: Verify step: 'When I navigate back'
Meta:
    @targetPlatform android

!-- The step doesn't work with newer version of iOS due to absence of navigation controls on the screen
When I navigate back
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `0`


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


Scenario: Verify step: 'When I activate application with bundle identifier `$bundleId`'
When I activate application with bundle identifier `${browser-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` disappears
When I activate application with bundle identifier `${main-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` appears


Scenario: [iOS] Verify step: 'When I select $direction value with `$offset` offset in picker wheel located `$locator`'
Meta:
    @targetPlatform ios
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(//XCUIElementTypeButton[@name="Date Picker"])`
When I change context to element located `accessibilityId(dateTimePicker)`
When I select next value with `0.1` offset in picker wheel located `xpath(//XCUIElementTypePickerWheel)->filter.index(1)`
When I select previous value with `0.1` offset in picker wheel located `xpath(//XCUIElementTypePickerWheel)->filter.index(2)`
When I select next value with `0.1` offset in picker wheel located `xpath(//XCUIElementTypePickerWheel)->filter.index(3)`
When I reset context
Then number of elements found by `accessibilityId(dateInput)->filter.textPart(1/10/2012)` is equal to `1`


Scenario: Verify step: 'When I close mobile application'
When I close mobile application

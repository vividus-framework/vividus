Description: Integration tests for mobile app steps

Meta:
    @epic vividus-plugin-mobile-app

Lifecycle:
Examples:
/data/tables/system/mobile_app/locators/${target-platform}.table


Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|


Scenario: Verify step: 'When I restart mobile application'
When I restart mobile application


Scenario: Validate coordinate/size dynamic variables, page source dynamic variable
Then `${source-code}` matches `.+Home.+`
When I change context to element located `xpath(<textElementXpath>)->filter.text(Home)`
Then `${context-height}`            is > `0`
Then `${context-width}`             is > `0`
Then `${context-x-coordinate}`      is > `0`
Then `${context-y-coordinate}`      is > `0`
When I reset context


Scenario: Verify steps: 'When I press $key key', 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`', 'When I press keys:$keys' and Text Part Filter and Text Filter
Then number of elements found by `xpath(<textElementXpath>)->filter.text(Home)` is equal to `1`
When I press Home key
Then number of elements found by `xpath(<textElementXpath>)->filter.text(Home)` is equal to `0`
When I activate application with bundle identifier `${main-app}`
When I wait until element located `xpath(<textElementXpath>)->filter.textPart(om)` appears
When I press keys:
|key |
|Home|
Then number of elements found by `xpath(<textElementXpath>)->filter.text(Home)` is equal to `0`
When I activate application with bundle identifier `${main-app}`
When I wait until element located `xpath(<textElementXpath>)->filter.textPart(om)` appears


Scenario: [Android] Verify step: 'When I change Appium session settings:$settings' and Id Locator
Meta:
    @targetPlatform android
Then number of elements found by `id(com.vividustestapp:id/action_bar_root)` is equal to `1`
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`
When I change Appium session settings:
|name                  |value|
|allowInvisibleElements|true |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`
When I change Appium session settings:
|name                  |value|
|allowInvisibleElements|false|
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`


Scenario: [iOS] Verify step: 'When I change Appium session settings:$settings' and Id Locator
Meta:
    @targetPlatform ios
Then number of elements found by `id(menu-toggler-testID)` is equal to `1`
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`
When I change Appium session settings:
|name            |value|
|snapshotMaxDepth|1    |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `0`
When I change Appium session settings:
|name            |value     |
|snapshotMaxDepth|50        |
Then number of elements found by `xpath(<menuButtonXpath>):a` is equal to `1`


Scenario: Verify step: 'Then number of $state elements found by `$locator` is $comparisonRule `$quantity`' and Accessibility Id Locator
Then number of VISIBLE elements found by `accessibilityId(<togglerAccessibilityId>):a` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator` with duration `$duration`'
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `0`
When I tap on element located `accessibilityId(<togglerAccessibilityId>)` with duration `PT0.5S`
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `1`


Scenario: Verify step: 'When I tap on element located `$locator`' and Attribute Filter
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)` is equal to `0`
When I tap on element located `xpath(<menuButtonXpath>)->filter.attribute(${visibility-attribute})`
Then number of elements found by `accessibilityId(<incrementAccessibilityId>)->filter.attribute(${visibility-attribute}=true)` is equal to `1`


Scenario: Verify step: 'When I navigate back'
Meta:
    @targetPlatform android

!-- The step doesn't work with newer version of iOS due to absence of navigation controls on the screen
When I navigate back
Then number of elements found by `xpath(<menuButtonXpath>)` is equal to `0`


Scenario: Verify step: 'When I type text `$text`'
Meta:
    @targetPlatform android

When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<menuInputXpath>)`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `0`
When I tap on element located `accessibilityId(<nameInputAccessibilityId>)`
When I initialize the story variable `text` with value `#{generate(regexify '[a-z]{10}')}`
When I type text `${text}`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `1`
When I clear field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `0`
When I navigate back


Scenario: Verify step: 'When I type `$text` in field located `$locator`'
When I initialize the story variable `text` with value `#{generate(regexify '[a-z]{10}')}`
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<menuInputXpath>)`
When I type `${text}` in field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `1`


Scenario: Verify dynamic variable: 'clipboard-text'
When I tap on element located `accessibilityId(CopyTextToClipboardButton)`
Then `${clipboard-text}` is equal to `${text}`


Scenario: Verify step: 'When I clear field located `$locator`' and Appium XPath Locator
When I clear field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameInputXpath>)` is equal to `1`

!-- There should be no error when trying to clear an empty field
When I clear field located `accessibilityId(<nameInputAccessibilityId>)`
Then number of elements found by `xpath(<nameInputXpath>)` is equal to `1`


Scenario: Verify step: 'When I type `$text` in field located `$locator` and keep keyboard opened'
Meta:
    @requirementId 1927
    @targetPlatform ios
!-- Typing on android emulator doesn't shows a keyboard.
When I initialize the story variable `text` with value `#{generate(regexify '[a-z]{10}')}`
When I type `${text}` in field located `accessibilityId(<nameInputAccessibilityId>)` and keep keyboard opened
When I save `<textFieldValueAttribute>` attribute value of element located `accessibilityId(<nameInputAccessibilityId>)` to scenario variable `typedText`
Then number of elements found by `xpath(<nameDisplayXpath>)` is equal to `1`
Then number of elements found by `<keyboardLocator>` is equal to `1`
Then `${text}` is equal to `${typedText}`
When I tap on element located `accessibilityId(Return)`
Then number of elements found by `<keyboardLocator>` is equal to `0`


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
When I wait until element located `accessibilityId(Recents)` appears
When I tap on element located `accessibilityId(Recents)`
When I tap on element located `xpath((//XCUIElementTypeCell[contains(@name, "Photo")])[last()])`
Then number of elements found by `xpath(//XCUIElementTypeStaticText[@value='228x228'])` is equal to `1`


Scenario: Verify step: 'When I activate application with bundle identifier `$bundleId`'
When I activate application with bundle identifier `${browser-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` disappears
When I activate application with bundle identifier `${main-app}`
When I wait until element located `accessibilityId(<togglerAccessibilityId>)` appears


Scenario: Verify step: 'When I terminate application with bundle identifier `$bundleId`'
When I terminate application with bundle identifier `${main-app}`
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


Scenario: [Android] Verify steps: 'When I switch to native context', 'When I switch to web view with index `$index`'
Meta:
    @targetPlatform android
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(//android.widget.TextView[@text='Web View'])`
When I wait until element located `xpath(//android.webkit.WebView[@focusable='true'])` appears
When I switch to web view with index `1`
Then number of elements found by `xpath(//*[@id='welcome-message'])` is equal to `1`
When I switch to native context
Then number of elements found by `xpath(//*[@id='welcome-message'])` is equal to `0`


Scenario: Verify step: 'When I swipe $direction to element located `$locator` with duration $swipeDuration'
When I tap on element located `accessibilityId(<togglerAccessibilityId>)`
When I tap on element located `xpath(<carouselViewXpath>)`
Then number of elements found by `accessibilityId(<firstItemAccessibilityId>)` is = `1`
When I swipe LEFT to element located `accessibilityId(<secondItemAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<firstItemAccessibilityId>)` is = `0`
Then number of elements found by `accessibilityId(<secondItemAccessibilityId>)` is = `1`
When I swipe RIGHT to element located `accessibilityId(<firstItemAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<firstItemAccessibilityId>)` is = `1`
Then number of elements found by `accessibilityId(<secondItemAccessibilityId>)` is = `0`
When I change context to element located `xpath(<swipeableAreaXpath>)`
When I swipe LEFT to element located `accessibilityId(<secondItemAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<firstItemAccessibilityId>)` is = `0`
Then number of elements found by `accessibilityId(<secondItemAccessibilityId>)` is = `1`
When I swipe RIGHT to element located `accessibilityId(<firstItemAccessibilityId>)` with duration PT1S
Then number of elements found by `accessibilityId(<firstItemAccessibilityId>)` is = `1`
Then number of elements found by `accessibilityId(<secondItemAccessibilityId>)` is = `0`

Examples:
|firstItemAccessibilityId|secondItemAccessibilityId|
|Item 1                  |Item 2                   |


Scenario: Verify step: 'When I long press $key key'
Meta:
    @targetPlatform android
When I long press POWER key
When I reset context
Then number of elements found by `xpath(//*[@text = 'Power off'])` is = `1`

Scenario: Verify step: 'When I close mobile application'
When I close mobile application

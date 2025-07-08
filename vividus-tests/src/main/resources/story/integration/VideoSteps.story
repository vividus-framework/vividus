Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
|videoLocator      |
|cssSelector(video)|


Scenario: Verify step: "When I pause video in video player located by `$locator`"
Given I am on page with URL `${vividus-test-site-url}/video.html`
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is = `0`


When I click on element located by `<videoLocator>`
Then element located by `<videoLocator>` exists for `PT2S` duration
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is > `0`
When I pause video in video player located `<videoLocator>`
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Given I initialize story variable `pausedTime` with value `${details.currentTime}`
Then element located by `<videoLocator>` exists for `PT1S` duration
Then `${details.currentTime}` is = `${pausedTime}`
When I play video in video player located by `<videoLocator>`
Then element located by `<videoLocator>` exists for `PT1S` duration
When I pause video in video player located by `<videoLocator>`
When I save info from video player located by `<videoLocator>` to SCENARIO variable `details`
Given I initialize story variable `pausedTime` with value `${details.currentTime}`
Then element located by `<videoLocator>` exists for `PT1S` duration
When I save info from video player located by `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is = `${pausedTime}`

Scenario: Verify step: "When I rewind time to `$time` seconds in video player located by `$locator`"
Given I am on page with URL `${vividus-test-site-url}/video.html`
When I rewind time to `4` seconds in video player located `<videoLocator>`
When I save info from video player located `<videoLocator>` to story variable `details`
Given I initialize scenario variable `rewindTime` with value `${details.currentTime}`
Then `${rewindTime}` is > `0`
When I rewind time to `6` seconds in video player located by `<videoLocator>`
When I save info from video player located by `<videoLocator>` to SCENARIO variable `details`
Given I initialize scenario variable `secondRewindTime` with value `${details.currentTime}`
Then `${secondRewindTime}` is > `${rewindTime}`

Scenario: Verify steps: "When I play video in video player located by `$locator`" and "When I save details of video player located by `$locator` to $scopes variable `$variableName`"
When I play video in video player located `<videoLocator>`
Then element located by `<videoLocator>` exists for `PT1S` duration
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is > `${rewindTime}`
Then `${details.duration}` is > `8`
Then `${details.networkState}` is >= `1`
Then `${details.src}` matches `.+/video/countdown\.mp4`
When I play video in video player located by `<videoLocator>`
Then element located by `<videoLocator>` exists for `PT1S` duration
When I save info from video player located by `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is > `${rewindTime}`
Then `${details.duration}` is > `8`
Then `${details.networkState}` is >= `1`
Then `${details.src}` matches `.+/video/countdown\.mp4`

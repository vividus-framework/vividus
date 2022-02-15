Meta:
    @epic vividus-plugin-web-app

Lifecycle:
Examples:
|videoLocator      |
|cssSelector(video)|


Scenario: Verify step: "When I pause video in video player located `$locator`"
Given I am on a page with the URL '${vividus-test-site-url}/video.html'
When I switch to frame located `id(video)`
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is = `0`
When I click on element located `xpath(//button[@aria-label='Play'])`
Then element located `<videoLocator>` exists for `PT1S` duration
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is > `0`
When I pause video in video player located `<videoLocator>`
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
When I initialize the scenario variable `pausedTime` with value `${details.currentTime}`
Then element located `<videoLocator>` exists for `PT1S` duration
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is = `${pausedTime}`


Scenario: Verify step: "When I rewind time to `$time` seconds in video player located `$locator`"
When I rewind time to `777` seconds in video player located `<videoLocator>`
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
When I initialize the scenario variable `rewindTime` with value `${details.currentTime}`
Then `${rewindTime}` is > `${pausedTime}`


Scenario: Verify steps: "When I play video in video player located `$locator`" and "When I save details of video player located `$locator` to $scopes variable `$variableName`"
When I play video in video player located `<videoLocator>`
Then element located `<videoLocator>` exists for `PT1S` duration
When I save info from video player located `<videoLocator>` to SCENARIO variable `details`
Then `${details.currentTime}` is > `${rewindTime}`
Then `${details.duration}` is > `1000`
Then `${details.networkState}` is = `2`
Then `${details.src}` matches `.+youtube.+`

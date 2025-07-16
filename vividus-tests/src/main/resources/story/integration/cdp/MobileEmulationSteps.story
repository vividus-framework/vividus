Scenario: Validate steps: 'When I emulate mobile device with configuration:`$deviceMetrics`', 'When I reset mobile device emulation'
Given I am on page with URL `${vividus-test-site-url}`
When I execute javascript `return window.screen;` and save result to scenario variable `initialScreen`
When I emulate mobile device with configuration:`{
    "width": 430,
    "height": 932,
    "deviceScaleFactor": 3,
    "mobile": true
}`

When I execute javascript `return window.screen;` and save result to scenario variable `overridenScreen`
Then `${overridenScreen.height}` is = `932`
Then `${overridenScreen.width}` is = `430`

When I reset mobile device emulation

When I execute javascript `return window.screen;` and save result to scenario variable `clearedScreen`
Then `${initialScreen.height}` is = `${clearedScreen.height}`
Then `${initialScreen.width}` is = `${clearedScreen.width}`

Scenario: Validate steps: 'When I emulate mobile device with configuration:`$deviceMetrics`', 'When I reset mobile device emulation'
Meta:
    @playwrightOnly
Given I am on page with URL `${vividus-test-site-url}`
When I emulate mobile device with configuration:`{
    "width": 430,
    "height": 932,
    "deviceScaleFactor": 3,
    "mobile": true
}`

When I execute javascript `return {width: screen.width, height: screen.height}` and save result to scenario variable `overridenScreen`
Then `${overridenScreen.height}` is = `932`
Then `${overridenScreen.width}` is = `430`

When I reset mobile device emulation

When I execute javascript `return {width: screen.width, height: screen.height}` and save result to scenario variable `clearedScreen`
Then `${clearedScreen.height}` is < `${overridenScreen.height}`
Then `${clearedScreen.width}` is > `${overridenScreen.width}`

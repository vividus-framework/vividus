Scenario: Validate steps: 'When I override network state with configuration:`$jsonConfiguration`', 'When I reset network conditions emulation'
Given I am on page with URL `${vividus-test-site-url}`
When I execute javascript `return navigator.onLine` and save result to scenario variable `initialOnline`
Then `${initialOnline}` is = `true`
When I override network state with configuration:`{
    "offline": true,
    "downloadThroughput": 0,
    "uploadThroughput": 0,
    "latency": 0
}`
When I execute javascript `return navigator.onLine` and save result to scenario variable `offline`
Then `${offline}` is = `false`
When I reset network conditions emulation
When I execute javascript `return navigator.onLine` and save result to scenario variable `online`
Then `${online}` is = `true`

Scenario: Validate step: 'When I emulate network conditions with configuration:`$jsonConfiguration`'
Given I am on page with URL `${vividus-test-site-url}`
When I emulate network conditions with configuration:`{
    "matchedNetworkConditions": [
        {
            "urlPattern": "",
            "downloadThroughput": 50000,
            "uploadThroughput": 20000,
            "latency": 200
        }
    ]
}`
When I reset network conditions emulation

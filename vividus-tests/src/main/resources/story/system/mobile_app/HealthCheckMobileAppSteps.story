Scenario: Verify BrowserStack integration

Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|
When I close mobile application
When I save BrowserStack network logs to SCENARIO variable `networkLogs`
Then number of JSON elements from `${networkLogs}` by JSON path `$.log` is equal to 1

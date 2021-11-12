Scenario: Step verification: When I save BrowserStack network logs to $scopes variable `$variableName`

Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|
When I close mobile application
When I save BrowserStack network logs to SCENARIO variable `networkLogs`
Then number of JSON elements from `${networkLogs}` by JSON path `$.log` is equal to 1


Scenario: Step verification: When I save BrowserStack network logs to JSON context

Given I start mobile application with capabilities:
|name|value     |
|app |${app-url}|
When I close mobile application
When I save BrowserStack network logs to JSON context
Then number of JSON elements by JSON path `$.log` is equal to 1

Meta:
    @requirementId 1757

Scenario: Start Applications
When I execute steps:
/data/tables/${appType}/start.table


Scenario: Verify screenshot taking steps
When I initialize the scenario variable `numberOfScreenshots` with value `#{evalGroovy(def files = java.nio.file.Path.of($/${screenshot-directory}/$).toFile()?.listFiles(); return files?.length?:0)}`
When I execute steps:
|step           |
|<stepUnderTest>|
Then `1` is = `#{evalGroovy(return java.nio.file.Path.of($/${screenshot-directory}/$).toFile().listFiles().length - ${numberOfScreenshots})}`

Examples:
|stepUnderTest                                                                                              |                                                                                  |
|When I take screenshot and save it to folder `${screenshot-directory}/#{generate(Number.randomNumber)}.png`|

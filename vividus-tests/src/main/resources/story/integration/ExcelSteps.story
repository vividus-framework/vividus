Meta:
    @epic vividus-plugin-excel

Lifecycle:
Before:
Scope: STORY
When I issue a HTTP GET request for a resource with the URL 'https://github.com/vividus-framework/vividus/blob/master/vividus-plugin-excel/src/test/resources/TestTemplate.xlsx?raw=true'

Scenario: Step verification 'Then response contains excel sheet with index `$index` and records:$records'
Then response contains excel sheet with index `0` and records:
{valueSeparator=!}
|cellsRange|valueRegex             |
!A4:B5     !(Product|Price)\d+\s*  !
!B3        !Price                  !
!C1:C5     !                       !

Scenario: Step verification 'Then response contains excel sheet with name `$name` and records:$records'
Then response contains excel sheet with name `Mapping` and records:
{valueSeparator=!}
|cellsRange|valueRegex             |
!A4:B5     !(Product|Price)\d+\s*  !
!B3        !Price                  !
!C1:C5     !                       !

Scenario: Step verification 'When I create temporary excel file with content:$content and put path to $scopes variable `$variableName`'
Meta:
    @requirementId 1028
When I create temporary excel file with content:
|key1  |key2  |
|value1|value2|
and put path to scenario variable `path`
When I initialize the scenario variable `fileExists` with value `#{evalGroovy(return path.toFile().exists())}`
Then `${fileExists}` is = `true`

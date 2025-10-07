Meta:
    @epic vividus-engine
    @feature file-operations

Scenario: Verify file existence validation
Given I initialize scenario variable `temp-file-content` with value `Some value`
When I create temporary file with name `test-file.txt` and content `${temp-file-content}` and put path to scenario variable `temp-file-path`
Then file with name `test-file.txt` exists by path `temp-file-path`

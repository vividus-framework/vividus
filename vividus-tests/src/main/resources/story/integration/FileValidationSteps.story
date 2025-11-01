Meta:
    @epic vividus-engine
    @feature file-operations

Scenario: Verify file existence validation
Given I initialize scenario variable `test-dir` with value `${java.io.tmpdir}`
Given I initialize scenario variable `temp-file-content` with value `Some value`
When I create file with content `${temp-file-content}` at path `${test-dir}/test-file.txt`
Then file exists at path `${test-dir}/test-file.txt`

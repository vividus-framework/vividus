Meta:
    @epic vividus-engine
    @feature file-operations

Scenario: Verify file existence validation
Given I initialize scenario variable `test-dir` with value `${java.io.tmpdir}`
When I create file with content `${temp-file-content}` at path `${test-dir}/test-file.txt`
Then file with name `test-file.txt` exists by path `${test-dir}`

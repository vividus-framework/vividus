Meta:
    @epic vividus-core
    @feature file-operations

Scenario: Verify file existence validation
Given I initialize scenario variable `file-path` with value `${java.io.tmpdir}/test-file.txt`
When I create file with content `Some value` at path `${file-path}`
Then file exists at path `${file-path}`

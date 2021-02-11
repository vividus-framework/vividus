Meta:
    @epic vividus-core
    @feature executable-steps
    @issueId 805

Lifecycle:
Examples:
|var3|var4|
|1   |1   |

Scenario: Should resolve examples values in executable steps
When the condition `true` is true I do
|step                                        |
|When I use examples vars on the first level |
|When I use examples vars on the second level|
|When I use examples vars on the third level |
When I use examples vars on the first level
When I use examples vars on the second level
When I use examples vars on the third level

Examples:
|var|var2|
|1  |1   |

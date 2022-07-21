Meta:
    @epic vividus-engine

Scenario: Precondition with Examples
Meta: @id scenario-to-run
Then `<expected>` is equal to `<actual>`
Examples:
|expected|actual|
|1       |1     |
|2       |2     |

Scenario: Precondition with meta @id
Meta: @id given1
Then `<expected>` is equal to `<actual>`

Description: Integration tests for encryption functionality

Meta:
    @epic vividus-core
    @feature encryption

Scenario: Verify decrypted value is loaded from properties
Then `${encrypted-variable}` is equal to `my-secret`

Scenario: Verify that required parts of the value from properties decrypted
Meta:
    @requirementId 2000
Then `${partial-encrypted-variable}` is equal to `required username="my-username" password=my-secret; some-secret-value=top-secret`

Scenario: Verify decrypted value is loaded from system variables
Then `${system-property}` is equal to `Encrypted message`

Meta:
    @epic vividus-plugin-web-app
    @requirementId 1876

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}/webStorage.html`
Examples:
|storageType|key       |
|session    |sessionKey|
|local      |localKey  |

Scenario: Verify step: "Then $storageType storage item with key `$key` does not exist"
Then <storageType> storage item with key `<key>` does not exist

Scenario: Verify step: "Then $storageType storage item with key `$key` exists"
When I click on element located by `id(set-<storageType>-storage-item)`
Then <storageType> storage item with key `<key>` exists

Scenario: Verify step: "When I save $storageType storage item with key `$key` to $scopes variable `$variable`"
When I save <storageType> storage item with key `<key>` to scenario variable `<key>Value`
Then `${<key>Value}` is equal to `test#{capitalizeFirstWord(<storageType>)}Value`

Scenario: Verify step: "When I set $storageType storage item with key `$key` and value `$value`"
When I set <storageType> storage item with key `<key>FromTest` and value `test-<storageType>-value`
Then <storageType> storage item with key `<key>FromTest` exists
When I save <storageType> storage item with key `<key>FromTest` to scenario variable `<key>Value`
Then `${<key>Value}` is equal to `test-<storageType>-value`

Meta:
    @epic vividus-core

Scenario: Take objects from the initialized object pool
Given I initialize object pool `credentials` with data:
|login |password|
|user-1|pass-1  |
|user-2|pass-2  |
|user-3|pass-3  |

When I take object from pool `credentials` and save it to scenario variable `firstCredential`
When I take object from pool `credentials` and save it to scenario variable `secondCredential`
When I take object from pool `credentials` and save it to scenario variable `thirdCredential`

Given I initialize scenario variable `expectedJson` with value `[
    {
        "login": "user-1",
        "password": "pass-1"
    },
    {
        "login": "user-2",
        "password": "pass-2"
    },
    {
        "login": "user-3",
        "password": "pass-3"
    }
]`

Given I initialize scenario variable `actualJson` with value `[
    {
        "login": "${firstCredential.login}",
        "password": "${firstCredential.password}"
    },
    {
        "login": "${secondCredential.login}",
        "password": "${secondCredential.password}"
    },
    {
        "login": "${thirdCredential.login}",
        "password": "${thirdCredential.password}"
    }
]`

Then JSON element from `${actualJson}` by JSON path `$` is equal to `${expectedJson}`IGNORING_ARRAY_ORDER


Scenario: Add objects to the existing object pool
Given I initialize object pool `extraCredentials` with data:
|login |password|
|user-1|pass-1  |

When I take object from pool `extraCredentials` and save it to scenario variable `takenBeforeReplenish`

Given I add objects to object pool `extraCredentials`:
|login |password|
|user-4|pass-4  |
|user-5|pass-5  |

When I take object from pool `extraCredentials` and save it to scenario variable `firstAddedCredential`
When I take object from pool `extraCredentials` and save it to scenario variable `secondAddedCredential`

Given I initialize scenario variable `expectedJson` with value `[
    {
        "login": "user-4",
        "password": "pass-4"
    },
    {
        "login": "user-5",
        "password": "pass-5"
    }
]`

Given I initialize scenario variable `actualJson` with value `[
    {
        "login": "${firstAddedCredential.login}",
        "password": "${firstAddedCredential.password}"
    },
    {
        "login": "${secondAddedCredential.login}",
        "password": "${secondAddedCredential.password}"
    }
]`

Then `${takenBeforeReplenish.login}` is equal to `user-1`
Then JSON element from `${actualJson}` by JSON path `$` is equal to `${expectedJson}`IGNORING_ARRAY_ORDER


Scenario: Take objects from object pool with objects having nested structures
Given I initialize scenario variable `user` with value `{
    "login": "user-1",
    "password": "pass-1",
    "roles": [
        "admin",
        "editor"
    ],
    "address": {
        "city": "New York",
        "zip": "10001"
    }
}`

Given I initialize scenario variable `users` with value `[${user}]`

Given I initialize object pool `usersPool` with data:
{transformer=FROM_JSON, variableName=users, columns=json=$}

When I take object from pool `usersPool` and save it to scenario variable `userJson`
Then JSON element from `${userJson.json}` by JSON path `$` is equal to `${user}`


Scenario: Save object pool size and iterate over remaining objects
Given I initialize object pool `sizedCredentials` with data:
|login |password|
|user-1|pass-1  |
|user-2|pass-2  |
|user-3|pass-3  |

When I take object from pool `sizedCredentials` and save it to scenario variable `alreadyTaken`
When I save size of object pool `sizedCredentials` to scenario variable `poolSize`
Then `${poolSize}` is equal to `2`

When I execute steps while counter is less than `${poolSize}` with increment `1` starting from `0`:
|step                                                                                               |
|When I take object from pool `sizedCredentials` and save it to scenario variable `credential`      |
|Given I initialize scenario variable `login-${iterationVariable}` with value `${credential.login}` |

When I save size of object pool `sizedCredentials` to scenario variable `poolSizeAfterIteration`
Then `${poolSizeAfterIteration}` is equal to `0`

Given I initialize scenario variable `expectedJson` with value `["user-1", "user-2", "user-3"]`
Given I initialize scenario variable `actualJson` with value `["${alreadyTaken.login}", "${login-0}", "${login-1}"]`
Then JSON element from `${actualJson}` by JSON path `$` is equal to `${expectedJson}`IGNORING_ARRAY_ORDER

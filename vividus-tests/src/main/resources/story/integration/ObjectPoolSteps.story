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

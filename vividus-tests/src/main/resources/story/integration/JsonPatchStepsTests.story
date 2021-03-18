Description: Integration tests for JsonPatchSteps class.

Meta:
    @epic vividus-plugin-json

Scenario: Step verification 'When I patch JSON `$sourceJson` using `$jsonPatch` and save result to $scopes variable `$variableName`'
When I patch JSON `{"a":"b"}` using `[{ "op": "replace", "path": "/a", "value": "c" }]` and save result to SCENARIO variable `patchedJson`
Then `{"a":"c"}` is equal to `${patchedJson}`

Description: Story validates dynamic variables

Meta:
    @epic vividus-plugin-web-app
    @feature dynamic-variables

Lifecycle:
Before:
Scope: STORY
Given I am on page with URL `${vividus-test-site-url}`


Scenario: Verify: 'Contextual rectangle dynamic variables', 'When I save coordinates and size of element located by `$locator` to $scopes variable `$variableName'
Meta:
    @requirementId 802
!-- Deprecated
When I change context to element located by `tagName(img)`
Then `${context-height}`            is > `0`
Then `${context-width}`             is > `0`
Then `${context-x-coordinate}`      is > `0`
Then `${context-y-coordinate}`      is > `0`
When I reset context
!-- Replacement, move to GenericSetVariableSteps.story once the expressions are removed
When I save coordinates and size of element located by `tagName(img)` to scenario variable `rect`
Then `${rect.height}`            is = `400`
Then `${rect.width}`             is = `400`
!-- Browser window has borders of 8px size on Windows OS, that's why x == 192 on Windows and x == 200 on Linux and MacOS
!-- https://stackoverflow.com/a/42491227/2067574
Then `${rect.x}`                 is >= `192`
Then `${rect.x}`                 is <= `200`
Then `${rect.y}`                 is = `8`

Scenario: Verify `source-code` dynamic variable
Then `${source-code}` matches `.+Vividus Logo.+`

Scenario: Verify browser windows size dynamic variables
When I change window size to `600x500`
Then `${browser-window-height}` is = `500`
Then `${browser-window-width}`  is = `600`

Scenario: Verify `context-source-code` dynamic variable
Meta:
    @playwrightSupported
When I reset context
Then `${context-source-code}` matches `^(<!DOCTYPE html>)?<html><head>.+`
When I change context to element located by `name(vividus-logo)`
Then `${context-source-code}` matches `^<img name.+`

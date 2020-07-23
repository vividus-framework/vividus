Description: Integration tests for ApplicationSteps class

Meta:
    @epic vividus-plugin-mobile-app

Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Given I start mobile application with capabilities:
|name                            |value                                        |
|app                             |bs://444bd0308813ae0dc236f8cd461c02d3afa7901d|

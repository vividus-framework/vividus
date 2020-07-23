Description: Integration tests for ApplicationSteps class

Meta:
    @epic vividus-plugin-mobile-app

Scenario: Verify step: 'Given I start mobile application with capabilities:$capabilities'
Given I start mobile application with capabilities:
|name                            |value                                        |
|app                             |bs://c700ce60cf13ae8ed97705a55b8e022f13c5827c|

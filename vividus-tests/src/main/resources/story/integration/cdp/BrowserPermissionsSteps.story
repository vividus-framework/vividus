Meta:
    @capability.webSocketUrl true

!-- The reason we check "timeout expired" message on granting geolocation permission is because we've granted permission,
!-- but do not mock or override the location itself, so when the page calls navigator.geolocation.getCurrentPosition(),
!-- Chrome is sitting there trying to get a real GPS signal which doesn't exist and finally times out.

Lifecycle:
Examples:
{transformer=FROM_LANDSCAPE}
|denyInfoLocator |xpath(//p[contains(., 'User denied Geolocation')])|
|grantInfoLocator|xpath(//p[contains(., 'Timeout expired')])        |


Scenario: Verify step: When I set state of `$permission` browser permission to `$state` for `$origin` origin
When I set state of `geolocation` browser permission to `denied` for `${vividus-test-site-url}` origin
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`
When I wait until element located by `<denyInfoLocator>` appears
When I set state of `geolocation` browser permission to `granted` for `${vividus-test-site-url}` origin
When I refresh page
When I wait until element located by `<grantInfoLocator>` appears


Scenario: Verify step: When I set state of `$permission` browser permission to `$state`
When I set state of `geolocation` browser permission to `denied`
When I refresh page
When I wait until element located by `<denyInfoLocator>` appears
When I set state of `geolocation` browser permission to `granted`
When I refresh page
When I wait until element located by `<grantInfoLocator>` appears


Scenario: Verify step: When I configure browser permissions:$permissions
When I configure browser permissions:
|permissionName|state |origin                  |
|geolocation   |denied|${vividus-test-site-url}|
When I refresh page
When I wait until element located by `<denyInfoLocator>` appears
When I configure browser permissions:
|permissionName|state |origin                   |
|geolocation   |granted|${vividus-test-site-url}|
When I refresh page
When I wait until element located by `<grantInfoLocator>` appears

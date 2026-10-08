Meta:
    @capability.webSocketUrl true

!-- The location is emulated once the geolocation permission is granted, otherwise Chrome tries to get a real location
!-- from the OS provider, which is slow and non-deterministic (e.g. on Windows).

Lifecycle:
Examples:
{transformer=FROM_LANDSCAPE}
|denyInfoLocator|xpath(//p[contains(., 'User denied Geolocation')])|
|latitude       |55.488696                                          |
|longitude      |28.771732                                          |


Scenario: Verify step: When I set state of `$permission` browser permission to `$state` for `$origin` origin
When I set state of `geolocation` browser permission to `denied` for `${vividus-test-site-url}` origin
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`
When I wait until element located by `<denyInfoLocator>` appears
When I set state of `geolocation` browser permission to `granted` for `${vividus-test-site-url}` origin
When I emulate Geolocation using coordinates with latitude `<latitude>` and longitude `<longitude>`
When I refresh page
Then text `Latitude: <latitude>` exists
Then text `Longitude: <longitude>` exists
When I reset Geolocation emulation


Scenario: Verify step: When I set state of `$permission` browser permission to `$state`
When I set state of `geolocation` browser permission to `denied`
When I refresh page
When I wait until element located by `<denyInfoLocator>` appears
When I set state of `geolocation` browser permission to `granted`
When I emulate Geolocation using coordinates with latitude `<latitude>` and longitude `<longitude>`
When I refresh page
Then text `Latitude: <latitude>` exists
Then text `Longitude: <longitude>` exists
When I reset Geolocation emulation


Scenario: Verify step: When I configure browser permissions:$permissions
When I configure browser permissions:
|permissionName|state |origin                  |
|geolocation   |denied|${vividus-test-site-url}|
When I refresh page
When I wait until element located by `<denyInfoLocator>` appears
When I configure browser permissions:
|permissionName|state |origin                   |
|geolocation   |granted|${vividus-test-site-url}|
When I emulate Geolocation using coordinates with latitude `<latitude>` and longitude `<longitude>`
When I refresh page
Then text `Latitude: <latitude>` exists
Then text `Longitude: <longitude>` exists
When I reset Geolocation emulation

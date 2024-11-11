!-- This test does not work in 'old' headless mode, TODO: move test to integration suite once it is migrated to 'new' headless mode

Scenario: Validate steps: 'When I emulate Geolocation using coordinates with latitude `$latitude` and longitude `$longitude`', 'When I reset Geolocation emulation'
Given I am on page with URL `${vividus-test-site-url}/geolocation.html`
Then text `Latitude: <latitude>` does not exist
Then text `Longitude: <longitude>` does not exist
When I emulate Geolocation using coordinates with latitude `<latitude>` and longitude `<longitude>`
When I refresh page
Then text `Latitude: <latitude>` exists
Then text `Longitude: <longitude>` exists
When I reset Geolocation emulation
When I refresh page
Then text `Latitude: <latitude>` does not exist
Then text `Longitude: <longitude>` does not exist
Examples:
|latitude  |longitude |
|55.488696 |28.771732 |

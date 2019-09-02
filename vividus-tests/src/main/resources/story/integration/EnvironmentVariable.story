Description: Integration tests for environment variables

Meta:
    @group environment-variables

Scenario: Verify environment variable is loaded from local system
Then `${java}` matches `.*(jdk|jre){1}.*`

Scenario: Verify combination of environment variables are loaded in one property
Then `${var}` matches `[JAVA_HOME=].*(jdk|jre){1}.*;[Path=][^${PATH}].*`

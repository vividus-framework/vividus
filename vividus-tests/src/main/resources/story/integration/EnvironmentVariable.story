Description: Integration tests for environment variables

Meta:
    @epic vividus-core
    @feature environment-variables

Scenario: Verify environment variable is loaded from local system
Then `${java}` matches `.*(jdk|jre){1}.*`

Scenario: Verify combination of environment variables are loaded in one property
Then `${var}` matches `JAVA_HOME=.*(jdk|jre|java){1}.*;Path=((?!\{PATH}).)*`
